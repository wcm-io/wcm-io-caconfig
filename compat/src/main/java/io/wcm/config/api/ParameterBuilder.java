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
package io.wcm.config.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.osgi.annotation.versioning.ProviderType;

import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.wcm.caconfig.application.spi.ApplicationProvider;

/**
 * Fluent API for building configuration parameter definitions.
 * @param <T> Parameter value type
 */
@ProviderType
public final class ParameterBuilder<T> {

  private static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.]+$");
  private static final Pattern OSGI_CONFIG_PROPERTY_PATTERN =
      Pattern.compile("^[a-zA-Z0-9\\-\\_\\.\\$]+\\:[a-zA-Z0-9\\-\\_\\.]+$");

  private static final Set<Class<?>> SUPPORTED_TYPES = ImmutableSet.<Class<?>>builder()
      .add(String.class)
      .add(String[].class)
      .add(Integer.class)
      .add(Long.class)
      .add(Double.class)
      .add(Boolean.class)
      .add(Map.class)
      .build();

  private String name;
  private Class<T> type;
  private String applicationId;
  private String defaultOsgiConfigProperty;
  private T defaultValue;
  private final Map<String, Object> properties = new HashMap<>();

  private ParameterBuilder() {
    // private constructor
  }

  /**
   * Create a new parameter builder.
   * @param <T> Parameter type
   * @return Parameter builder
   */
  public static <T> ParameterBuilder<T> create() {
    return new ParameterBuilder<T>();
  }

  /**
   * Create a new parameter builder.
   * @param <T> Parameter type
   * @param name Parameter name. Only characters, numbers, hyphen, underline and point are allowed.
   * @return Parameter builder
   */
  public static <T> ParameterBuilder<T> create(String name) {
    return new ParameterBuilder<T>()
        .name(name);
  }

  /**
   * Create a new parameter builder.
   * @param <T> Parameter type
   * @param name Parameter name. Only characters, numbers, hyphen, underline and point are allowed.
   * @param type Parameter value type
   * @return Parameter builder
   */
  public static <T> ParameterBuilder<T> create(String name, Class<T> type) {
    return new ParameterBuilder<T>()
        .name(name)
        .type(type);
  }

  /**
   * Create a new parameter builder.
   * @param <T> Parameter type
   * @param name Parameter name. Only characters, numbers, hyphen, underline and point are allowed.
   * @param type Parameter Value type.
   * @param applicationId Application Id. Has to be a conent path starting with "/".
   * @return Parameter builder
   */
  public static <T> ParameterBuilder<T> create(String name, Class<T> type, String applicationId) {
    return new ParameterBuilder<T>()
        .name(name)
        .type(type)
        .applicationId(applicationId);
  }

  /**
   * @param value Parameter name. Only characters, numbers, hyphen, underline and point are allowed.
   * @return this
   */
  public ParameterBuilder<T> name(String value) {
    if (value == null || !PARAMETER_NAME_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid name: " + value);
    }
    this.name = value;
    return this;
  }

  /**
   * @param value Value type.
   * @return this
   */
  public ParameterBuilder<T> type(Class<T> value) {
    if (value == null || !SUPPORTED_TYPES.contains(value)) {
      throw new IllegalArgumentException("Invalid type: " + value);
    }
    this.type = value;
    return this;
  }

  /**
   * @param value Application Id. Has to be a conent path starting with "/".
   * @return this
   */
  public ParameterBuilder<T> applicationId(String value) {
    if (value == null || !ApplicationProvider.APPLICATION_ID_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid applicaiton id: " + value);
    }
    this.applicationId = value;
    return this;
  }

  /**
   * References OSGi configuration property which is checked for default value if this parameter is not set
   * in any configuration.
   * @param value OSGi configuration parameter name with syntax {serviceClassName}:{propertyName}
   * @return this
   */
  public ParameterBuilder<T> defaultOsgiConfigProperty(String value) {
    if (value == null || !OSGI_CONFIG_PROPERTY_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid value: " + value);
    }
    this.defaultOsgiConfigProperty = value;
    return this;
  }

  /**
   * @param value Default value if parameter is not set for configuration
   *          and no default value is defined in OSGi configuration
   * @return this
   */
  public ParameterBuilder<T> defaultValue(T value) {
    this.defaultValue = value;
    return this;
  }

  /**
   * Further properties for documentation and configuration of behavior in configuration editor.
   * @param map Property map. Is merged with properties already set in builder.
   * @return this
   */
  public ParameterBuilder<T> properties(Map<String, Object> map) {
    if (map == null) {
      throw new IllegalArgumentException("Map argument must not be null.");
    }
    this.properties.putAll(map);
    return this;
  }

  /**
   * Further property for documentation and configuration of behavior in configuration editor.
   * @param key Property key
   * @param value Property value
   * @return this
   */
  public ParameterBuilder<T> property(String key, Object value) {
    if (key == null) {
      throw new IllegalArgumentException("Key argument must not be null.");
    }
    this.properties.put(key, value);
    return this;
  }

  /**
   * Builds the parameter definition.
   * @return Parameter definition
   */
  public Parameter<T> build() {
    if (this.name == null) {
      throw new IllegalArgumentException("Name is missing.");
    }
    if (this.type == null) {
      throw new IllegalArgumentException("Type is missing.");
    }
    return new Parameter<T>(
        this.name,
        this.type,
        this.applicationId,
        this.defaultOsgiConfigProperty,
        this.defaultValue,
        new ValueMapDecorator(ImmutableMap.<String, Object>copyOf(this.properties)));
  }

}
