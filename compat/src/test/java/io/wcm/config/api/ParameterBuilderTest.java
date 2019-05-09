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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

class ParameterBuilderTest {

  private static final String APP_ID = "/apps/app1";

  @Test
  void testBuilder() {
    Map<String, Object> props = ImmutableMap.<String, Object>of("prop1", "value1");

    Parameter<String> param = ParameterBuilder.create("param1", String.class, APP_ID)
        .defaultOsgiConfigProperty("service:prop1")
        .defaultValue("defValue")
        .property("prop3", "value3")
        .properties(props)
        .property("prop2", "value2")
        .build();

    assertEquals("param1", param.getName());
    assertEquals("param1[String]", param.toString());
    assertEquals(String.class, param.getType());
    assertEquals(APP_ID, param.getApplicationId());
    assertEquals("service:prop1", param.getDefaultOsgiConfigProperty());
    assertEquals("defValue", param.getDefaultValue());
    assertEquals("value1", param.getProperties().get("prop1", String.class));
    assertEquals("value2", param.getProperties().get("prop2", String.class));
    assertEquals("value3", param.getProperties().get("prop3", String.class));
  }

  @Test
  void testSort() {
    Set<Parameter<String>> params = ImmutableSortedSet.of(
        ParameterBuilder.create("app5_param2", String.class, APP_ID).build(),
        ParameterBuilder.create("app1_param2", String.class, APP_ID).build(),
        ParameterBuilder.create("app5_param1", String.class, APP_ID).build()
        );

    Parameter[] paramArray = params.toArray(new Parameter[params.size()]);
    assertEquals("app1_param2", paramArray[0].getName());
    assertEquals("app5_param1", paramArray[1].getName());
    assertEquals("app5_param2", paramArray[2].getName());
  }

  @Test
  void testInvalidName() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param 1", String.class, APP_ID).build();
    });
  }

  @Test
  void testNullName() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create(null, String.class, APP_ID).build();
    });
  }

  @Test
  void testInvalidType() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param1", Date.class, APP_ID).build();
    });
  }

  @Test
  void testNullType() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param1", null, APP_ID).build();
    });
  }

  @Test
  void testInvalidApplicationId() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param1", String.class, "app 1").build();
    });
  }

  @Test
  void testNullApplicationId() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param1", String.class, null).build();
    });
  }

  @Test
  void testInvalidDefaultOsgiConfigProperty() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param1", String.class, APP_ID).defaultOsgiConfigProperty("aaa").build();
    });
  }

  @Test
  void testNullDefaultOsgiConfigProperty() {
    assertThrows(IllegalArgumentException.class, () -> {
      ParameterBuilder.create("param1", String.class, APP_ID).defaultOsgiConfigProperty(null).build();
    });
  }

}
