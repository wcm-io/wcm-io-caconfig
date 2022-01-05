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

import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_DROPDOWN_OPTIONS;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_DROPDOWN_OPTIONS_PROVIDER;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_WIDGET_TYPE;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_DROPDOWN;
import static io.wcm.caconfig.editor.impl.JsonMapper.OBJECT_MAPPER;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_COLLECTION;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.ValueInfo;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.caconfig.editor.impl.data.configdata.ConfigCollectionItem;
import io.wcm.caconfig.editor.impl.data.configdata.ConfigItem;
import io.wcm.caconfig.editor.impl.data.configdata.PropertyItem;
import io.wcm.caconfig.editor.impl.data.configdata.PropertyItemMetadata;

/**
 * Read configuration data.
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    resourceTypes = "/apps/wcm-io/caconfig/editor/components/page/editor",
    selectors = ConfigDataServlet.SELECTOR,
    extensions = "json",
    methods = "GET")
public class ConfigDataServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configData";

  private static final Pattern JSON_STRING_ARRAY_PATTERN = Pattern.compile("^\\[.*\\]$");
  private static final Pattern JSON_STRING_OBJECT_PATTERN = Pattern.compile("^\\{.*\\}$");

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private ConfigurationPersistenceStrategyMultiplexer configurationPersistenceStrategy;
  @Reference
  private EditorConfig editorConfig;
  @Reference
  private DropdownOptionProviderService dropdownOptionProviderService;

  private static Logger log = LoggerFactory.getLogger(ConfigDataServlet.class);

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
    if (!editorConfig.isEnabled()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    // get parameters
    String configName = request.getParameter(RP_CONFIGNAME);
    if (StringUtils.isBlank(configName)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    boolean collection = BooleanUtils.toBoolean(request.getParameter(RP_COLLECTION));

    // output configuration
    try {
      Object result = getConfiguration(request.getResource(), configName, collection);
      if (result == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else {
        response.setContentType("application/json;charset=" + StandardCharsets.UTF_8.name());
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
      }
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      log.error("Error getting configuration for " + configName + (collection ? "[col]" : ""), ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  private Object getConfiguration(@NotNull Resource contextResource, String configName, boolean collection) {
    Object result;
    if (collection) {
      ConfigurationData newItem = configManager.newCollectionItem(contextResource, configName);
      if (newItem == null) {
        throw new ConfigurationPersistenceException("Invalid configuration name: " + configName);
      }
      result = fromConfigCollection(contextResource,
          configManager.getConfigurationCollection(contextResource, configName), newItem, configName);
    }
    else {
      ConfigurationData configData = configManager.getConfiguration(contextResource, configName);
      if (configData != null) {
        result = fromConfig(contextResource, configData, configData.isInherited(), configName);
      }
      else {
        result = null;
      }
    }
    return result;
  }

  private ConfigCollectionItem fromConfigCollection(@NotNull Resource contextResource,
      ConfigurationCollectionData configCollection, ConfigurationData newItem, String fullConfigName) {
    ConfigCollectionItem result = new ConfigCollectionItem();
    result.setConfigName(configCollection.getConfigName());

    if (!configCollection.getProperties().isEmpty()) {
      Map<String, Object> properties = new TreeMap<>();
      for (Map.Entry<String, Object> entry : configCollection.getProperties().entrySet()) {
        properties.put(entry.getKey(), entry.getValue());
      }
      result.setProperties(properties);
    }

    List<ConfigItem> items = new ArrayList<>();
    for (ConfigurationData configData : configCollection.getItems()) {
      items.add(fromConfig(contextResource, configData, configData.isInherited(), fullConfigName));
    }
    result.setItems(items);

    result.setNewItem(fromConfig(contextResource, newItem, null, fullConfigName));

    return result;
  }

  private ConfigItem fromConfig(@NotNull Resource contextResource, ConfigurationData config, Boolean inherited, String fullConfigName) {
    ConfigItem result = new ConfigItem();

    result.setConfigName(config.getConfigName());
    result.setCollectionItemName(config.getCollectionItemName());
    result.setOverridden(config.isOverridden());
    result.setInherited(inherited);

    List<PropertyItem> props = new ArrayList<>();
    for (String propertyName : config.getPropertyNames()) {
      ValueInfo<?> item = config.getValueInfo(propertyName);
      if (item == null) {
        continue;
      }
      PropertyMetadata<?> itemMetadata = item.getPropertyMetadata();

      PropertyItem prop = new PropertyItem();
      prop.setName(item.getName());

      // special handling for nested configs and nested config collections
      if (itemMetadata != null && itemMetadata.isNestedConfiguration()) {
        PropertyItemMetadata metadata = new PropertyItemMetadata();
        metadata.setLabel(itemMetadata.getLabel());
        metadata.setDescription(itemMetadata.getDescription());
        metadata.setProperties(toJsonWithValueConversion(itemMetadata.getProperties(), contextResource));
        prop.setMetadata(metadata);

        if (itemMetadata.getType().isArray()) {
          ConfigurationData[] configDatas = (ConfigurationData[])item.getValue();
          if (configDatas != null) {
            ConfigCollectionItem nestedConfigCollection = new ConfigCollectionItem();
            StringBuilder collectionConfigName = new StringBuilder();
            if (config.getCollectionItemName() != null) {
              collectionConfigName.append(configurationPersistenceStrategy.getCollectionItemConfigName(fullConfigName
                      + "/" + config.getCollectionItemName(), config.getResourcePath()));
            }
            else {
              collectionConfigName.append(configurationPersistenceStrategy.getConfigName(fullConfigName, config.getResourcePath()));
            }
            collectionConfigName.append("/").append(itemMetadata.getConfigurationMetadata().getName());
            nestedConfigCollection.setConfigName(collectionConfigName.toString());
            List<ConfigItem> items = new ArrayList<>();
            for (ConfigurationData configData : configDatas) {
              items.add(fromConfig(contextResource, configData, false, collectionConfigName.toString()));
            }
            nestedConfigCollection.setItems(items);
            prop.setNestedConfigCollection(nestedConfigCollection);
          }
        }
        else {
          ConfigurationData configData = (ConfigurationData)item.getValue();
          if (configData != null) {
            prop.setNestedConfig(fromConfig(contextResource, configData, null, fullConfigName
                + "/" + itemMetadata.getConfigurationMetadata().getName()));
          }
        }
      }

      // property data and metadata
      else {
        prop.setValue(item.getValue());
        prop.setEffectiveValue(item.getEffectiveValue());
        prop.setConfigSourcePath(item.getConfigSourcePath());
        prop.setIsDefault(item.isDefault());
        prop.setInherited(item.isInherited());
        prop.setOverridden(item.isOverridden());

        if (itemMetadata != null) {
          PropertyItemMetadata metadata = new PropertyItemMetadata();
          if (itemMetadata.getType().isArray()) {
            metadata.setType(ClassUtils.primitiveToWrapper(itemMetadata.getType().getComponentType()).getSimpleName());
            metadata.setMultivalue(true);
          }
          else {
            metadata.setType(ClassUtils.primitiveToWrapper(itemMetadata.getType()).getSimpleName());
          }
          metadata.setDefaultValue(itemMetadata.getDefaultValue());
          metadata.setLabel(itemMetadata.getLabel());
          metadata.setDescription(itemMetadata.getDescription());
          metadata.setProperties(toJsonWithValueConversion(itemMetadata.getProperties(), contextResource));
          prop.setMetadata(metadata);
        }
      }
      props.add(prop);
    }
    result.setProperties(props);

    return result;
  }

  /**
   * Converts the given map to JSON. Each map value is checked for a valid JSON string - if this is the case it's
   * inserted as JSON objects and not as string.
   * @param properties Map
   * @param contextResource Context resource
   * @return JSON object
   */
  private @Nullable Map<String, Object> toJsonWithValueConversion(@Nullable Map<String, String> properties,
      @NotNull Resource contextResource) {
    if (properties == null || properties.isEmpty()) {
      return null;
    }

    Map<String, Object> metadataProps = new TreeMap<>();
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      metadataProps.put(entry.getKey(), tryConvertJsonString(entry.getValue()));
    }

    // check for dynamic dropdown option injection
    boolean isDropdown = WIDGET_TYPE_DROPDOWN.equals(metadataProps.get(PROPERTY_WIDGET_TYPE));
    if (isDropdown) {
      Optional<String> dynamicProvider = Optional.ofNullable(metadataProps.get(PROPERTY_DROPDOWN_OPTIONS_PROVIDER))
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .filter(StringUtils::isNotBlank);
      if (dynamicProvider.isPresent()) {
        List<Map<String, Object>> items = dropdownOptionProviderService.getDropdownOptions(dynamicProvider.get(), contextResource);
        if (!items.isEmpty()) {
          metadataProps.put(PROPERTY_DROPDOWN_OPTIONS, items);
        }
        metadataProps.remove(PROPERTY_DROPDOWN_OPTIONS_PROVIDER);
      }
    }

    return metadataProps;
  }

  private @Nullable Object tryConvertJsonString(@Nullable String value) {
    if (value == null) {
      return null;
    }
    if (JSON_STRING_ARRAY_PATTERN.matcher(value).matches()) {
      try {
         return OBJECT_MAPPER.readValue(value, List.class);
      }
      catch (IOException ex) {
        // no valid json - ignore
        log.trace("Conversion to JSON arary value failed for: {}", value, ex);
      }
    }
    if (JSON_STRING_OBJECT_PATTERN.matcher(value).matches()) {
      try {
        return OBJECT_MAPPER.readValue(value, Map.class);
      }
      catch (IOException ex) {
        // no valid json - ignore
        log.trace("Conversion to JSON object value failed for: {}", value, ex);
      }
    }
    return value;
  }

}
