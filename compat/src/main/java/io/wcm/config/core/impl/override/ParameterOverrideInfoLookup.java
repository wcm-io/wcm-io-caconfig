/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.config.core.impl.override;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * Contains all parameter overrides informations from all parameter override providers.
 */
public class ParameterOverrideInfoLookup {

  private final Map<String, String> overrideSystemDefaultMap = new HashMap<>();
  private final Map<String, String> overrideForceMap = new HashMap<>();
  private final Map<String, Map<String, String>> overrideForceScopeMap = new HashMap<>();
  private Set<String> lockedParameterNamesSet = new HashSet<>();
  private Map<String, Set<String>> lockedParameterNamesScopeMap = new HashMap<>();
  private boolean sealed;

  private final Logger log = LoggerFactory.getLogger(ParameterOverrideInfoLookup.class);

  /**
   * Adds map containing parameter override definitions.
   * Can be called multiple times. New calls do not override settings from previous calls, only add new settings.
   * Thus maps with highest priority should be added first.
   * @param overrideMap Override map
   */
  public void addOverrideMap(Map<String, String> overrideMap) {
    if (sealed) {
      throw new IllegalStateException("Instance is sealed.");
    }
    for (Map.Entry<String, String> entry : overrideMap.entrySet()) {
      try {
        ParameterOverrideInfo info = new ParameterOverrideInfo(entry.getKey());
        if (info.isOverrideSystemDefault()) {
          putMapIfNotExsits(overrideSystemDefaultMap, info.getParameterName(), entry.getValue());
        }
        else if (StringUtils.isNotEmpty(info.getConfigurationId()))  {
          Map<String, String> overrideForceScopeMapEntry = overrideForceScopeMap.get(info.getConfigurationId());
          if (overrideForceScopeMapEntry == null) {
            overrideForceScopeMapEntry = new HashMap<>();
            overrideForceScopeMap.put(info.getConfigurationId(), overrideForceScopeMapEntry);
          }
          putMapIfNotExsits(overrideForceScopeMapEntry, info.getParameterName(), entry.getValue());
          if (info.isLocked()) {
            Set<String> lockedParameterNamesScopeMapEntry = lockedParameterNamesScopeMap.get(info.getConfigurationId());
            if (lockedParameterNamesScopeMapEntry == null) {
              lockedParameterNamesScopeMapEntry = new HashSet<>();
              lockedParameterNamesScopeMap.put(info.getConfigurationId(), lockedParameterNamesScopeMapEntry);
            }
            putSetIfNotExsits(lockedParameterNamesScopeMapEntry, info.getParameterName());
          }
        }
        else {
          putMapIfNotExsits(overrideForceMap, info.getParameterName(), entry.getValue());
          if (info.isLocked()) {
            putSetIfNotExsits(lockedParameterNamesSet, info.getParameterName());
          }
        }
      }
      catch (IllegalArgumentException ex) {
        log.warn("Ignoring invalid parameter override definition:\n" + ex.getMessage());
      }
    }
  }

  /**
   * Make all maps and sets immutable.
   */
  public void seal() {
    lockedParameterNamesSet = ImmutableSet.copyOf(lockedParameterNamesSet);
    lockedParameterNamesScopeMap = ImmutableMap.copyOf(Maps.transformValues(lockedParameterNamesScopeMap, new Function<Set<String>, Set<String>>() {
      @Override
      public Set<String> apply(Set<String> input) {
        return ImmutableSet.copyOf(input);
      }
    }));
    sealed = true;
  }

  private void putMapIfNotExsits(Map<String, String> map, String key, String value) {
    if (!map.containsKey(key)) {
      map.put(key, value);
    }
  }

  private void putSetIfNotExsits(Set<String> set, String key) {
    if (!set.contains(key)) {
      set.add(key);
    }
  }

  /**
   * Lookup system default override.
   * @param parameterName Parameter name
   * @return Override value or null
   */
  public String getOverrideSystemDefault(String parameterName) {
    return overrideSystemDefaultMap.get(parameterName);
  }

  /**
   * Lookup force override without specific configuration Id.
   * @param parameterName Parameter name
   * @return Override value or null
   */
  public String getOverrideForce(String parameterName) {
    return overrideForceMap.get(parameterName);
  }

  /**
   * Lookup force override for given configuration Id.
   * @param parameterName Parameter name
   * @return Override value or null
   */
  public String getOverrideForce(String configurationId, String parameterName) {
    Map<String, String> overrideForceScopeMapEntry = overrideForceScopeMap.get(configurationId);
    if (overrideForceScopeMapEntry != null) {
      return overrideForceScopeMapEntry.get(parameterName);
    }
    return null;
  }

  /**
   * Get locked parameter names without specifc configuration Id.
   * @return Parameter names
   */
  public Set<String> getLockedParameterNames() {
    return lockedParameterNamesSet;
  }

  /**
   * Get locked parameter names for specific configuration Id.
   * @param configurationId Configuration Id
   * @return Parameter names
   */
  public Set<String> getLockedParameterNames(String configurationId) {
    Set<String> lockedParameterNamesScopeMapEntry = lockedParameterNamesScopeMap.get(configurationId);
    if (lockedParameterNamesScopeMapEntry != null) {
      return lockedParameterNamesScopeMapEntry;
    }
    else {
      return ImmutableSet.of();
    }
  }

}
