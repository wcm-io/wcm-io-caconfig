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
package io.wcm.config.core.management;

import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

import io.wcm.config.api.Parameter;

/**
 * Override configuration parameter. The implementation can decide at which level the override should take place.
 * @deprecated Use Sling Context-Aware Configuration Management API
 */
@Deprecated
@ProviderType
public interface ParameterOverride {

  /**
   * Scope for system default value
   */
  String DEFAULT_SCOPE = "default";

  /**
   * Suffix to mark a parameter override as locked, the parameter value cannot be overridden in nested configuration
   * scopes.
   */
  String LOCKED_SUFFIX = ":locked";

  /**
   * Allows to override the system default value of a parameter, that is applied if not configuration
   * value is set either in OSGi configuration or in the configuration hierarchy.
   * @param <T> Parameter type
   * @param parameter Parameter definition
   * @return Parameter value (null if no override required, or new value if it was overridden)
   */
  <T> T getOverrideSystemDefault(Parameter<T> parameter);

  /**
   * Allows to override the value of a parameter. This value is applied in the effective configuration
   * regardless of all other configuration sources.
   * @param <T> Parameter type
   * @param configurationId Configuration id
   * @param parameter Parameter definition
   * @return Parameter value (null if no override required, or new value if it was overridden)
   */
  <T> T getOverrideForce(String configurationId, Parameter<T> parameter);

  /**
   * Get parameter names that are locked for the given configuration ID.
   * @param configurationId Configuration id
   * @return Parameter names or empty set.
   */
  Set<String> getLockedParameterNames(String configurationId);

}
