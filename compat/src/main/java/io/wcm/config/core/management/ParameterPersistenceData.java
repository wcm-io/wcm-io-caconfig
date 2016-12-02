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

import java.util.Map;
import java.util.SortedSet;

import org.osgi.annotation.versioning.ProviderType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Configuration parameter data that is read/stored by {@link ParameterPersistence} implementations.
 */
@ProviderType
public final class ParameterPersistenceData {

  /**
   * Empty parameter persistence data.
   */
  public static final ParameterPersistenceData EMPTY = new ParameterPersistenceData(
      ImmutableMap.<String, Object>of(), ImmutableSortedSet.<String>of());

  private final Map<String, Object> values;
  private final SortedSet<String> lockedParameterNames;

  /**
   * @param values Map with configured parameter values. Never null.
   * @param lockedParameterNames List of parameter names that are "locked" on this configuration level that means they
   *          are not allowed to be overridden on lower levels by users.
   */
  public ParameterPersistenceData(Map<String, Object> values, SortedSet<String> lockedParameterNames) {
    if (values == null) {
      throw new IllegalArgumentException("values must not be null.");
    }
    if (lockedParameterNames == null) {
      throw new IllegalArgumentException("lockedParameterNames must not be null.");
    }
    this.values = values;
    this.lockedParameterNames = lockedParameterNames;
  }

  /**
   * @return Map with configured parameter values. Never null.
   */
  public Map<String, Object> getValues() {
    return this.values;
  }

  /**
   * @return List of parameter names that are "locked" on this configuration level that means they
   *         are not allowed to be overridden on lower levels by users
   */
  public SortedSet<String> getLockedParameterNames() {
    return this.lockedParameterNames;
  }

}
