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
package io.wcm.config.core.impl.combined;

import static io.wcm.config.core.impl.combined.CombinedAppContext.APP_ID;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.spi.ParameterProvider;

public class SampleParameterProvider implements ParameterProvider {

  static final Parameter<String> PROP_1 = ParameterBuilder.create("prop1", String.class, APP_ID).build();
  static final Parameter<String> PROP_2 = ParameterBuilder.create("prop2", String.class, APP_ID).build();
  static final Parameter<String> PROP_3 = ParameterBuilder.create("prop3", String.class, APP_ID).build();

  private static final Set<Parameter<?>> PARAMS = ImmutableSet.<Parameter<?>>builder()
      .add(PROP_1)
      .add(PROP_2)
      .add(PROP_3)
      .build();

  @Override
  public Set<Parameter<?>> getParameters() {
    return PARAMS;
  }

}
