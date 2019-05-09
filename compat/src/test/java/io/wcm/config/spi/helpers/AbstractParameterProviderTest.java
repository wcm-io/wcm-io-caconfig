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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.spi.ParameterProvider;

class AbstractParameterProviderTest {

  @Test
  void testFromSet() {
    ParameterProvider provider = new AbstractParameterProvider(ImmutableSet.<Parameter<?>>builder()
        .add(ParamsSample.PARAM_1).add(ParamsSample.PARAM_2).build()) {
      // nothing to override
    };
    Set<Parameter<?>> params = provider.getParameters();

    assertEquals(2, params.size());
    assertTrue(params.contains(ParamsSample.PARAM_1));
    assertTrue(params.contains(ParamsSample.PARAM_2));
    assertFalse(params.contains(ParamsSample.PARAM_3));
  }

  @Test
  void testFromType() {
    ParameterProvider provider = new AbstractParameterProvider(ParamsSample.class) {
      // nothing to override
    };
    Set<Parameter<?>> params = provider.getParameters();

    assertEquals(2, params.size());
    assertTrue(params.contains(ParamsSample.PARAM_1));
    assertTrue(params.contains(ParamsSample.PARAM_2));
    assertFalse(params.contains(ParamsSample.PARAM_3));
  }

  static final class ParamsSample {

    private ParamsSample() {
      // static methods only
    }

    static final String APP_ID = "/apps/app1";

    public static final Parameter<String> PARAM_1 = ParameterBuilder.create("param1", String.class, APP_ID).build();
    public static final Parameter<Integer> PARAM_2 = ParameterBuilder.create("param2", Integer.class, APP_ID).build();
    private static final Parameter<Long> PARAM_3 = ParameterBuilder.create("param3", Long.class, APP_ID).build();

  }


}
