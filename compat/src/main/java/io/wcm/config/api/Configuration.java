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
package io.wcm.config.api;

import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Configuration for a certain content scope.
 * The map backing this configuration is read-only.
 */
@ProviderType
public interface Configuration extends ValueMap {

  /**
   * @return Configuration id. This is the root path of the subtree this configuration belongs to.
   */
  String getConfigurationId();

  /**
   * Get a named property and convert it into the given type.
   * This method does not support conversion into a primitive type or an
   * array of a primitive type. It should return <code>null</code> in this
   * case.
   * @param <T> Parameter type
   * @param parameter Parameter definition
   * @return Return named value converted to type T or <code>null</code> if
   *         non existing or can't be converted.
   */
  <T> T get(Parameter<T> parameter);

  /**
   * Get a named property and convert it into the given type.
   * This method does not support conversion into a primitive type or an
   * array of a primitive type. It should return the default value in this
   * case.
   * @param <T> Parameter type
   * @param parameter Parameter definition
   * @param defaultValue The default value to use if the named property does
   *          not exist or cannot be converted to the requested type. The
   *          default value is also used to define the type to convert the
   *          value to. If this is <code>null</code> any existing property is
   *          not converted.
   * @return Return named value converted to type T or the default value if
   *         non existing or can't be converted.
   */
  <T> T get(Parameter<T> parameter, T defaultValue);

}
