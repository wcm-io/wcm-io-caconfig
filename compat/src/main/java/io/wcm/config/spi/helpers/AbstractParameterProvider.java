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
package io.wcm.config.spi.helpers;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.osgi.annotation.versioning.ConsumerType;

import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.spi.ParameterProvider;

/**
 * Abstract implementation of {@link ParameterProvider} providing list of parameters either from given
 * parameter set, or from reading all public static fields from a given class definition.
 */
@ConsumerType
public abstract class AbstractParameterProvider implements ParameterProvider {

  private final Set<Parameter<?>> parameters;

  /**
   * @param parameters Set of parameters for parameter provider
   */
  protected AbstractParameterProvider(Set<Parameter<?>> parameters) {
    this.parameters = parameters;
  }

  /**
   * @param type Type containing parameter definitions as public static fields.
   */
  protected AbstractParameterProvider(Class<?> type) {
    this(getParametersFromPublicFields(type));
  }

  @Override
  public final Set<Parameter<?>> getParameters() {
    return parameters;
  }

  /**
   * Get all parameters defined as public static fields in the given type.
   * @param type Type
   * @return Set of parameters
   */
  private static Set<Parameter<?>> getParametersFromPublicFields(Class<?> type) {
    Set<Parameter<?>> params = new HashSet<>();
    try {
      Field[] fields = type.getFields();
      for (Field field : fields) {
        if (field.getType().isAssignableFrom(Parameter.class)) {
          params.add((Parameter<?>)field.get(null));
        }
      }
    }
    catch (IllegalArgumentException | IllegalAccessException ex) {
      throw new RuntimeException("Unable to access fields of " + type.getName(), ex);
    }
    return ImmutableSet.copyOf(params);
  }

}
