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
import java.util.HashMap;
import java.util.Map;

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
 * Provides parameter override map from OSGi factory configuration.
 */
@Component(immediate = true, metatype = true, configurationFactory = true,
label = "wcm.io Configuration Property Override Provider: OSGi configuration",
description = "Allows to define configuration property default values or overrides from OSGi configuration.")
@Service(ParameterOverrideProvider.class)
@Property(name = "webconsole.configurationFactory.nameHint", value = "{description}, enabled={enabled}")
public final class OsgiConfigOverrideProvider implements ParameterOverrideProvider {

  @Property(label = "Enabled", boolValue = OsgiConfigOverrideProvider.DEFAULT_ENABLED,
      description = "Enable parameter override provider")
  static final String PROPERTY_ENABLED = "enabled";
  static final boolean DEFAULT_ENABLED = false;

  @Property(label = "Service Ranking", intValue = OsgiConfigOverrideProvider.DEFAULT_RANKING,
      description = "Priority of parameter override providers (lower = higher priority)",
      propertyPrivate = false)
  static final String PROPERTY_RANKING = Constants.SERVICE_RANKING;
  static final int DEFAULT_RANKING = 3000;

  @Property(label = "Overrides", cardinality = Integer.MAX_VALUE,
      description = "Key/Value pairs defining parameter overrides.\n"
          + "Syntax: [{scope}]{parameterName}={value}\n"
          + "Examples:\n"
          + "[default]param1 - Override default value for parameter 'param1'\n"
          + "param1 - Override value for parameter 'param1' for all configurations\n"
          + "[/content/region1/site1]param1 - Override value for parameter 'param1' for the "
          + "configurations at /content/region1/site1. This has higher precedence than the other variants.")
  static final String PROPERTY_OVERRIDES = "overrides";
  static final String[] DEFAULT_OVERRIDES = new String[0];

  @Property(label = "Description",
      description = "This description is used for display in the web console.")
  static final String PROPERTY_DESCRIPTION = "description";

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
      Map<String, String> overrides = PropertiesUtil.toMap(config.get(PROPERTY_OVERRIDES), DEFAULT_OVERRIDES);
      if (overrides != null) {
        map.putAll(overrides);
      }
    }
    this.overrideMap = ImmutableMap.copyOf(map);
  }

}
