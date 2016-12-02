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
 * Defines a configuration parameter.
 * @param <T> Parameter value type
 */
@ProviderType
public final class Parameter<T> implements Comparable<Parameter> {

  private final String name;
  private final Class<T> type;
  private final ValueMap properties;
  private final String applicationId;
  private final String defaultOsgiConfigProperty;
  private final T defaultValue;

  Parameter(String name, Class<T> type, String applicationId,
      String defaultOsgiConfigProperty, T defaultValue, ValueMap properties) {
    this.name = name;
    this.type = type;
    this.applicationId = applicationId;
    this.defaultOsgiConfigProperty = defaultOsgiConfigProperty;
    this.defaultValue = defaultValue;
    this.properties = properties;
  }

  /**
   * @return Parameter name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return Parameter type
   */
  public Class<T> getType() {
    return this.type;
  }

  /**
   * @return Application Id
   */
  public String getApplicationId() {
    return this.applicationId;
  }

  /**
   * References OSGi configuration property which is checked for default value if this parameter is not set
   * in any configuration.
   * @return OSGi configuration parameter name with syntax {serviceClassName}:{propertyName}
   */
  public String getDefaultOsgiConfigProperty() {
    return this.defaultOsgiConfigProperty;
  }

  /**
   * @return Default value if parameter is not set for configuration
   *         and no default value is defined in OSGi configuration
   */
  public T getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * @return Further properties for documentation and configuration of behavior in configuration editor.
   */
  public ValueMap getProperties() {
    return this.properties;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Parameter)) {
      return false;
    }
    return this.name.equals(((Parameter)obj).name);
  }

  @Override
  public int compareTo(Parameter o) {
    return this.name.compareTo(o.getName());
  }

  @Override
  public String toString() {
    return this.name + "[" + this.type.getSimpleName() + "]";
  }

}
