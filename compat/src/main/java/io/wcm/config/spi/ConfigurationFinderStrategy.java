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

import java.util.Iterator;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Allows application to define a strategy to find configurations for content paths.
 */
@ConsumerType
public interface ConfigurationFinderStrategy {

  /**
   * @return Application Id
   */
  String getApplicationId();

  /**
   * Finds configuration ids for the given path.
   * @param resource Content resource
   * @return Configuration ids that where detected in the given path (in order of closest matching first).
   *         If none are found an empty itrator is returned.
   */
  Iterator<String> findConfigurationIds(Resource resource);

}
