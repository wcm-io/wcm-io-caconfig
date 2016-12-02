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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.config.api.Parameter;

/**
 * Resolve parameter values respecting configuration hierarchy and overriding.
 * @deprecated Use Sling Context-Aware Configuration Management API
 */
@Deprecated
@ProviderType
public interface ParameterResolver {

  /**
   * Get all parameter values effective for a configuration including configuration inheritance.
   * @param resolver Resource resolver
   * @param configurationIds List of configuration ids (in order of closest matching first).
   * @return Parameter values
   */
  Map<String, Object> getEffectiveValues(ResourceResolver resolver, Collection<String> configurationIds);

  /**
   * Get all parameter definitions from all parameter providers.
   * @return Parameter definitions
   */
  Set<Parameter<?>> getAllParameters();

}
