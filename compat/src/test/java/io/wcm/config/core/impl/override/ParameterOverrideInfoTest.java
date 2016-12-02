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
package io.wcm.config.core.impl.override;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParameterOverrideInfoTest {

  @Test
  public void testParameterOnly() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("aaa");
    assertNull(underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertTrue(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  public void testParameterWithConfigurationId() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[/path1]aaa");
    assertEquals("/path1", underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertFalse(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  public void testParameterWithConfigurationIdLocked() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[/path1:locked]aaa");
    assertEquals("/path1", underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertTrue(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  public void testParameterLocked() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[locked]aaa");
    assertNull(underTest.getConfigurationId());
    assertFalse(underTest.isOverrideSystemDefault());
    assertTrue(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test
  public void testParameterWithDefaultScope() {
    ParameterOverrideInfo underTest = new ParameterOverrideInfo("[default]aaa");
    assertNull(underTest.getConfigurationId());
    assertTrue(underTest.isOverrideSystemDefault());
    assertFalse(underTest.isLocked());
    assertEquals("aaa", underTest.getParameterName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParameterWithDefaultScopeLockedNotAllowed() {
    new ParameterOverrideInfo("[default:locked]aaa");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParameterDoubleLockedNotAllowed() {
    new ParameterOverrideInfo("[locked:locked]aaa");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalid() {
    new ParameterOverrideInfo("[aaa");
  }

}
