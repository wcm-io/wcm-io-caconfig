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
package io.wcm.config.spi;

import java.util.Map;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Provides configuration override values (default or forced).
 */
@ConsumerType
public interface ParameterOverrideProvider {

  /**
   * Returns a map with key value pairs for configuration parameter override.
   * <p>
   * Key:
   * </p>
   * <ul>
   * <li>Syntax: <code>[{scope}[:locked]]{parameterName}</code></li>
   * <li><code>{scope}</code>: if "default", the system default parameter is overriden. Otherwise <code>{scope}</code>
   * may define a configuration id (path), in this case the configuration parameter is overwritten by force for this
   * configuration level. If the [{scope}] part is missing or [locked], the parameter is overridded for all
   * configurations.</li>
   * <li><code>locked</code>: If the scope value is suffixed with the string &quot;:locked&quot; this configuration
   * parameter cannot be overridden in nested configuration scopes.</li>
   * <li><code>{parameterName}</code>: Parameter name (from parameter definitions)</li>
   * </ul>
   * <p>
   * Examples:
   * </p>
   * <ul>
   * <li><code>[default]param1</code> - Override default value for parameter "param1"</li>
   * <li><code>param1</code> - Override value for parameter "param1" for all configurations</li>
   * <li><code>[/content/region1/site1]param1</code> - Override value for parameter "param1" for the configurations at
   * <code>/content/region1/site1</code>. This has higher precedence than the other variants.</li>
   * </ul>
   * <p>
   * Value:
   * </p>
   * <ul>
   * <li>Override value</li>
   * <li>Has to be convertible to the parameter's type</li>
   * </ul>
   * @return Map (never null)
   */
  Map<String, String> getOverrideMap();

}
