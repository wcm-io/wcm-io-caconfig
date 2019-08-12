/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
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
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Get configuration names with labels and descriptions.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.resourceTypes=/apps/wcm-io/caconfig/editor/components/page/editor",
    "sling.servlet.extensions=json",
    "sling.servlet.selectors=" + ConfigNamesServlet.SELECTOR,
    "sling.servlet.methods=GET"
})
public class ConfigNamesServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configNames";

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private ConfigurationResourceResolver configurationResourceResolver;
  @Reference
  private EditorConfig editorConfig;
  @Reference(policy = ReferencePolicy.STATIC, cardinality = ReferenceCardinality.OPTIONAL,
      policyOption = ReferencePolicyOption.GREEDY)
  private ConfigurationEditorFilterService configurationEditorFilterService;

  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
    if (!editorConfig.isEnabled()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    Resource contextResource = request.getResource();
    try {
      JSONObject result = new JSONObject();
      result.putOpt("contextPath", getContextPath(contextResource));
      result.put("configNames", getConfigNames(contextResource));

      response.setContentType("application/json;charset=" + CharEncoding.UTF_8);
      response.getWriter().write(result.toString());
    }
    catch (JSONException ex) {
      throw new ServletException("Unable to generate JSON.", ex);
    }
  }

  private String getContextPath(Resource contextResource) {
    return configurationResourceResolver.getContextPath(contextResource);
  }

  private JSONArray getConfigNames(Resource contextResource) throws JSONException {
    JSONArray output = new JSONArray();

    SortedSet<String> configNames = configManager.getConfigurationNames();
    SortedSet<JSONObject> sortedResult = new TreeSet<>(new Comparator<JSONObject>() {
      @Override
      public int compare(JSONObject o1, JSONObject o2) {
        String label1 = o1.optString("label");
        String label2 = o2.optString("label");
        if (StringUtils.equals(label1, label2)) {
          String configName1 = o1.optString("configName");
          String configName2 = o2.optString("configName");
          return configName1.compareTo(configName2);
        }
        return label1.compareTo(label2);
      }
    });
    for (String configName : configNames) {
      ConfigurationMetadata metadata = configManager.getConfigurationMetadata(configName);
      if (metadata != null) {
        JSONObject item = new JSONObject();
        item.put("configName", configName);
        item.putOpt("label", metadata.getLabel());
        item.putOpt("description", metadata.getDescription());
        item.put("collection", metadata.isCollection());

        ConfigurationState state = getConfigurationState(contextResource, configName, metadata.isCollection());
        item.put("exists", state.exists);
        item.put("inherited", state.inherited);
        item.put("overridden", state.overridden);

        item.put("allowAdd", allowAdd(contextResource, configName));
        sortedResult.add(item);
      }
    }

    sortedResult.forEach(output::put);

    return output;
  }

  private boolean allowAdd(Resource contextResource, String configName) {
    if (configurationEditorFilterService == null) {
      return true;
    }
    return configurationEditorFilterService.allowAdd(contextResource, configName);
  }

  private ConfigurationState getConfigurationState(Resource contextResource, String configName, boolean collection) {
    ConfigurationState result = new ConfigurationState();
    if (collection) {
      Collection<ConfigurationData> configs = configManager.getConfigurationCollection(contextResource, configName).getItems();
      result.exists = !configs.isEmpty();
      result.inherited = configs.stream().filter(ConfigurationData::isInherited).findAny().isPresent();
      result.overridden = configs.stream().filter(ConfigurationData::isOverridden).findAny().isPresent();
    }
    else {
      ConfigurationData config = configManager.getConfiguration(contextResource, configName);
      if (config != null) {
        result.exists = config.getResourcePath() != null;
        result.inherited = config.isInherited();
        result.overridden = config.isOverridden();
      }
    }
    return result;
  }

  private final class ConfigurationState {
    private boolean exists;
    private boolean inherited;
    private boolean overridden;
  }

}
