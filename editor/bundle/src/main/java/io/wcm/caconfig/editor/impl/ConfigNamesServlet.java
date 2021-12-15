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

import static io.wcm.caconfig.editor.impl.JsonMapper.OBJECT_MAPPER;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.wcm.caconfig.editor.impl.data.confignames.ConfigNameItem;
import io.wcm.caconfig.editor.impl.data.confignames.ConfigNamesResponse;

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
    ConfigNamesResponse result = new ConfigNamesResponse();
    result.setContextPath(getContextPath(contextResource));
    result.setConfigNames(getConfigNames(contextResource));

    response.setContentType("application/json;charset=" + StandardCharsets.UTF_8.name());
    response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
  }

  private String getContextPath(Resource contextResource) {
    return configurationResourceResolver.getContextPath(contextResource);
  }

  private Collection<ConfigNameItem> getConfigNames(Resource contextResource) {
    SortedSet<String> configNames = configManager.getConfigurationNames();
    SortedSet<ConfigNameItem> sortedResult = new TreeSet<>((ConfigNameItem o1, ConfigNameItem o2) -> {
        String label1 = o1.getLabel();
        String label2 = o2.getLabel();
        if (StringUtils.equals(label1, label2)) {
          String configName1 = o1.getConfigName();
          String configName2 = o2.getConfigName();
          return configName1.compareTo(configName2);
        }
        return label1.compareTo(label2);
    });
    for (String configName : configNames) {
      ConfigurationMetadata metadata = configManager.getConfigurationMetadata(configName);
      if (metadata != null) {
        ConfigNameItem item = new ConfigNameItem();
        item.setConfigName(configName);
        item.setLabel(metadata.getLabel());
        item.setDescription(metadata.getDescription());
        item.setCollection(metadata.isCollection());

        ConfigurationState state = getConfigurationState(contextResource, configName, metadata.isCollection());
        item.setExists(state.exists);
        item.setInherited(state.inherited);
        item.setOverridden(state.overridden);

        item.setAllowAdd(allowAdd(contextResource, configName));
        sortedResult.add(item);
      }
    }
    return sortedResult;
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
        result.exists = config.getResourcePath() != null || config.isOverridden();
        result.inherited = config.isInherited();
        result.overridden = config.isOverridden();
      }
    }
    return result;
  }

  private static final class ConfigurationState {
    private boolean exists;
    private boolean inherited;
    private boolean overridden;
  }

}
