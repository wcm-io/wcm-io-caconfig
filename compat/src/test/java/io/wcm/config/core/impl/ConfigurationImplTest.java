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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.sling.commons.resource.ImmutableValueMap;

public class ConfigurationImplTest {

  private static final String APP_ID = "/apps/app1";
  private static final String CONFIG_ID = "/config1";
  private static final Map<String, Object> SAMPLE_PROPS = new HashMap<>();
  static {
    SAMPLE_PROPS.put("prop1", "value1");
    SAMPLE_PROPS.put("prop2", 55);
  }

  private static final Parameter<String> PARAM1 = ParameterBuilder.create("prop1", String.class, APP_ID).build();
  private static final Parameter<Integer> PARAM2 = ParameterBuilder.create("prop2", Integer.class, APP_ID).build();
  private static final Parameter<String> PARAM3 = ParameterBuilder.create("prop3", String.class, APP_ID).build();

  private Configuration underTest;

  @Before
  public void setUp() {
    underTest = new ConfigurationImpl(CONFIG_ID, SAMPLE_PROPS);
  }

  @Test
  public void testProperties() {
    assertEquals(CONFIG_ID, underTest.getConfigurationId());
    assertEquals(CONFIG_ID, underTest.toString());
    assertEquals("value1", underTest.get("prop1", String.class));
    assertEquals((Integer)55, underTest.get("prop2", Integer.class));
    assertEquals("value1", underTest.get(PARAM1));
    assertEquals((Integer)55, underTest.get(PARAM2));
  }

  @Test
  public void testDefault() {
    assertEquals("def", underTest.get("prop3", "def"));
    assertEquals("def", underTest.get(PARAM3, "def"));
  }

  @Test
  public void testMapAccessMethods() {
    assertEquals(2, underTest.size());
    assertFalse(underTest.isEmpty());
    assertTrue(underTest.containsKey("prop1"));
    assertTrue(underTest.containsValue("value1"));
    assertEquals("value1", underTest.get("prop1"));
    assertTrue(underTest.keySet().contains("prop1"));
    assertTrue(underTest.values().contains("value1"));
    assertEquals(2, underTest.entrySet().size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMapClear() {
    underTest.clear();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMapRemove() {
    underTest.remove("prop2");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMapPut() {
    underTest.put("prop3", "value3");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testMapPutAll() {
    underTest.putAll(ImmutableValueMap.builder()
        .put("prop4", 25)
        .put("prop5", 33)
        .build());
  }

}
