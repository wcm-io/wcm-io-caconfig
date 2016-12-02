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

import static io.wcm.config.core.impl.util.ConversionStringUtils.decodeString;
import static io.wcm.config.core.impl.util.ConversionStringUtils.encodeString;
import static io.wcm.config.core.impl.util.ConversionStringUtils.splitPreserveAllTokens;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ConversionStringUtilsTest {

  @Test
  public void testSplitPreserveAllTokens() {
    assertArrayEquals(new String[0], splitPreserveAllTokens(null, ';'));
    assertArrayEquals(new String[0], splitPreserveAllTokens("", ';'));

    assertArrayEquals(new String[] {
        "aa"
    }, splitPreserveAllTokens("aa", ';'));
    assertArrayEquals(new String[] {
        "aa",
        "bb",
        "cc"
    }, splitPreserveAllTokens("aa;bb;cc", ';'));
    assertArrayEquals(new String[] {
        "aa",
        ""
    }, splitPreserveAllTokens("aa;", ';'));
    assertArrayEquals(new String[] {
        "",
        "aa",
        ""
    }, splitPreserveAllTokens(";aa;", ';'));

    assertArrayEquals(new String[] {
        "aa\\;bb\\;cc"
    }, splitPreserveAllTokens("aa\\;bb\\;cc", ';'));
    assertArrayEquals(new String[] {
        "\\;aa",
        "bb\\;",
        "cc"
    }, splitPreserveAllTokens("\\;aa;bb\\;;cc", ';'));
  }

  @Test
  public void testEncodeString() {
    assertNull(encodeString((String)null));
    assertEquals("", encodeString(""));

    assertEquals("aa", encodeString("aa"));
    assertEquals("aa\\;", encodeString("aa;"));
    assertEquals("\\=a\\;a", encodeString("=a;a"));
    assertEquals("a\\\\\\;a", encodeString("a\\;a"));
  }

  @Test
  public void testDecodeStringString() {
    assertNull(decodeString((String)null));
    assertEquals("", decodeString(""));

    assertEquals("aa", decodeString("aa"));
    assertEquals("aa;", decodeString("aa\\;"));
    assertEquals("=a;a", decodeString("\\=a\\;a"));
    assertEquals("a\\;a", decodeString("a\\\\\\;a"));
  }

}
