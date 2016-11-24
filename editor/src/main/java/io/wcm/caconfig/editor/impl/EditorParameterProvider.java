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
package io.wcm.caconfig.editor.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

import io.wcm.caconfig.editor.EditorProperties;
import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.core.management.Application;
import io.wcm.config.core.management.ApplicationFinder;
import io.wcm.config.core.management.ConfigurationFinder;
import io.wcm.config.core.management.ParameterOverride;
import io.wcm.config.core.management.ParameterPersistence;
import io.wcm.config.core.management.ParameterPersistenceData;
import io.wcm.config.core.management.ParameterResolver;
import io.wcm.config.core.management.util.TypeConversion;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Exports the list of parameters available for the current application in JSON format to the response.
 */
@SlingServlet(
    resourceTypes = {
        "/apps/wcm-io/caconfig/editor/components/page/editor"
    },
    extensions = FileExtension.JSON,
    selectors = "configProvider",
    methods = HttpConstants.METHOD_GET)
public class EditorParameterProvider extends SlingAllMethodsServlet {
  private static final long serialVersionUID = 1L;

  @Reference
  private ConfigurationFinder configurationFinder;
  @Reference
  private ApplicationFinder applicationFinder;
  @Reference
  private ParameterPersistence persistence;
  @Reference
  private ParameterResolver parameterResolver;
  @Reference
  private ParameterOverride parameterOverride;

  private static final Logger log = LoggerFactory.getLogger(EditorParameterProvider.class);

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    Configuration[] allConfigurations = Iterators.toArray(getConfigurations(request), Configuration.class);
    Map<String, Application> applicationsMap = getApplicationsMap();
    JSONArray parameters = new JSONArray();

    for (int i = allConfigurations.length - 1; i >= 0; i--) {
      Configuration configuration = allConfigurations[i];
      try {
        addParameters(parameters, configuration, request, applicationsMap);
      }
      catch (JSONException ex) {
        log.error("Could not parse parameters for the configuration {}", configuration.getConfigurationId(), ex);
        response.sendError(500);
      }
    }

