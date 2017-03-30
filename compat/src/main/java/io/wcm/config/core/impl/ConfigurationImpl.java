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
package io.wcm.config.core.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.resource.ValueMap;

import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.google.common.collect.ImmutableMap;

import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;

/**
 * Default implementation of {@link Configuration}.
 */
public final class ConfigurationImpl implements Configuration {

  private final String configurationId;
  private final ValueMap properties;

  /**
   * @param configurationId Configuration Id
   * @param properties Effective properties
   */
  public ConfigurationImpl(String configurationId, Map<String, Object> properties) {
    this.configurationId = configurationId;
    this.properties = new ValueMapDecorator(ImmutableMap.<String, Object>copyOf(properties));
  }

  @Override
  public String getConfigurationId() {
    return configurationId;
  }

  @Override
  public <T> T get(Parameter<T> parameter) {
    return this.properties.get(parameter.getName(), parameter.getType());
  }

  @Override
  public <T> T get(Parameter<T> parameter, T defaultValue) {
    return this.properties.get(parameter.getName(), defaultValue);
  }

  // -- delegate methods for ValueMap --

  @Override
  public <T> T get(String name, Class<T> type) {
    return this.properties.get(name, type);
  }

  @Override
  public <T> T get(String name, T defaultValue) {
    return this.properties.get(name, defaultValue);
  }

  @Override
  public int size() {
    return this.properties.size();
  }

  @Override
  public boolean isEmpty() {
    return this.properties.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return this.properties.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return this.properties.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return this.properties.get(key);
  }

  @Override
  public Object put(String key, Object value) {
    return this.properties.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return this.properties.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    this.properties.putAll(m);
  }

  @Override
  public void clear() {
    this.properties.clear();
  }

  @Override
  public Set<String> keySet() {
    return this.properties.keySet();
  }

  @Override
  public Collection<Object> values() {
    return this.properties.values();
  }

  @Override
  public Set<Map.Entry<String, Object>> entrySet() {
    return this.properties.entrySet();
  }

  @Override
  public String toString() {
    return this.configurationId;
  }

}
