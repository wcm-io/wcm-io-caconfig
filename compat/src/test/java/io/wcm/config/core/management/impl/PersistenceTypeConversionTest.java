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

import static io.wcm.config.core.management.impl.PersistenceTypeConversion.fromPersistenceType;
import static io.wcm.config.core.management.impl.PersistenceTypeConversion.isTypeConversionRequired;
import static io.wcm.config.core.management.impl.PersistenceTypeConversion.toPersistenceType;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class PersistenceTypeConversionTest {

  static final Map<String, String> SAMPLE_MAP = ImmutableMap.<String, String>builder()
      .put("key1", "value=1")
      .put("key2", "55")
      .put("key3", "5.5")
      .build();

  static final Map<String, Object> SAMPLE_MAP_OTHERTYPES = ImmutableMap.<String, Object>builder()
      .put("key1", "value=1")
      .put("key2", 55)
      .put("key3", 5.5d)
      .build();

  static final String[] SAMPLE_MAP_PERSISTENCE = new String[] {
      "key1=value\\=1",
      "key2=55",
      "key3=5.5"
  };

  @Test
  public void testString() throws Exception {
    assertEquals("value1", toPersistenceType("value1", String.class));
    assertEquals("value1", fromPersistenceType("value1", String.class));
  }

  @Test
  public void testMap() throws Exception {
    assertArrayEquals(SAMPLE_MAP_PERSISTENCE, (String[])toPersistenceType(SAMPLE_MAP_OTHERTYPES, Map.class));
    assertEquals(SAMPLE_MAP, fromPersistenceType(SAMPLE_MAP_PERSISTENCE, Map.class));
  }

  @Test
  public void testIsTypeConversionRequired() throws Exception {
    assertTrue(isTypeConversionRequired(Map.class));
    assertFalse(isTypeConversionRequired(String.class));
  }

}
