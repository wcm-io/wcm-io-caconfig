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

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.config.api.Configuration;

/**
 * Find matching configurations for a resource.
 * @deprecated Use Sling Context-Aware Configuration Management API
 */
@Deprecated
@ProviderType
public interface ConfigurationFinder {

  /**
   * Tries to find the closed matching configuration for the given path.
   * Tries to detect the application for the resource using {@link ApplicationFinder} to use the
   * configuration finder strategy of this application.
   * If no application is found, or it does not provide such a strategy, all configuration finder strategies are
   * enquired, iterated in order of service ranking.
   * @param resource Content resource
   * @return Configuration or null if none was found
   */
  Configuration find(Resource resource);

  /**
   * Tries to find the closed matching configuration for the given path.
   * Only the configuration finding strategies of the given application are used.
   * @param resource Content resource
   * @param applicationId Application Id
   * @return Configuration or null if none was found
   */
  Configuration find(Resource resource, String applicationId);

  /**
   * Tries to find all enclosing configurations for the given path.
   * Tries to detect the application for the resource using {@link ApplicationFinder} to use the
   * configuration finder strategy of this application.
   * If no application is found, or it does not provide such a strategy, all configuration finder strategies are
   * enquired, iterated in order of service ranking.
   * @param resource Content resource
   * @return List of configurations that where found in the given path (in order of closest matching first).
   *         If none are found an empty iterator is returned.
   */
  Iterator<Configuration> findAll(Resource resource);

  /**
   * Tries to find all enclosing configurations for the given path.
   * Only the configuration finding strategies of the given application are used.
   * @param resource Content resource
   * @param applicationId Application Id
   * @return List of configurations that where found in the given path (in order of closest matching first).
   *         If none are found an empty iterator is returned.
   */
  Iterator<Configuration> findAll(Resource resource, String applicationId);

}
