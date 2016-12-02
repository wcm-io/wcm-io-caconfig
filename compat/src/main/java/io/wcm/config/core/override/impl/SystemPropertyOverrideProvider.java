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
package io.wcm.config.core.override.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.google.common.collect.ImmutableMap;

import io.wcm.config.spi.ParameterOverrideProvider;

/**
 * Provides parameter override map from system properties.
 */
@Component(immediate = true, service = ParameterOverrideProvider.class)
@Designate(ocd = SystemPropertyOverrideProvider.Config.class)
public final class SystemPropertyOverrideProvider implements ParameterOverrideProvider {

  /**
   * Prefix for override system property
   */
  public static final String SYSTEM_PROPERTY_PREFIX = "config.override.";

  @ObjectClassDefinition(name = "wcm.io Configuration Property Override Provider: System Properties", description = "Allows to define "
      + "configuration property default values or overrides from system environment properties.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Enable parameter override provider.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Service Ranking", description = "Priority of parameter override providers (lower = higher priority).")
    int service_ranking() default 2000;

  }

  private Map<String, String> overrideMap;

  @Override
  public Map<String, String> getOverrideMap() {
    return overrideMap;
  }

  @Activate
  void activate(Config config) {
    final boolean enabled = config.enabled();

    Map<String, String> map = new HashMap<>();
    if (enabled) {
      Properties properties = System.getProperties();
      Enumeration<Object> keys = properties.keys();
      while (keys.hasMoreElements()) {
        Object keyObject = keys.nextElement();
        if (keyObject instanceof String) {
          String key = (String)keyObject;
          if (StringUtils.startsWith(key, SYSTEM_PROPERTY_PREFIX)) {
            map.put(StringUtils.substringAfter(key, SYSTEM_PROPERTY_PREFIX), System.getProperty(key));
          }
        }
      }
    }
    this.overrideMap = ImmutableMap.copyOf(map);
  }

}
