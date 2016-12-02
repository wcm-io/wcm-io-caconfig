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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.spi.ParameterOverrideProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;

@RunWith(MockitoJUnitRunner.class)
public class ParameterOverrideImplTest {

  private static final String APP_ID = "/apps/app1";

  @Mock
  private ParameterOverrideProvider provider1;
  private static final Map<String, Object> SERVICE_PROPS_1 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 1L,
          Constants.SERVICE_RANKING, 10);

  @Mock
  private ParameterOverrideProvider provider2;
  private static final Map<String, Object> SERVICE_PROPS_2 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 2L,
          Constants.SERVICE_RANKING, 5);

  private static final Parameter<String> PARAM1 = ParameterBuilder.create("param1", String.class, APP_ID).build();
  private static final Parameter<String> PARAM2 = ParameterBuilder.create("param2", String.class, APP_ID).build();
  private static final Parameter<String> PARAM3 = ParameterBuilder.create("param3", String.class, APP_ID).build();

  private ParameterOverrideImpl underTest;

  @Before
  public void setUp() {
    when(provider1.getOverrideMap()).thenReturn(ImmutableMap.<String, String>builder()
        .put("[default]param1", "value1")
        .put("[/config1:locked]param1", "value11")
        .put("[/config1]param2", "value21")
        .build());
    when(provider2.getOverrideMap()).thenReturn(ImmutableMap.<String, String>builder()
        .put("[default]param1", "value2")
        .put("[locked]param2", "value2")
        .put("param3", "value3")
        .build());

    underTest = new ParameterOverrideImpl();
    underTest.bindParameterOverrideProvider(provider1, SERVICE_PROPS_1);
    underTest.bindParameterOverrideProvider(provider2, SERVICE_PROPS_2);
  }

  @Test
  public void testOverrideSystemDefault() {
    assertEquals("value2", underTest.getOverrideSystemDefault(PARAM1));
    assertNull(underTest.getOverrideSystemDefault(PARAM2));
    assertNull(underTest.getOverrideSystemDefault(PARAM3));
  }

  @Test
  public void testOverrideForce() {
    assertEquals("value11", underTest.getOverrideForce("/config1", PARAM1));
    assertNull(underTest.getOverrideForce("/config2", PARAM1));

    assertEquals("value21", underTest.getOverrideForce("/config1", PARAM2));
    assertEquals("value2", underTest.getOverrideForce("/config2", PARAM2));

    assertEquals("value3", underTest.getOverrideForce("/config1", PARAM3));
    assertEquals("value3", underTest.getOverrideForce("/config2", PARAM3));
  }

  @Test
  public void testTypes() {
    when(provider1.getOverrideMap()).thenReturn(ImmutableMap.<String, String>builder()
        .put("[default]stringParam", "value1")
        .put("[default]stringArrayParam", "value1;value2;")
        .put("[default]integerParam", "55")
        .build());

    underTest = new ParameterOverrideImpl();
    underTest.bindParameterOverrideProvider(provider1, SERVICE_PROPS_1);
    underTest.bindParameterOverrideProvider(provider2, SERVICE_PROPS_2);

    assertEquals("value1", underTest.getOverrideSystemDefault(
        ParameterBuilder.create("stringParam", String.class, APP_ID).build()));
    assertArrayEquals(new String[] {
        "value1", "value2", ""
    }, underTest.getOverrideSystemDefault(ParameterBuilder.create("stringArrayParam", String[].class, APP_ID).build()));
    assertEquals((Integer)55, underTest.getOverrideSystemDefault(
        ParameterBuilder.create("integerParam", Integer.class, APP_ID).build()));
  }

  @Test
  public void testGetLockedParameterNames() {
    assertEquals(ImmutableSet.of("param1", "param2", "param3"), underTest.getLockedParameterNames("/config1"));
    assertEquals(ImmutableSet.of("param2", "param3"), underTest.getLockedParameterNames("/config2"));
  }

}
