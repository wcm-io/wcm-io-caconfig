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

import static io.wcm.config.core.management.util.TypeConversion.KEY_VALUE_DELIMITER;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterators;

import io.wcm.config.core.management.util.ConversionStringUtils;

/**
 * Handles conversions that are required to transform a type to be prepared persisted and vice versa.
 * The configuration API supports types like Map that need to transformed to other types e.g. a string array
 * to be persisted.
 */
final class PersistenceTypeConversion {

  private PersistenceTypeConversion() {
    // static methods only
  }

  /**
   * Checks if type conversion for persistence is required for the given parameter type.
   * @param parameterType Parameter type
   * @return true if type conversion is required.
   */
  public static boolean isTypeConversionRequired(Class<?> parameterType) {
    if (Map.class.isAssignableFrom(parameterType)) {
      return true;
    }
    return false;
  }

  /**
   * Convert object to be persisted.
   * @param value Configured value
   * @param parameterType Parameter type
   * @return value that can be persisted
   */
  public static Object toPersistenceType(Object value, Class<?> parameterType) {
    if (!isTypeConversionRequired(parameterType)) {
      return value;
    }
    if (Map.class.isAssignableFrom(parameterType) && (value instanceof Map)) {
      Map<?, ?> map = (Map<?, ?>)value;
      Map.Entry<?, ?>[] entries = Iterators.toArray(map.entrySet().iterator(), Map.Entry.class);
      String[] stringArray = new String[entries.length];
      for (int i = 0; i < entries.length; i++) {
        Map.Entry<?, ?> entry = entries[i];
        String entryKey = Objects.toString(entry.getKey(), "");
        String entryValue = Objects.toString(entry.getValue(), "");
        stringArray[i] = ConversionStringUtils.encodeString(entryKey) + KEY_VALUE_DELIMITER + ConversionStringUtils.encodeString(entryValue);
      }
      return stringArray;
    }
    throw new IllegalArgumentException("Type conversion not supported: " + parameterType.getName());
  }

  /**
   * Convert object from persistence to be used in configuration.
   * @param value Persisted value
   * @param parameterType Parameter type
   * @return Configured value
   */
  public static Object fromPersistenceType(Object value, Class<?> parameterType) {
    if (!isTypeConversionRequired(parameterType)) {
      return value;
    }
    if (Map.class.isAssignableFrom(parameterType) && (value instanceof String[])) {
      String[] rows = (String[])value;
      Map<String, String> map = new LinkedHashMap<>();
      for (int i = 0; i < rows.length; i++) {
        String[] keyValue = ConversionStringUtils.splitPreserveAllTokens(rows[i], KEY_VALUE_DELIMITER.charAt(0));
        if (keyValue.length == 2 && StringUtils.isNotEmpty(keyValue[0])) {
          String entryKey = keyValue[0];
          String entryValue = StringUtils.isEmpty(keyValue[1]) ? null : keyValue[1];
          map.put(ConversionStringUtils.decodeString(entryKey), ConversionStringUtils.decodeString(entryValue));
        }
      }
      return map;
    }
    throw new IllegalArgumentException("Type conversion not supported: " + parameterType.getName());
  }

}
