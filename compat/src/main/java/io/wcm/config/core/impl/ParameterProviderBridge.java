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
package io.wcm.config.core.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.caconfig.spi.ConfigurationMetadataProvider;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.apache.sling.commons.osgi.RankedServices.ChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.editor.EditorProperties;
import io.wcm.config.spi.ParameterProvider;

/**
 * Bridges parameter provider to configuration metadata providers.
 */
@Component(service = ConfigurationMetadataProvider.class, immediate = true, reference = {
    @Reference(service = ParameterProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
        name = "parameterProvider", bind = "bindParameterProvider", unbind = "unbindParameterProvider")
})
public class ParameterProviderBridge implements ConfigurationMetadataProvider, ChangeListener {

  /**
   * All legacy config parameters are mapped to single config name 'config'.
   */
  public static final String DEFAULT_CONFIG_NAME = "config";

  private RankedServices<ParameterProvider> parameterProviders = new RankedServices<>(Order.ASCENDING, this);

  private volatile ConfigurationMetadata configMetadata;

  @Override
  public SortedSet<String> getConfigurationNames() {
    if (configMetadata != null) {
      return ImmutableSortedSet.of(DEFAULT_CONFIG_NAME);
    }
    else {
      return ImmutableSortedSet.of();
    }
  }

  @Override
  public ConfigurationMetadata getConfigurationMetadata(String configName) {
    if (StringUtils.equals(configName, DEFAULT_CONFIG_NAME)) {
      return configMetadata;
    }
    else {
      return null;
    }
  }

  void bindParameterProvider(ParameterProvider service, Map<String, Object> props) {
    parameterProviders.bind(service, props);
  }

  void unbindParameterProvider(ParameterProvider service, Map<String, Object> props) {
    parameterProviders.unbind(service, props);
  }

  @Override
  public void changed() {
    List<Parameter<?>> parameters = new ArrayList<>();
    for (ParameterProvider parameterProvider : parameterProviders) {
      for (Parameter<?> parameter : parameterProvider.getParameters()) {
        parameters.add(parameter);
      }
    }

    if (parameters.isEmpty()) {
      configMetadata = null;
    }
    else {
      configMetadata = toConfigMetadata(parameters);
    }
  }

  @SuppressWarnings("unchecked")
  private ConfigurationMetadata toConfigMetadata(List<Parameter<?>> parameters) {
    SortedSet<PropertyMetadata<?>> properties = new TreeSet<>(new Comparator<PropertyMetadata<?>>() {
      @Override
      public int compare(PropertyMetadata<?> o1, PropertyMetadata<?> o2) {
        String sort1 = StringUtils.defaultString(o1.getLabel(), o1.getName());
        String sort2 = StringUtils.defaultString(o2.getLabel(), o2.getName());
        return sort1.compareTo(sort2);
      }
    });

    for (Parameter<?> parameter : parameters) {
      PropertyMetadata<?> property;
      if (parameter.getType().equals(Map.class)) {
        property = toPropertyStringArray((Parameter<Map>)parameter);
      }
      else {
        property = toProperty(parameter);
      }
      String label = (String)parameter.getProperties().get(EditorProperties.LABEL);
      String description = (String)parameter.getProperties().get(EditorProperties.DESCRIPTION);
      String group = (String)parameter.getProperties().get(EditorProperties.GROUP);
      if (group != null) {
        label = group + ": " + StringUtils.defaultString(label, parameter.getName());
      }
      properties.add(property.label(label).description(description));
    }

    return new ConfigurationMetadata(DEFAULT_CONFIG_NAME, properties, false)
        .label("wcm.io Configuration Parameters");
  }

  private <T> PropertyMetadata<T> toProperty(Parameter<T> parameter) {
    return new PropertyMetadata<T>(parameter.getName(), parameter.getType())
        .defaultValue(parameter.getDefaultValue());
  }

  private PropertyMetadata<String[]> toPropertyStringArray(Parameter<Map> parameter) {
    return new PropertyMetadata<String[]>(parameter.getName(), String[].class);
  }

}
