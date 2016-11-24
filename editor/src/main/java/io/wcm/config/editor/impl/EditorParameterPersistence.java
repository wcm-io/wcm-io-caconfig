/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.config.editor.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.core.management.ConfigurationFinder;
import io.wcm.config.core.management.ParameterPersistence;
import io.wcm.config.core.management.ParameterPersistenceData;
import io.wcm.config.core.management.ParameterResolver;
import io.wcm.config.core.management.util.TypeConversion;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Persists configuration parameters
 */
@SlingServlet(
    resourceTypes = {
        "/apps/wcm-io/config/editor/components/page/editor"
    },
    extensions = FileExtension.JSON,
    selectors = "configProvider",
    methods = HttpConstants.METHOD_POST)
public class EditorParameterPersistence extends SlingAllMethodsServlet {
  private static final long serialVersionUID = 1L;

  @Reference
  private ConfigurationFinder configurationFinder;
  @Reference
  private ParameterPersistence persistence;
  @Reference
  private ParameterResolver parameterResolver;

  static final String MAP_KEY_SUFFIX = "$key";

  private static final Logger log = LoggerFactory.getLogger(EditorParameterPersistence.class);

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    String configurationId = getCurrentConfigurationId(request);
    if (StringUtils.isEmpty(configurationId)) {
      log.error("Could not find configuration id for resource {}", request.getResource().getPath());
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find configuration id for resource " + request.getResource().getPath());
      return;
    }

    try {
      persistence.storeData(request.getResourceResolver(), configurationId, getPersistenceData(request), false);
    }
    catch (Throwable ex) {
      log.error("Could not persist data for configuration id {}", configurationId, ex);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  protected ParameterPersistenceData getPersistenceData(SlingHttpServletRequest request) {
    Map<String, Parameter<?>> parameters = getParametersMap(parameterResolver.getAllParameters());
    Enumeration<String> requestParameterNames = request.getParameterNames();
    Map<String, Object> values = new HashMap<>();
    SortedSet<String> lockedParameterNames = ImmutableSortedSet.<String>of();
    while (requestParameterNames.hasMoreElements()) {
      String parameterName = requestParameterNames.nextElement();
      Parameter<?> parameter = parameters.get(parameterName);
      if (parameter != null) {
        Object value;
        if (parameter.getType() == Map.class) {
          value = getMapValue(request.getParameterValues(parameterName + MAP_KEY_SUFFIX), request.getParameterValues(parameterName));
        }
        else {
          value = getValue(request.getParameterValues(parameterName), parameter);
        }
        if (value != null) {
          values.put(parameterName, value);
        }
      }
      else if (StringUtils.equals(ParameterPersistence.PN_LOCKED_PARAMETER_NAMES, parameterName)) {
        lockedParameterNames = getLockedParameterNames(request.getParameterValues(parameterName));
      }
    }
    return new ParameterPersistenceData(values, lockedParameterNames);
  }

  private Map<String, Parameter<?>> getParametersMap(Set<Parameter<?>> allParameters) {
    Map<String, Parameter<?>> result = new HashMap<>();
    Iterator<Parameter<?>> iterator = allParameters.iterator();
    while (iterator.hasNext()) {
      Parameter parameter = iterator.next();
      result.put(parameter.getName(), parameter);
    }
    return result;
  }

  private SortedSet<String> getLockedParameterNames(String[] lockedParameterValues) {
    if (lockedParameterValues != null && lockedParameterValues.length > 0) {
      return ImmutableSortedSet.copyOf(lockedParameterValues);
    }
    return ImmutableSortedSet.<String>of();
  }

  private Object getValue(String[] values, Parameter<?> parameter) {
    Object value = null;
    if (values != null && values.length > 0) {
      if (parameter.getType() == String[].class) {
        value = values;
      }
      else {
        value = TypeConversion.stringToObject(values[0], parameter.getType());
      }
    }
    return value;
  }

  private Map<String, String> getMapValue(String[] keys, String[] values) {
    Map<String, String> map = null;
    if (keys != null && keys.length > 0 && values != null && values.length > 0) {
      map = new HashMap<>();
      for (int i = 0; i < keys.length && i < values.length; i++) {
        map.put(keys[i], values[i]);
      }
    }
    return map;
  }

  private String getCurrentConfigurationId(SlingHttpServletRequest request) {
    if (configurationFinder != null) {
      Resource resource = request.getResource();
      Configuration configuration = configurationFinder.find(resource);
      return configuration != null ? configuration.getConfigurationId() : StringUtils.EMPTY;
    }

    return StringUtils.EMPTY;

  }
}
