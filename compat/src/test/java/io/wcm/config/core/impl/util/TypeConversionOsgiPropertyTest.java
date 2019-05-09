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
package io.wcm.config.core.impl.util;

import static io.wcm.config.core.management.util.TypeConversion.KEY_VALUE_DELIMITER;
import static io.wcm.config.core.management.util.TypeConversion.osgiPropertyToObject;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import io.wcm.config.core.management.util.ConversionStringUtils;

@SuppressWarnings("null")
class TypeConversionOsgiPropertyTest {

  @Test
  void testString() {
    assertEquals("value", osgiPropertyToObject("value", String.class, null));
    assertNull(osgiPropertyToObject(null, String.class, null));
    assertEquals("defValue", osgiPropertyToObject(null, String.class, "defValue"));
  }

  @Test
  void testStringArray() {
    assertArrayEquals(new String[] {
        "value"
    }, osgiPropertyToObject("value", String[].class, null));
    assertArrayEquals(new String[] {
        "value1",
        "value2"
    }, osgiPropertyToObject(new Object[] {
        "value1", "value2"
    }, String[].class, null));
    assertArrayEquals(new String[] {
        "value1",
        "value2",
        ""
    }, osgiPropertyToObject(new Object[] {
        "value1",
        "value2",
        ""
    }, String[].class, null));
    assertNull(osgiPropertyToObject(null, String[].class, null));
    assertArrayEquals(new String[] {
        "defValue"
    }, osgiPropertyToObject(null, String[].class, new String[] {
        "defValue"
    }));
  }

  @Test
  void testStringArrayWithSpecialChars() {
    String[] values = new String[] {
        "value1",
        "value;2",
        "value=3",
    };
    String[] encodedArray = ConversionStringUtils.encodeString(values);
    String[] convertedValues = osgiPropertyToObject(encodedArray, String[].class, new String[0]);
    assertArrayEquals(values, convertedValues);
  }

  @Test
  void testInteger() {
    assertEquals((Integer)55, osgiPropertyToObject(55, Integer.class, null));
    assertEquals((Integer)55, osgiPropertyToObject(55L, Integer.class, null));
    assertEquals((Integer)55, osgiPropertyToObject("55", Integer.class, null));
    assertEquals((Integer)0, osgiPropertyToObject("wurst", Integer.class, null));
    assertEquals((Integer)66, osgiPropertyToObject("wurst", Integer.class, 66));
    assertEquals((Integer)0, osgiPropertyToObject(null, Integer.class, null));
    assertEquals((Integer)66, osgiPropertyToObject(null, Integer.class, 66));
  }

  @Test
  void testLong() {
    assertEquals((Long)55L, osgiPropertyToObject(55, Long.class, null));
    assertEquals((Long)55L, osgiPropertyToObject(55L, Long.class, null));
    assertEquals((Long)55L, osgiPropertyToObject("55", Long.class, null));
    assertEquals((Long)0L, osgiPropertyToObject("wurst", Long.class, null));
    assertEquals((Long)66L, osgiPropertyToObject("wurst", Long.class, 66L));
    assertEquals((Long)0L, osgiPropertyToObject(null, Long.class, null));
    assertEquals((Long)66L, osgiPropertyToObject(null, Long.class, 66L));
  }

  @Test
  void testDouble() {
    assertEquals(55d, osgiPropertyToObject(55, Double.class, null), 0.0001d);
    assertEquals(55d, osgiPropertyToObject(55L, Double.class, null), 0.0001d);
    assertEquals(55d, osgiPropertyToObject(55d, Double.class, null), 0.0001d);
    assertEquals(55.123d, osgiPropertyToObject(55.123d, Double.class, null), 0.0001d);
    assertEquals(55d, osgiPropertyToObject("55", Double.class, null), 0.0001d);
    assertEquals(0d, osgiPropertyToObject("wurst", Double.class, null), 0.0001d);
    assertEquals(66.123d, osgiPropertyToObject("wurst", Double.class, 66.123d), 0.0001d);
    assertEquals(0d, osgiPropertyToObject(null, Double.class, null), 0.0001d);
    assertEquals(66.123d, osgiPropertyToObject(null, Double.class, 66.123d), 0.0001d);
  }

  @Test
  void testBoolean() {
    assertTrue(osgiPropertyToObject(true, Boolean.class, null));
    assertTrue(osgiPropertyToObject("true", Boolean.class, null));
    assertFalse(osgiPropertyToObject("wurst", Boolean.class, null));
    assertFalse(osgiPropertyToObject("wurst", Boolean.class, true));
    assertFalse(osgiPropertyToObject(null, Boolean.class, null));
    assertTrue(osgiPropertyToObject(null, Boolean.class, true));
  }

  @Test
  void testMap() {
    Map<String, String> map = new HashMap<>();
    map.put("key1", "abc");
    map.put("key2", "def");
    map.put("key3", null);
    assertEquals(map, osgiPropertyToObject(new String[] {
        "key1=abc", "key2=def", "key3=", "", "=xyz"
    }, Map.class, null));

    map = new HashMap<>();
    assertEquals(map, osgiPropertyToObject(null, Map.class, null));

    map = new HashMap<>();
    map.put("key1", "abc");
    assertEquals(map, osgiPropertyToObject(null, Map.class, map));
  }

  @Test
  void testMapWithSpecialChars() {
    Map<String, String> map = new TreeMap<>();
    map.put("key1", "value1");
    map.put("key;2", "value;2");
    map.put("key=3", "value=3");
    map.put("key=4;", "=value;4");

    List<String> encodedMapList = new ArrayList<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      encodedMapList.add(ConversionStringUtils.encodeString(entry.getKey()) + KEY_VALUE_DELIMITER
          + ConversionStringUtils.encodeString(entry.getValue()));
    }
    String[] encodedMap = encodedMapList.toArray(new String[encodedMapList.size()]);
    assertArrayEquals(new String[] {
        "key1=value1",
        "key\\;2=value\\;2",
        "key\\=3=value\\=3",
        "key\\=4\\;=\\=value\\;4"
    }, encodedMap);

    @SuppressWarnings("unchecked")
    Map<String, String> convertedMap = osgiPropertyToObject(encodedMap, Map.class, new HashMap<String, String>());
    assertEquals(map, convertedMap);
  }

  @Test
  void testIllegalType() {
    assertThrows(IllegalArgumentException.class, () -> {
      osgiPropertyToObject("value", Date.class, null);
    });
  }

}
