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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.ImmutableMap;

import io.wcm.config.spi.ParameterOverrideProvider;

/**
 * Provides parameter override map from system properties.
 */
@Component(immediate = true, metatype = true,
label = "wcm.io Configuration Property Override Provider: System Properties",
description = "Allows to define configuration property default values or overrides from system environment properties.")
@Service(ParameterOverrideProvider.class)
public final class SystemPropertyOverrideProvider implements ParameterOverrideProvider {

  /**
   * Prefix for override system property
   */
  public static final String SYSTEM_PROPERTY_PREFIX = "config.override.";

  @Property(label = "Enabled", boolValue = SystemPropertyOverrideProvider.DEFAULT_ENABLED,
      description = "Enable parameter override provider")
  static final String PROPERTY_ENABLED = "enabled";
  static final boolean DEFAULT_ENABLED = false;

  @Property(label = "Service Ranking", intValue = SystemPropertyOverrideProvider.DEFAULT_RANKING,
      description = "Priority of parameter override providers (lower = higher priority)",
      propertyPrivate = false)
  static final String PROPERTY_RANKING = Constants.SERVICE_RANKING;
  static final int DEFAULT_RANKING = 2000;

  private Map<String, String> overrideMap;

  @Override
  public Map<String, String> getOverrideMap() {
    return overrideMap;
  }

  @Activate
  void activate(final ComponentContext ctx) {
    Dictionary config = ctx.getProperties();
    final boolean enabled = PropertiesUtil.toBoolean(config.get(PROPERTY_ENABLED), DEFAULT_ENABLED);

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
