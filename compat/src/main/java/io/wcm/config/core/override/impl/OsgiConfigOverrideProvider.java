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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.google.common.collect.ImmutableMap;

import io.wcm.config.spi.ParameterOverrideProvider;

/**
 * Provides parameter override map from OSGi factory configuration.
 */
@Component(immediate = true, service = ParameterOverrideProvider.class)
@Designate(ocd = OsgiConfigOverrideProvider.Config.class, factory = true)
public final class OsgiConfigOverrideProvider implements ParameterOverrideProvider {

  @ObjectClassDefinition(name = "wcm.io Configuration Compatibility: Property Override Provider - OSGi configuration",
      description = "Allows to define configuration property default values or overrides from OSGi configuration.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Enable parameter override provider.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Service Ranking", description = "Priority of parameter override providers (lower = higher priority).")
    int service_ranking() default 3000;

    @AttributeDefinition(name = "Overrides",
        description = "Key/Value pairs defining parameter overrides.\n"
            + "Syntax: [{scope}]{parameterName}={value}\n"
            + "Examples:\n"
            + "[default]param1 - Override default value for parameter 'param1'\n"
            + "param1 - Override value for parameter 'param1' for all configurations\n"
            + "[/content/region1/site1]param1 - Override value for parameter 'param1' for the "
            + "configurations at /content/region1/site1. This has higher precedence than the other variants.", cardinality = Integer.MAX_VALUE)
    String[] overrides();

    @AttributeDefinition(name = "Description", description = "This description is used for display in the web console.")
    String description();

    String webconsole_configurationFactory_nameHint() default "{description}, enabled={enabled}";

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
      Map<String, String> overrides = PropertiesUtil.toMap(config.overrides(), ArrayUtils.EMPTY_STRING_ARRAY);
      if (overrides != null) {
        map.putAll(overrides);
      }
    }
    this.overrideMap = ImmutableMap.copyOf(map);
  }

}
