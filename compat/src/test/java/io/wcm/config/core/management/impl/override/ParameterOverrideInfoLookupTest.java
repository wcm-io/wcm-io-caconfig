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
package io.wcm.config.core.management.impl.override;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ParameterOverrideInfoLookupTest {

  private ParameterOverrideInfoLookup underTest;

  @Before
  public void setUp() {
    underTest = new ParameterOverrideInfoLookup();

    underTest.addOverrideMap(ImmutableMap.<String, String>builder()
        .put("[/path1]param1", "v1")
        .put("[/path2:locked]param1", "v2")
        .put("param1", "v3")
        .put("[locked]param2", "v10")
        .build());

    underTest.addOverrideMap(ImmutableMap.<String, String>builder()
        .put("[/path1]param1", "v1a")
        .put("[/path1/path11]param1", "v1b")
        .put("[/path2/path21]param1", "v1c")
        .put("[default]param1", "v4")
        .put("[/path1]param2", "v10a")
        .build());

    underTest.seal();
  }

  @Test
  public void testGetOverrideSystemDefault() {
    assertEquals("v4", underTest.getOverrideSystemDefault("param1"));
    assertNull(underTest.getOverrideSystemDefault("param2"));
  }

  @Test
  public void testGetOverrideForceForConfigurationId() {
    assertEquals("v1", underTest.getOverrideForce("/path1", "param1"));
    assertEquals("v1b", underTest.getOverrideForce("/path1/path11", "param1"));
    assertNull(null, underTest.getOverrideForce("/path1/path11/path111", "param1"));

    assertEquals("v2", underTest.getOverrideForce("/path2", "param1"));
    assertEquals("v1c", underTest.getOverrideForce("/path2/path21", "param1"));
    assertNull(underTest.getOverrideForce("/path2/path21/211", "param1"));

    assertEquals("v10a", underTest.getOverrideForce("/path1", "param2"));
    assertNull(underTest.getOverrideForce("/path2", "param2"));
  }

  @Test
  public void testGetOverrideForce() {
    assertEquals("v3", underTest.getOverrideForce("param1"));
    assertEquals("v10", underTest.getOverrideForce("param2"));
  }

  @Test
  public void testGetLockedParameterNames() {
    assertEquals(ImmutableSet.of("param1", "param2"), underTest.getLockedParameterNames());
  }

  @Test
  public void testGetLockedParameterNamesForConfigurationId() {
    assertEquals(ImmutableSet.of(), underTest.getLockedParameterNames("/path1"));
    assertEquals(ImmutableSet.of(), underTest.getLockedParameterNames("/path1/path11"));
    assertEquals(ImmutableSet.of("param1"), underTest.getLockedParameterNames("/path2"));
    assertEquals(ImmutableSet.of(), underTest.getLockedParameterNames("/path2/path21"));
  }

  @Test(expected = IllegalStateException.class)
  public void testSeal() {
    // do not allow adding additional maps after sealing
    underTest.addOverrideMap(ImmutableMap.of("param5", "v5"));
  }

}
