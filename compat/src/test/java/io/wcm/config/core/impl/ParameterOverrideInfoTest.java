/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParameterOverrideInfoTest {

  @Test
  void testParameterOnly() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("aaa");
    assertNull(underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertTrue(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  void testParameterWithConfigurationId() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[/path1]aaa");
    assertEquals("/path1", underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertFalse(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  void testParameterWithConfigurationIdLocked() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[/path1:locked]aaa");
    assertEquals("/path1", underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertTrue(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  void testParameterLocked() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[locked]aaa");
    assertNull(underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertTrue(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  void testParameterWithDefaultScope() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[default]aaa");
    assertNull(underTest.getConfigurationId());
    assertTrue(underTest.isOverrideSystemDefault());
    assertFalse(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  void testParameterWithDefaultScopeLockedNotAllowed() {
    assertThrows(IllegalArgumentException.class, () -> {
      new ParameterOverrideInfo("[default:locked]aaa");
    });
  }

  @Test
  void testParameterDoubleLockedNotAllowed() {
    assertThrows(IllegalArgumentException.class, () -> {
      new ParameterOverrideInfo("[locked:locked]aaa");
    });
  }

  @Test
  void testInvalid() {
    assertThrows(IllegalArgumentException.class, () -> {
      new ParameterOverrideInfo("[aaa");
    });
  }

}
