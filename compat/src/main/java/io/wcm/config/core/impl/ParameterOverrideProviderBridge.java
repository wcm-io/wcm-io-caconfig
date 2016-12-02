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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationOverrideProvider;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import io.wcm.config.spi.ParameterOverrideProvider;
import io.wcm.sling.commons.osgi.RankedServices;
import io.wcm.sling.commons.osgi.RankedServices.ChangeListener;

/**
 * Bridges parameter override provider to configuration override providers.
 */
@Component(service = ConfigurationOverrideProvider.class, immediate = true)
public class ParameterOverrideProviderBridge implements ConfigurationOverrideProvider, ChangeListener {

  @Reference(service = ParameterOverrideProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
      bind = "bindParameterOverrideProvider", unbind = "unbindParameterOverrideProvider")
  private RankedServices<ParameterOverrideProvider> parameterOverrideProviders = new RankedServices<>(this);

  private volatile List<String> overrideStrings = ImmutableList.of();

  private static final Logger log = LoggerFactory.getLogger(ParameterOverrideProviderBridge.class);

  @Reference
  private ConfigurationManager configManager;

  @Override
  public Collection<String> getOverrideStrings() {
    return overrideStrings;
  }

  void bindParameterOverrideProvider(ParameterOverrideProvider service, Map<String, Object> props) {
    parameterOverrideProviders.bind(service, props);
  }

  void unbindParameterOverrideProvider(ParameterOverrideProvider service, Map<String, Object> props) {
    parameterOverrideProviders.unbind(service, props);
  }

  @Override
  public void changed() {
    Map<String, String> overrideMap = new HashMap<>();
    for (ParameterOverrideProvider provider : parameterOverrideProviders) {
      overrideMap.putAll(provider.getOverrideMap());
    }
    if (overrideMap.isEmpty()) {
      overrideStrings = ImmutableList.of();
      return;
    }

    List<String> overrideStringList = new ArrayList<>();
    ConfigurationMetadata configMetadata = configManager.getConfigurationMetadata(ParameterProviderBridge.DEFAULT_CONFIG_NAME);
    for (Map.Entry<String, String> entry : overrideMap.entrySet()) {
      String item = toCaConfigOverrideString(entry.getKey(), entry.getValue(), configMetadata);
      if (item != null) {
        overrideStringList.add(item);
      }
    }
    overrideStrings = ImmutableList.copyOf(overrideStringList);
  }

  private String toCaConfigOverrideString(String key, String value, ConfigurationMetadata configMetadata) {
    try {
      ParameterOverrideInfo info = new ParameterOverrideInfo(key);
      Class type = String.class;
      if (configMetadata != null) {
        PropertyMetadata<?> propertyMetadata = configMetadata.getPropertyMetadata().get(info.getParameterName());
        if (propertyMetadata != null) {
          type = propertyMetadata.getType();
        }
      }
      return (info.getConfigurationId() != null ? "[" + info.getConfigurationId() + "]" : "")
          + info.getParameterName()
          + "="
          + toJsonValue(value, type);
    }
    catch (IllegalArgumentException ex) {
      log.warn("Ignoring invalid parameter override string ({}): {}={}", ex.getMessage(), key, value);
    }
    return null;
  }

  private String toJsonValue(String value, Class type) {
    if (type.isArray()) {
      return "[" + toJsonValue(value, type.getComponentType()) + "]";
    }
    else if (type == String.class) {
      return JSONObject.quote(value);
    }
    else if (type == int.class) {
      return Integer.toString(NumberUtils.toInt(value));
    }
    else if (type == long.class) {
      return Long.toString(NumberUtils.toLong(value));
    }
    else if (type == double.class) {
      return Double.toString(NumberUtils.toDouble(value));
    }
    else if (type == boolean.class) {
      return Boolean.toString(BooleanUtils.toBoolean(value));
    }
    else {
      throw new IllegalArgumentException("Illegal value type: " + type.getName());
    }
  }

}
