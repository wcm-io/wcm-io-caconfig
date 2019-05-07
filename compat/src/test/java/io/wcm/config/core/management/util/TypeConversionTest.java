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
package io.wcm.config.core.management.util;

import static io.wcm.config.core.management.util.TypeConversion.objectToString;
import static io.wcm.config.core.management.util.TypeConversion.stringToObject;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TypeConversionTest {

  @Test
  void testString() {
    assertConversion("value", "value", String.class);
    assertConversion(null, null, String.class);
  }

  @Test
  void testStringArray() {
    assertConversion(new String[] {
        "value"
    }, "value", String[].class);
    assertConversion(new String[] {
        "value1",
        "value2"
    }, "value1;value2", String[].class);
    assertArrayEquals(new String[] {
        "value1",
        "value2",
        ""
    }, stringToObject("value1;value2;", String[].class));
    assertConversion(null, null, String[].class);
  }

  @Test
  void testStringArrayWithSpecialChars() {
    String[] values = new String[] {
        "value1",
        "value;2",
        "value=3",
    };
    String convertedString = objectToString(values);
    assertEquals("value1;value\\;2;value\\=3", convertedString);
    String[] convertedValues = stringToObject(convertedString, String[].class);
    assertArrayEquals(values, convertedValues);
  }

  @Test
  void testInteger() {
    assertConversion(55, "55", Integer.class);
    assertEquals((Integer)0, stringToObject("wurst", Integer.class));
    assertConversion(null, null, Integer.class);
  }

  @Test
  void testLong() {
    assertConversion(55L, "55", Long.class);
    assertEquals((Long)0L, stringToObject("wurst", Long.class));
    assertConversion(null, null, Long.class);
  }

  @Test
  void testDouble() {
    assertConversion(55d, "55.0", Double.class);
    assertConversion(55.123d, "55.123", Double.class);
    assertEquals((Double)0d, stringToObject("wurst", Double.class));
    assertConversion(null, null, Double.class);
  }

  @Test
  void testBoolean() {
    assertConversion(true, "true", Boolean.class);
    assertFalse(stringToObject("wurst", Boolean.class));
    assertConversion(null, null, Boolean.class);
  }

  @Test
  void testMap() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("key1", "abc");
    map.put("key2", "def");
    map.put("key3", null);

    assertConversion(map, "key1=abc;key2=def;key3=", Map.class);
    assertEquals(map, stringToObject("key1=abc;key2=def;key3=;;=xyz", Map.class));
    assertConversion(null, null, Map.class);
  }

  @Test
  void testMapWithSpecialChars() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put("key1", "value1");
    map.put("key;2", "value;2");
    map.put("key=3", "value=3");
    map.put("key=4;", "=value;4");

    @SuppressWarnings({ "unchecked", "null" })
    Map<String, String> convertedMap = stringToObject(objectToString(map), Map.class);
    assertEquals(map, convertedMap);
  }

  @Test
  void testToObjectIllegalType() {
    assertThrows(IllegalArgumentException.class, () -> {
      stringToObject("value", Date.class);
    });
  }

  @Test
  void testToStringIllegalType() {
    assertThrows(IllegalArgumentException.class, () -> {
      objectToString(new Date());
    });
  }

  /**
   * Asserts that conversions works in boths ways (stringToObject and objectToString) and returns the same value.
   * @param objectValue Object value
   * @param stringValue String value
   * @param type type
   */
  private void assertConversion(Object objectValue, String stringValue, Class<?> type) {
    if (type == String[].class) {
      assertArrayEquals((String[])objectValue, stringToObject(stringValue, String[].class), "stringToObject(" + stringValue + ")");
    }
    else {
      assertEquals(objectValue, stringToObject(stringValue, type), "stringToObject(" + stringValue + ")");
    }
    assertEquals(stringValue, objectToString(objectValue), "objectToString(" + objectValue + ")");
  }

}
