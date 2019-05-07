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

import static io.wcm.config.core.impl.ParameterProviderBridge.DEFAULT_CONFIG_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableSet;

import io.wcm.config.spi.ParameterProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ParameterProviderBridgeTest {

  private final AemContext context = new AemContext();

  private ParameterProviderBridge underTest;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(new ParameterProviderBridge());
  }

  @Test
  void testWithParameters() {
    context.registerService(ParameterProvider.class, new DummyParameterProvider());

    assertEquals(ImmutableSet.of(DEFAULT_CONFIG_NAME), underTest.getConfigurationNames());
    ConfigurationMetadata metadata = underTest.getConfigurationMetadata(DEFAULT_CONFIG_NAME);
    assertNotNull(metadata);

    assertEquals(DEFAULT_CONFIG_NAME, metadata.getName());
    assertEquals(7, metadata.getPropertyMetadata().size());

    PropertyMetadata<?> stringParam = metadata.getPropertyMetadata().get("stringParam");
    assertEquals("stringParam", stringParam.getName());
    assertEquals(String.class, stringParam.getType());
    assertEquals("group1: label-stringParam", stringParam.getLabel());
    assertEquals("desc-stringParam", stringParam.getDescription());
    assertEquals("defValue", stringParam.getDefaultValue());

    PropertyMetadata<?> stringArrayParam = metadata.getPropertyMetadata().get("stringArrayParam");
    assertEquals("stringArrayParam", stringArrayParam.getName());
    assertEquals(String[].class, stringArrayParam.getType());
    assertEquals("group1: stringArrayParam", stringArrayParam.getLabel());
    assertArrayEquals(new String[] {
        "value1", "value2"
    }, (String[])stringArrayParam.getDefaultValue());

    PropertyMetadata<?> intParam = metadata.getPropertyMetadata().get("intParam");
    assertEquals("intParam", intParam.getName());
    assertEquals(int.class, intParam.getType());

    PropertyMetadata<?> longParam = metadata.getPropertyMetadata().get("longParam");
    assertEquals("longParam", longParam.getName());
    assertEquals(long.class, longParam.getType());

    PropertyMetadata<?> doubleParam = metadata.getPropertyMetadata().get("doubleParam");
    assertEquals("doubleParam", doubleParam.getName());
    assertEquals(double.class, doubleParam.getType());

    PropertyMetadata<?> boolParam = metadata.getPropertyMetadata().get("boolParam");
    assertEquals("boolParam", boolParam.getName());
    assertEquals(boolean.class, boolParam.getType());

    PropertyMetadata<?> mapParam = metadata.getPropertyMetadata().get("mapParam");
    assertEquals("mapParam", mapParam.getName());
    assertEquals(String[].class, mapParam.getType());
  }

  @Test
  void testWithoutParameters() {
    assertTrue(underTest.getConfigurationNames().isEmpty());
    assertNull(underTest.getConfigurationMetadata(DEFAULT_CONFIG_NAME));
  }

}
