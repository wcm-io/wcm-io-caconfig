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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Helps converting types from object to parameter type.
 */
@ProviderType
public final class ConversionStringUtils {

  static final char ARRAY_DELIMITER_CHAR = ';';
  static final char KEY_VALUE_DELIMITER_CHAR = '=';

  /**
   * Delimiter to prefix escaped characters.
   */
  private static final char ESCAPE_CHAR = '\\';

  private ConversionStringUtils() {
    // static methods only
  }

  /**
   * String tokenizer that preservers all tokens and ignores escaped separator chars.
   * @param value Value to tokenize
   * @param separatorChar Separator char
   * @return String parts. Never null.
   */
  public static String[] splitPreserveAllTokens(String value, char separatorChar) {
    if (value == null) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    int len = value.length();
    if (len == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    List<String> list = new ArrayList<>();
    int i = 0;
    int start = 0;
    boolean match = false;
    boolean lastMatch = false;
    int escapeStart = -2;
    while (i < len) {
      char c = value.charAt(i);
      if (c == ESCAPE_CHAR) {
        escapeStart = i;
      }
      if (c == separatorChar && escapeStart != i - 1) {
        lastMatch = true;
        list.add(value.substring(start, i));
        match = false;
        start = ++i;
        continue;
      }
      match = true;
      i++;
    }
    if (match || lastMatch) {
      list.add(value.substring(start, i));
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Escape all delimiter chars in string.
   * @param values Strings
   * @return Encoded strings
   */
  public static String[] encodeString(String[] values) {
    if (values == null) {
      return null;
    }
    String[] encodedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      encodedValues[i] = encodeString(values[i]);
    }
    return encodedValues;
  }

  /**
   * Escape all delimiter chars in string.
   * @param value String
   * @return Encoded string
   */
  public static String encodeString(String value) {
    if (value == null) {
      return null;
    }
    StringBuilder encoded = new StringBuilder();
    int len = value.length();
    for (int i = 0; i < len; i++) {
      char c = value.charAt(i);
      switch (c) {
        case ARRAY_DELIMITER_CHAR:
        case KEY_VALUE_DELIMITER_CHAR:
        case ESCAPE_CHAR:
          encoded.append(ESCAPE_CHAR);
          break;
        default:
          // just append char
      }
      encoded.append(c);
    }
    return encoded.toString();
  }

  /**
   * Unescape all delimiter chars in string.
   * @param values Strings
   * @return Decoded strings
   */
  public static String[] decodeString(String[] values) {
    if (values == null) {
      return null;
    }
    String[] decodedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      decodedValues[i] = decodeString(values[i]);
    }
    return decodedValues;
  }

  /**
   * Unescape all delimiter chars in string.
   * @param value String
   * @return Decoded string
   */
  public static String decodeString(String value) {
    if (value == null) {
      return null;
    }
    StringBuilder decoded = new StringBuilder();
    int len = value.length();
    int escapeStart = -2;
    for (int i = 0; i < len; i++) {
      char c = value.charAt(i);
      if (c == ESCAPE_CHAR && escapeStart != i - 1) {
        escapeStart = i;
      }
      else {
        decoded.append(c);
      }
    }
    return decoded.toString();
  }

}
