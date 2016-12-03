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

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Provides parameter persistence implementations.
 * @deprecated Please use {@link org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy}
 */
@Deprecated
@ConsumerType
public interface ParameterPersistenceProvider {

  /**
   * Get all parameter values stored for a configuration.
   * @param resolver Resource resolver
   * @param configurationId Configuration id
   * @return Set of parameter values. Returns null if no parameters stored for this configuration, allowing other
   *         parameter persistence providers to step in.
   */
  Map<String, Object> get(ResourceResolver resolver, String configurationId);

  /**
   * Writes parameter values for a configuration.
   * All existing parameter values are erased before writing the new ones.
   * @param resolver Resource resolver
   * @param configurationId Configuration id
   * @param values Parameter values. Map may include "control" properties within the "wcm-io-config:" namespace.
   * @return true if parameters are accepted. false if this provider does not accept storing the parameters
   *         and the next provider should be asked to store them.
   * @throws PersistenceException Persistence exception is thrown when storing configuration parameters fails.
   */
  boolean store(ResourceResolver resolver, String configurationId, Map<String, Object> values)
      throws PersistenceException;

}