    writeResponse(response, parameters);
  }

  private Map<String, Application> getApplicationsMap() {
    Map<String, Application> result = new HashMap<>();
    Set<Application> applications = applicationFinder.getAll();
    if (applications != null) {
      Iterator<Application> iterator = applications.iterator();
      while (iterator.hasNext()) {
        Application application = iterator.next();
        result.put(application.getApplicationId(), application);
      }

    }
    return result;
  }

  protected void addParameters(JSONArray parameters, Configuration configuration, SlingHttpServletRequest request, Map<String, Application> applicationsMap)
      throws JSONException {

    Set<Parameter<?>> allParameters = parameterResolver.getAllParameters();
    ParameterPersistenceData persistedData = persistence.getData(request.getResourceResolver(), configuration.getConfigurationId());
    Map<String, Object> persistedValues = persistedData.getValues();

    // get locked parameter names from persisted configuration, and add those from overrides
    Set<String> lockedParameterNames = new HashSet<>(persistedData.getLockedParameterNames());
    lockedParameterNames.addAll(parameterOverride.getLockedParameterNames(configuration.getConfigurationId()));
    Set<String> overrideLockedParameterNames = parameterOverride.getLockedParameterNames(configuration.getConfigurationId());

    Iterator<Parameter<?>> parameterIterator = allParameters.iterator();
    while (parameterIterator.hasNext()) {
      Parameter parameter = parameterIterator.next();
      String parameterName = parameter.getName();
      if (isEditable(parameter)) {
        JSONObject jsonParameter = getOrCreateJSONParameter(parameters, parameterName);
        addWidgetConfiguration(jsonParameter, parameter, applicationsMap);

        Object effectiveValue = configuration.get(parameterName);
        Object persistedValue = persistedValues.get(parameterName);

        // set the locked and inheritedLocked flags
        setLocked(jsonParameter, parameterName, lockedParameterNames, overrideLockedParameterNames);

        // set inherited flag
        setInherited(jsonParameter, effectiveValue, persistedValue);

        addValue(jsonParameter, effectiveValue, parameter, configuration.getConfigurationId());
        addLabel(jsonParameter, parameter);
      }
    }
  }

  private void addLabel(JSONObject jsonParameter, Parameter parameter) throws JSONException {
    ValueMap properties = parameter.getProperties();
    jsonParameter.put(EditorProperties.LABEL, StringUtils.defaultString(
        properties.get(EditorProperties.LABEL, String.class),
        parameter.getName()));
  }

  private void setInherited(JSONObject jsonParameter, Object effectiveValue, Object persistedValue) throws JSONException {
    if (persistedValue == null) {
      if (effectiveValue != null) {
        jsonParameter.put("inherited", true);
      }
      else {
        jsonParameter.put("inherited", false);
      }
    }
    else {
      jsonParameter.put("inherited", false);
    }
  }

  private void setLocked(JSONObject jsonParameter, String parameterName, Set<String> lockedParameterNames,
      Set<String> overrideLockedParameterNames) throws JSONException {
    if (overrideLockedParameterNames.contains(parameterName)) {
      jsonParameter.put(EditorNameConstants.LOCKED, true);
      jsonParameter.put(EditorNameConstants.LOCKED_INHERITED, true);
    }
    else if (lockedParameterNames.contains(parameterName)) {
      jsonParameter.put(EditorNameConstants.LOCKED, true);
      jsonParameter.put(EditorNameConstants.LOCKED_INHERITED, false);
    }
    else if (jsonParameter.has(EditorNameConstants.LOCKED) && jsonParameter.getBoolean(EditorNameConstants.LOCKED)) {
      jsonParameter.put(EditorNameConstants.LOCKED_INHERITED, true);
    }
    else {
      jsonParameter.put(EditorNameConstants.LOCKED, false);
      jsonParameter.put(EditorNameConstants.LOCKED_INHERITED, false);
    }
  }

  private void addValue(JSONObject jsonParameter, Object effectiveValue, Parameter<?> parameter, String configurationId) throws JSONException {
    Object previousValue = null;

    try {
      previousValue = jsonParameter.get(EditorNameConstants.PARAMETER_VALUE);
    }
    catch (JSONException ex) {
      previousValue = parameterOverride.getOverrideForce(configurationId, parameter);
      if (previousValue == null) {
        previousValue = parameterOverride.getOverrideSystemDefault(parameter);
      }
      if (previousValue == null) {
        previousValue = parameter.getDefaultValue();
      }
    }

    jsonParameter.put(EditorNameConstants.INHERITED_VALUE, getJSONValue(previousValue));
    jsonParameter.put(EditorNameConstants.PARAMETER_VALUE, getJSONValue(effectiveValue));

  }

  @SuppressWarnings("unchecked")
  private Object getJSONValue(Object value) {
    if (value instanceof Boolean) {
      return value;
    }
    else if (value instanceof String[]) {
      return new JSONArray(Arrays.asList((String[])value));
    }
    else if (value instanceof Map) {
      JSONArray jsonMap = new JSONArray();
      for (Map.Entry<String, String> entry : ((Map<String, String>)value).entrySet()) {
        JSONObject item = new JSONObject();
        try {
          item.putOpt("key", entry.getKey());
          item.putOpt("value", entry.getValue());
        }
        catch (JSONException ex) {
          throw new RuntimeException("Error converting value '" + value + "' to JSON.", ex);
        }
        jsonMap.put(item);
      }
      return jsonMap;
    }
    return TypeConversion.objectToString(value);
  }

  private JSONObject getOrCreateJSONParameter(JSONArray parameters, String parameterName) throws JSONException {
    JSONObject jsonParameter = new JSONObject();
    jsonParameter.put(EditorNameConstants.PARAMETER_NAME, parameterName);
    boolean found = false;
    for (int i = 0; i < parameters.length(); i++) {
      JSONObject parameter = parameters.getJSONObject(i);
      if (StringUtils.equals(parameter.getString(EditorNameConstants.PARAMETER_NAME), parameterName)) {
        jsonParameter = parameter;
        found = true;
        break;
      }
    }
    if (!found) {
      parameters.put(jsonParameter);
    }
    return jsonParameter;
  }

  private void addWidgetConfiguration(JSONObject jsonParameter, Parameter parameter, Map<String, Application> applicationsMap) throws JSONException {
    ValueMap parameterProperties = parameter.getProperties();
    if (parameterProperties != null) {
      for (Map.Entry<String, Object> entry : parameterProperties.entrySet()) {
        jsonParameter.put(entry.getKey(), entry.getValue());
      }
    }
    Application application = applicationsMap.get(parameter.getApplicationId());
    String appName = application != null ? application.getLabel() : parameter.getApplicationId();
    jsonParameter.put(EditorNameConstants.APPLICATION_ID, appName);
    jsonParameter.put(EditorNameConstants.PARAMETER_NAME, parameter.getName());
  }

  private boolean isEditable(Parameter parameter) {
    ValueMap parameterProperties = parameter.getProperties();
    return StringUtils.isNotEmpty(parameterProperties.get(EditorProperties.WIDGET_TYPE, ""));
  }

  private void writeResponse(SlingHttpServletResponse response, JSONArray parameters) throws IOException, ServletException {
    JSONObject result = new JSONObject();
    try {
      result.put("parameters", parameters);
      response.setContentType(ContentType.JSON);
      response.getWriter().write(result.toString());
    }
    catch (JSONException ex) {
      throw new ServletException(ex);
    }
  }


  private Iterator<Configuration> getConfigurations(SlingHttpServletRequest request) {
    if (configurationFinder != null) {
      Resource resource = request.getResource();
      return configurationFinder.findAll(resource);
    }

    return Iterators.emptyIterator();
  }


}
