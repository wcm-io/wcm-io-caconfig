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

import static io.wcm.caconfig.editor.impl.NameConstants.RP_COLLECTION;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharEncoding;
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
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read configuration data.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.resourceTypes=/apps/wcm-io/caconfig/editor/components/page/editor",
    "sling.servlet.extensions=json",
    "sling.servlet.selectors=" + ConfigDataServlet.SELECTOR,
    "sling.servlet.methods=GET"
})
public class ConfigDataServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configData";

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private ConfigurationPersistenceStrategyMultiplexer configurationPersistenceStrategy;
  @Reference
  private EditorConfig editorConfig;

  private static Logger log = LoggerFactory.getLogger(ConfigDataServlet.class);

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
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
      JSONObject result = getConfiguration(request.getResource(), configName, collection);
      if (result == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else {
        response.setContentType("application/json;charset=" + CharEncoding.UTF_8);
        response.getWriter().write(result.toString());
      }
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      log.error("Error getting configuration for " + configName + (collection ? "[col]" : ""), ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  private JSONObject getConfiguration(Resource contextResource, String configName, boolean collection) throws JSONException {
    JSONObject result;
    if (collection) {
      result = toJson(configManager.getConfigurationCollection(contextResource, configName),
          configManager.newCollectionItem(contextResource, configName));
    }
    else {
      ConfigurationData configData = configManager.getConfiguration(contextResource, configName);
      if (configData != null) {
        result = toJson(configData);
      }
      else {
        result = null;
      }
    }
    return result;
  }

  private JSONObject toJson(ConfigurationCollectionData configCollection, ConfigurationData newItem) throws JSONException {
    JSONObject result = new JSONObject();
    result.putOpt("configName", configCollection.getConfigName());

    if (!configCollection.getProperties().isEmpty()) {
      JSONObject properties = new JSONObject();
      for (Map.Entry<String, Object> entry : configCollection.getProperties().entrySet()) {
        properties.putOpt(entry.getKey(), entry.getValue());
      }
      result.put("properties", properties);
    }

    JSONArray items = new JSONArray();
    for (ConfigurationData configData : configCollection.getItems()) {
      items.put(toJson(configData));
    }
    result.put("items", items);

    result.put("newItem", toJson(newItem));

    return result;
  }

  private JSONObject toJson(ConfigurationData config) throws JSONException {
    JSONObject result = new JSONObject();

    result.putOpt("configName", config.getConfigName());
    result.putOpt("collectionItemName", config.getCollectionItemName());
    result.putOpt("overridden", config.isOverridden());

    JSONArray props = new JSONArray();
    for (String propertyName : config.getPropertyNames()) {
      ValueInfo<?> item = config.getValueInfo(propertyName);
      PropertyMetadata<?> itemMetadata = item.getPropertyMetadata();

      JSONObject prop = new JSONObject();
      prop.putOpt("name", item.getName());

      // special handling for nested configs and nested config collections
      if (itemMetadata != null && itemMetadata.isNestedConfiguration()) {
        JSONObject metadata = new JSONObject();
        metadata.putOpt("label", itemMetadata.getLabel());
        metadata.putOpt("description", itemMetadata.getDescription());
        metadata.putOpt("properties", toJson(itemMetadata.getProperties()));
        prop.put("metadata", metadata);

        if (itemMetadata.getType().isArray()) {
          ConfigurationData[] configDatas = (ConfigurationData[])item.getValue();
          if (configDatas != null) {
            JSONObject nestedConfigCollection = new JSONObject();
            String collectionConfigName = configurationPersistenceStrategy.getCollectionParentConfigName(config.getConfigName(), config.getResourcePath())
                + "/" + itemMetadata.getConfigurationMetadata().getName();
            nestedConfigCollection.put("configName", collectionConfigName);
            JSONArray items = new JSONArray();
            for (ConfigurationData configData : configDatas) {
              items.put(toJson(configData));
            }
            nestedConfigCollection.put("items", items);
            prop.put("nestedConfigCollection", nestedConfigCollection);
          }
        }
        else {
          ConfigurationData configData = (ConfigurationData)item.getValue();
          if (configData != null) {
            prop.put("nestedConfig", toJson(configData));
          }
        }
      }

      // property data and metadata
      else {
        prop.putOpt("value", toJsonValue(item.getValue()));
        prop.putOpt("effectiveValue", toJsonValue(item.getEffectiveValue()));
        prop.putOpt("configSourcePath", item.getConfigSourcePath());
        prop.putOpt("default", item.isDefault());
        prop.putOpt("inherited", item.isInherited());
        prop.putOpt("overridden", item.isOverridden());

        if (itemMetadata != null) {
          JSONObject metadata = new JSONObject();
          if (itemMetadata.getType().isArray()) {
            metadata.put("type", ClassUtils.primitiveToWrapper(itemMetadata.getType().getComponentType()).getSimpleName());
            metadata.put("multivalue", true);
          }
          else {
            metadata.put("type", ClassUtils.primitiveToWrapper(itemMetadata.getType()).getSimpleName());
          }
          metadata.putOpt("defaultValue", toJsonValue(itemMetadata.getDefaultValue()));
          metadata.putOpt("label", itemMetadata.getLabel());
          metadata.putOpt("description", itemMetadata.getDescription());
          metadata.putOpt("properties", toJson(itemMetadata.getProperties()));
          prop.put("metadata", metadata);
        }
      }
      props.put(prop);
    }
    result.put("properties", props);

    return result;
  }

  private JSONObject toJson(Map<String, String> properties) throws JSONException {
    if (properties == null || properties.isEmpty()) {
      return null;
    }
    else {
      JSONObject metadataProps = new JSONObject();
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        metadataProps.putOpt(entry.getKey(), entry.getValue());
      }
      return metadataProps;
    }
  }

  private Object toJsonValue(Object value) {
    if (value == null) {
      return null;
    }
    if (value.getClass().isArray()) {
      JSONArray array = new JSONArray();
      for (int i = 0; i < Array.getLength(value); i++) {
        array.put(Array.get(value, i));
      }
      return array;
    }
    else {
      return value;
    }
  }

}
