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
package io.wcm.config.core.management.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;

import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.core.management.ParameterOverride;
import io.wcm.config.core.management.impl.override.ParameterOverrideInfoLookup;
import io.wcm.config.core.management.util.TypeConversion;
import io.wcm.config.spi.ParameterOverrideProvider;
import io.wcm.sling.commons.osgi.RankedServices;

/**
 * Default implementation of {@link ParameterOverride}.
 */
@Component(immediate = true, metatype = false)
@Service(ParameterOverride.class)
public final class ParameterOverrideImpl implements ParameterOverride {

  /**
   * Parameter override providers implemented by installed applications.
   */
  @Reference(name = "parameterOverrideProvider", referenceInterface = ParameterOverrideProvider.class,
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final RankedServices<ParameterOverrideProvider> parameterOverrideProviders = new RankedServices<>();

  private volatile ParameterOverrideInfoLookup lookup = new ParameterOverrideInfoLookup();

  @Override
  public <T> T getOverrideSystemDefault(Parameter<T> parameter) {
    String value = lookup.getOverrideSystemDefault(parameter.getName());
    return TypeConversion.stringToObject(value, parameter.getType());
  }

  @Override
  public <T> T getOverrideForce(String configurationId, Parameter<T> parameter) {
    // try to get override for explicit configuration
    String value = lookup.getOverrideForce(configurationId, parameter.getName());
    if (value == null) {
      // try to get override for all configurations
      value = lookup.getOverrideForce(parameter.getName());
    }
    return TypeConversion.stringToObject(value, parameter.getType());
  }

  @Override
  public Set<String> getLockedParameterNames(String configurationId) {
    // get locked parameter names for explicit configuration and global and merge them
    Set<String> lockedParameterNamesScope = lookup.getLockedParameterNames(configurationId);
    Set<String> lockedParameterNamesGlobal = lookup.getLockedParameterNames();
    if (lockedParameterNamesScope.isEmpty()) {
      return lockedParameterNamesGlobal;
    }
    else if (lockedParameterNamesGlobal.isEmpty()) {
      return lockedParameterNamesScope;
    }
    else {
      Set<String> merged = new HashSet<>(lockedParameterNamesScope.size() + lockedParameterNamesGlobal.size());
      merged.addAll(lockedParameterNamesScope);
      merged.addAll(lockedParameterNamesGlobal);
      return ImmutableSet.copyOf(merged);
    }
  }

  void bindParameterOverrideProvider(ParameterOverrideProvider service, Map<String, Object> props) {
    parameterOverrideProviders.bind(service, props);
    updateLoockup();
  }

  void unbindParameterOverrideProvider(ParameterOverrideProvider service, Map<String, Object> props) {
    parameterOverrideProviders.unbind(service, props);
    updateLoockup();
  }

  /**
   * Update lookup maps with override maps from all override providers.
   */
  private void updateLoockup() {
    synchronized (parameterOverrideProviders) {
      ParameterOverrideInfoLookup newLookup = new ParameterOverrideInfoLookup();
      for (ParameterOverrideProvider provider : parameterOverrideProviders) {
        newLookup.addOverrideMap(provider.getOverrideMap());
      }
      newLookup.seal();
      lookup = newLookup;
    }
  }

}
