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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.editor.EditorProperties;
import io.wcm.config.spi.ParameterProvider;

final class DummyParameterProvider implements ParameterProvider {

  @Override
  public Set<Parameter<?>> getParameters() {
    return ImmutableSet.<Parameter<?>>of(
        ParameterBuilder.create("stringParam", String.class).defaultValue("defValue")
        .property(EditorProperties.LABEL, "label-stringParam")
        .property(EditorProperties.DESCRIPTION, "desc-stringParam").build(),
        ParameterBuilder.create("stringArrayParam", String[].class).defaultValue(new String[] {
            "value1", "value2"
        }).build(),
        ParameterBuilder.create("intParam", Integer.class).build(),
        ParameterBuilder.create("longParam", Long.class).build(),
        ParameterBuilder.create("doubleParam", Double.class).build(),
        ParameterBuilder.create("boolParam", Boolean.class).defaultValue(true).build(),
        ParameterBuilder.create("mapParam", Map.class).build());
  }

}
