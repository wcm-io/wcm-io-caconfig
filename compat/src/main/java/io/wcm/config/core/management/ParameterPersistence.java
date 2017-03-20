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

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Manages reading and storing parameter values for a single configuration (without inheritance).
 * @deprecated Use Sling Context-Aware Configuration Management API
 */
@Deprecated
@ProviderType
public interface ParameterPersistence {

  /**
   * Control property holding String Array with list of locked parameter names.
   */
  String PN_LOCKED_PARAMETER_NAMES = "wcmio:lockedParameterNames";

  /**
   * Get all parameter values stored for a configuration.
   * @param resolver Resource resolver
   * @param configurationId Configuration id
   * @return Parameter data. Never null.
   */
  ParameterPersistenceData getData(ResourceResolver resolver, String configurationId);

  /**
   * Writes parameter values for a configuration.
   * All existing parameter values are erased before writing the new ones.
   * @param resolver Resource resolver
   * @param configurationId Configuration id
   * @param data Parameter data. Never null.
   * @throws PersistenceException Persistence exception is thrown when storing configuration parameters fails.
   */
  void storeData(ResourceResolver resolver, String configurationId, ParameterPersistenceData data)
      throws PersistenceException;

  /**
   * Writes parameter values for a configuration.
   * @param resolver Resource resolver
   * @param configurationId Configuration id
   * @param data Parameter data. Never null.
   * @param mergeWithExisting If true, existing parameter values are only overridden when they are contained in the
   *          set of parameter values. Otherwise all existing parameter values are erased before writing the new ones.
   * @throws PersistenceException Persistence exception is thrown when storing configuration parameters fails.
   */
  void storeData(ResourceResolver resolver, String configurationId, ParameterPersistenceData data,
      boolean mergeWithExisting) throws PersistenceException;

}
