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

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.wcm.config.api.Configuration;

/**
 * Binds a script variable "config" to the current configuration value map to be used in Sightly.
 */
@Component(immediate = true, service = BindingsValuesProvider.class, property = {
    "javax.script.name=sightly",
    Constants.SERVICE_RANKING + ":Integer=100"
})
@Designate(ocd = ConfigBindingsValueProvider.Config.class)
@SuppressWarnings("null")
public class ConfigBindingsValueProvider implements BindingsValuesProvider {

  /**
   * Name of the variable to which the config value map is bound to in script configuration.
   */
  public static final String BINDING_VARIABLE = "config";

  @ObjectClassDefinition(name = "wcm.io Configuration Compatibility: Bindings Value Provider",
      description = "Binds a script variable \"config\" to the current configuration value map to be used in Sightly.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Enable provider.")
    boolean enabled() default true;

  }

  private boolean enabled;

  @Override
  public void addBindings(Bindings bindings) {
    if (!enabled || !bindings.containsKey(SlingBindings.REQUEST)) {
      return;
    }
    SlingHttpServletRequest request = (SlingHttpServletRequest)bindings.get(SlingBindings.REQUEST);
    Configuration config = request.adaptTo(Configuration.class);
    if (config != null) {
      bindings.put(BINDING_VARIABLE, config);
    }
  }

  @Activate
  void activate(Config config) {
    this.enabled = config.enabled();
  }


}
