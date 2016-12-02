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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.osgi.framework.Constants;

import io.wcm.config.api.Configuration;

/**
 * Binds a script variable "config" to the current configuration value map to be used in Sightly.
 */
@Component(immediate = true)
@Service(BindingsValuesProvider.class)
@Properties({
  @Property(name = "javax.script.name", value = "sightly"),
  @Property(name = Constants.SERVICE_RANKING, intValue = 100)
})
public class ConfigBindingsValueProvider implements BindingsValuesProvider {

  /**
   * Name of the variable to which the config value map is bound to in script configuration.
   */
  public static final String BINDING_VARIABLE = "config";

  @Override
  public void addBindings(Bindings bindings) {
    if (!bindings.containsKey(SlingBindings.REQUEST)) {
      return;
    }
    SlingHttpServletRequest request = (SlingHttpServletRequest)bindings.get(SlingBindings.REQUEST);
    Configuration config = request.adaptTo(Configuration.class);
    if (config != null) {
      bindings.put(BINDING_VARIABLE, config);
    }
  }

}
