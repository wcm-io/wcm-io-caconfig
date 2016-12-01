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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Persist configuration data.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.resourceTypes=/apps/wcm-io/caconfig/editor/components/page/editor",
    "sling.servlet.extensions=json",
    "sling.servlet.selectors=" + ConfigPersistServlet.SELECTOR,
    "sling.servlet.methods=POST"
})
public class ConfigPersistServlet extends SlingAllMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configPersist";

  @Reference
  private ConfigurationManager configManager;

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

    // get parameters
    String configName = request.getParameter(RP_CONFIGNAME);
    if (StringUtils.isBlank(configName)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    boolean collection = BooleanUtils.toBoolean(request.getParameter(RP_COLLECTION));

    ConfigurationMetadata configMetadata = configManager.getConfigurationMetadata(configName);
    if (configMetadata != null && configMetadata.isCollection() != collection) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Collection parameter mismatch.");
      return;
    }

    // parse JSON parameter data
    List<ConfigurationPersistData> persistData;
    try {
      String jsonDataString = IOUtils.toString(request.getInputStream(), CharEncoding.UTF_8);
      JSONArray jsonData = new JSONArray(jsonDataString);
      persistData = parseJson(jsonData, configMetadata);
    }
    catch (JSONException ex) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data: " + ex.getMessage());
      return;
    }
    if (!collection && persistData.size() != 1) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Exactly 1 JSON object expected.");
      return;
    }

    // persist data
    try {
      if (collection) {
        configManager.persistConfigurationCollection(request.getResource(), configName,
            new ConfigurationCollectionPersistData(persistData));
      }
      else {
        configManager.persistConfiguration(request.getResource(), configName, persistData.get(0));
      }
    }
    catch (ConfigurationPersistenceException ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to persist data: " + ex.getMessage());
    }
  }

  private List<ConfigurationPersistData> parseJson(JSONArray jsonData, ConfigurationMetadata configMetadata) throws JSONException {
    List<ConfigurationPersistData> persistData = new ArrayList<>();
    for (int i = 0; i < jsonData.length(); i++) {
      JSONObject item = jsonData.getJSONObject(i);
      persistData.add(parseJson(item, configMetadata));
    }
    return persistData;
  }

  private ConfigurationPersistData parseJson(JSONObject item, ConfigurationMetadata configMetadata) throws JSONException {
    Map<String, Object> props = new HashMap<>();
    JSONObject properties = item.getJSONObject("properties");
    Iterator<String> propertyNames = properties.keys();
    while (propertyNames.hasNext()) {
      String propertyName = propertyNames.next();
      Class propertyType = null;
      boolean isArray = false;
      if (configMetadata != null) {
        PropertyMetadata<?> propertyMetadata = configMetadata.getPropertyMetadata().get(propertyName);
        if (propertyMetadata != null) {
          isArray = propertyMetadata.getType().isArray();
          if (isArray) {
            propertyType = propertyMetadata.getType().getComponentType();
          }
          else {
            propertyType = propertyMetadata.getType();
          }
        }
      }
      if (propertyType == ConfigurationMetadata.class || properties.isNull(propertyName)) {
        // skip nested configuration and empty properties
        continue;
      }
      else if (propertyType == null) {
        Object value = properties.get(propertyName);
        if (value instanceof JSONArray) {
          JSONArray values = (JSONArray)value;
          if (values.length() == 0) {
            props.put(propertyName, ArrayUtils.EMPTY_STRING_ARRAY);
          }
          else {
            propertyType = String.class;
            if (values.get(0) instanceof Integer) {
              props.put(propertyName, toArray(properties, propertyName, int.class));
            }
            else if (values.get(0) instanceof Long) {
              props.put(propertyName, toArray(properties, propertyName, long.class));
            }
            else if (values.get(0) instanceof Double) {
              props.put(propertyName, toArray(properties, propertyName, double.class));
            }
            else if (values.get(0) instanceof Boolean) {
              props.put(propertyName, toArray(properties, propertyName, boolean.class));
            }
            else {
              props.put(propertyName, toArray(properties, propertyName, String.class));
            }
          }
        }
        else {
          props.put(propertyName, value);
        }
      }
      else if (isArray) {
        props.put(propertyName, toArray(properties, propertyName, propertyType));
      }
      else {
        props.put(propertyName, toSingle(properties, propertyName, propertyType));
      }
    }

    String collectionItemName = null;
    if (configMetadata != null && configMetadata.isCollection()) {
      collectionItemName = item.getString("collectionItemName");
    }

    return new ConfigurationPersistData(props)
        .collectionItemName(collectionItemName);
  }

  private Object toSingle(JSONObject properties, String propertyName, Class propertyType) throws JSONException {
    if (propertyType.equals(String.class)) {
      return properties.getString(propertyName);
    }
    else if (propertyType.equals(int.class)) {
      return properties.getInt(propertyName);
    }
    else if (propertyType.equals(long.class)) {
      return properties.getLong(propertyName);
    }
    else if (propertyType.equals(double.class)) {
      return properties.getDouble(propertyName);
    }
    else if (propertyType.equals(boolean.class)) {
      return properties.getBoolean(propertyName);
    }
    else {
      throw new IllegalArgumentException("Unexpected type: " + propertyType.getName());
    }
  }

  private Object toArray(JSONObject properties, String propertyName, Class propertyType) throws JSONException {
    JSONArray array = properties.getJSONArray(propertyName);
    if (propertyType.equals(String.class)) {
      String[] values = new String[array.length()];
      for (int i = 0; i < values.length; i++) {
        values[i] = array.getString(i);
      }
      return values;
    }
    else if (propertyType.equals(int.class)) {
      int[] values = new int[array.length()];
      for (int i = 0; i < values.length; i++) {
        values[i] = array.getInt(i);
      }
      return values;
    }
    else if (propertyType.equals(long.class)) {
      long[] values = new long[array.length()];
      for (int i = 0; i < values.length; i++) {
        values[i] = array.getLong(i);
      }
      return values;
    }
    else if (propertyType.equals(double.class)) {
      double[] values = new double[array.length()];
      for (int i = 0; i < values.length; i++) {
        values[i] = array.getDouble(i);
      }
      return values;
    }
    else if (propertyType.equals(boolean.class)) {
      boolean[] values = new boolean[array.length()];
      for (int i = 0; i < values.length; i++) {
        values[i] = array.getBoolean(i);
      }
      return values;
    }
    else {
      throw new IllegalArgumentException("Unexpected type: " + propertyType.getName());
    }
  }

}
