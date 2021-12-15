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
import static io.wcm.caconfig.editor.impl.NameConstants.RP_COLLECTION;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceAccessDeniedException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Persist configuration data.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.resourceTypes=/apps/wcm-io/caconfig/editor/components/page/editor",
    "sling.servlet.extensions=json",
    "sling.servlet.selectors=" + ConfigPersistServlet.SELECTOR,
    "sling.servlet.methods=POST",
    "sling.servlet.methods=DELETE"
})
public class ConfigPersistServlet extends SlingAllMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configPersist";

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private EditorConfig editorConfig;

  private static Logger log = LoggerFactory.getLogger(ConfigPersistServlet.class);

  @Override
  @SuppressWarnings({ "null", "PMD.GuardLogStatement" })
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    if (!editorConfig.isEnabled()) {
      sendForbiddenWithMessage(response, "Configuration editor is disabled.");
      return;
    }

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
    ConfigurationPersistData persistData = null;
    ConfigurationCollectionPersistData collectionPersistData = null;
    try {
      String jsonDataString = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
      JsonNode jsonData = OBJECT_MAPPER.readTree(jsonDataString);
      if (collection) {
        collectionPersistData = parseCollectionConfigData(jsonData, configMetadata);
      }
      else {
        persistData = parseConfigData(jsonData, configMetadata);
      }
    }
    catch (IOException ex) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data: " + ex.getMessage());
      return;
    }

    // persist data
    try {
      if (collection) {
        configManager.persistConfigurationCollection(request.getResource(), configName, collectionPersistData);
      }
      else {
        configManager.persistConfiguration(request.getResource(), configName, persistData);
      }
    }
    catch (ConfigurationPersistenceAccessDeniedException ex) {
      sendForbiddenWithMessage(response, ex.getMessage());
    }
    catch (ConfigurationPersistenceException ex) {
      log.warn("Unable to persist data for " + configName + (collection ? "[col]" : ""), ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to persist data: " + ex.getMessage());
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      log.error("Error getting configuration for " + configName + (collection ? "[col]" : ""), ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  private ConfigurationCollectionPersistData parseCollectionConfigData(JsonNode jsonData, ConfigurationMetadata configMetadata) {
    List<ConfigurationPersistData> items = new ArrayList<>();
    ArrayNode itemsObject = (ArrayNode)jsonData.get("items");
    for (int i = 0; i < itemsObject.size(); i++) {
      JsonNode item = itemsObject.get(i);
      items.add(parseConfigData(item, configMetadata));
    }

    Map<String, Object> properties = null;
    JsonNode propertiesObject = jsonData.get("properties");
    if (propertiesObject != null) {
      properties = new HashMap<>();
      Iterator<String> propertyNames = propertiesObject.fieldNames();
      while (propertyNames.hasNext()) {
        String propertyName = propertyNames.next();
        properties.put(propertyName, toSingle(propertiesObject.get(propertyName)));
      }
    }

    return new ConfigurationCollectionPersistData(items)
        .properties(properties);
  }

  private ConfigurationPersistData parseConfigData(JsonNode item, ConfigurationMetadata configMetadata) {
    Map<String, Object> props = new HashMap<>();
    JsonNode properties = item.get("properties");
    Iterator<String> propertyNames = properties.fieldNames();
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
      if (propertyType == ConfigurationMetadata.class || !properties.hasNonNull(propertyName)) {
        // skip nested configuration and empty properties
        continue;
      }
      else if (propertyType == null) {
        JsonNode value = properties.get(propertyName);
        if (value.isArray()) {
          ArrayNode arrayValue = (ArrayNode)value;
          if (arrayValue.size() == 0) {
            props.put(propertyName, ArrayUtils.EMPTY_STRING_ARRAY);
          }
          JsonNode firstValue = arrayValue.get(0);
          if (firstValue.canConvertToInt()) {
            props.put(propertyName, toArray(arrayValue, int.class));
          }
          else if (firstValue.canConvertToLong()) {
            props.put(propertyName, toArray(arrayValue, long.class));
          }
          else if (firstValue.isDouble()) {
            props.put(propertyName, toArray(arrayValue, double.class));
          }
          else if (firstValue.isBoolean()) {
            props.put(propertyName, toArray(arrayValue, boolean.class));
          }
          else if (firstValue.isTextual()) {
            props.put(propertyName, toArray(arrayValue, String.class));
          }
        }
        else {
          props.put(propertyName, toSingle(value));
        }
      }
      else if (isArray) {
        JsonNode value = properties.get(propertyName);
        if (value.isArray()) {
          props.put(propertyName, toArray((ArrayNode)value, propertyType));
        }
      }
      else {
        JsonNode value = properties.get(propertyName);
        props.put(propertyName, toSingle(value, propertyType));
      }
    }

    String collectionItemName = null;
    if (configMetadata != null && configMetadata.isCollection()) {
      collectionItemName = item.get("collectionItemName").textValue();
    }

    return new ConfigurationPersistData(props)
        .collectionItemName(collectionItemName);
  }

  private Object toSingle(@NotNull JsonNode value, @NotNull Class propertyType) {
    if (propertyType.equals(String.class)) {
      return toString(value);
    }
    else if (propertyType.equals(int.class)) {
      return toInt(value);
    }
    else if (propertyType.equals(long.class)) {
      return toLong(value);
    }
    else if (propertyType.equals(double.class)) {
      return toDouble(value);
    }
    else if (propertyType.equals(boolean.class)) {
      return toBoolean(value);
    }
    else {
      throw new IllegalArgumentException("Unexpected type: " + propertyType.getName());
    }
  }

  private @Nullable Object toSingle(@NotNull JsonNode value) {
    if (value.isTextual()) {
      return value.asText();
    }
    if (value.canConvertToInt()) {
      return value.asInt();
    }
    if (value.canConvertToLong()) {
      return value.asLong();
    }
    if (value.isDouble()) {
      return value.doubleValue();
    }
    if (value.isBoolean()) {
      return value.booleanValue();
    }
    throw new IllegalArgumentException("Unexpected type: " + value);
  }

  private @NotNull Object toArray(@NotNull ArrayNode array, @NotNull Class propertyType) {
    if (propertyType.equals(String.class)) {
      String[] values = new String[array.size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = toString(array.get(i));
      }
      return values;
    }
    else if (propertyType.equals(int.class)) {
      int[] values = new int[array.size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = toInt(array.get(i));
      }
      return values;
    }
    else if (propertyType.equals(long.class)) {
      long[] values = new long[array.size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = toLong(array.get(i));
      }
      return values;
    }
    else if (propertyType.equals(double.class)) {
      double[] values = new double[array.size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = toDouble(array.get(i));
      }
      return values;
    }
    else if (propertyType.equals(boolean.class)) {
      boolean[] values = new boolean[array.size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = toBoolean(array.get(i));
      }
      return values;
    }
    else {
      throw new IllegalArgumentException("Unexpected type: " + propertyType.getName());
    }
  }

  private @Nullable String toString(@NotNull JsonNode value) {
    return value.textValue();
  }

  private int toInt(@NotNull JsonNode value) {
    if (value.isTextual()) {
      return Integer.parseInt(value.textValue());
    }
    return value.intValue();
  }

  private long toLong(@NotNull JsonNode value) {
    if (value.isTextual()) {
      return Long.parseLong(value.textValue());
    }
    return value.longValue();
  }

  private double toDouble(@NotNull JsonNode value) {
    if (value.isTextual()) {
      return Double.parseDouble(value.textValue());
    }
    return value.doubleValue();
  }

  private boolean toBoolean(@NotNull JsonNode value) {
    if (value.isTextual()) {
      return Boolean.parseBoolean(value.textValue());
    }
    return value.booleanValue();
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void doDelete(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {

    // get parameters
    String configName = request.getParameter(RP_CONFIGNAME);
    if (StringUtils.isBlank(configName)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // delete data
    try {
      configManager.deleteConfiguration(request.getResource(), configName);
    }
    catch (ConfigurationPersistenceAccessDeniedException ex) {
      sendForbiddenWithMessage(response, ex.getMessage());
    }
    catch (ConfigurationPersistenceException ex) {
      log.warn("Unable to delete data for " + configName, ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to delete data: " + ex.getMessage());
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      log.error("Error deleting configuration for " + configName, ex);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  private void sendForbiddenWithMessage(SlingHttpServletResponse response, String message) throws IOException {
    response.setContentType("text/plain;charset=" + StandardCharsets.UTF_8.name());
    response.getWriter().write(message);
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
  }

}
