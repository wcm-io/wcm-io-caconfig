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
package io.wcm.config.core.override.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.component.ComponentContext;

@RunWith(MockitoJUnitRunner.class)
public class SystemPropertyOverrideProviderTest {

  @Mock
  private ComponentContext componentContext;
  @Mock
  private Dictionary<String, Object> config;

  @Before
  public void setUp() {
    when(componentContext.getProperties()).thenReturn(config);
    System.setProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[default]param1", "value1");
    System.setProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[/config1]param2", "value2");
  }

  @After
  public void tearDown() {
    System.clearProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[default]param1");
    System.clearProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[/config1]param2");
  }

  @Test
  public void testEnabled() {
    SystemPropertyOverrideProvider provider = new SystemPropertyOverrideProvider();
    when(config.get(SystemPropertyOverrideProvider.PROPERTY_ENABLED)).thenReturn(true);
    provider.activate(componentContext);

    Map<String,String> overrideMap = provider.getOverrideMap();
    assertEquals("value1", overrideMap.get("[default]param1"));
    assertEquals("value2", overrideMap.get("[/config1]param2"));
  }

  @Test
  public void testDisabled() {
    SystemPropertyOverrideProvider provider = new SystemPropertyOverrideProvider();
    when(config.get(SystemPropertyOverrideProvider.PROPERTY_ENABLED)).thenReturn(false);
    provider.activate(componentContext);

    Map<String, String> overrideMap = provider.getOverrideMap();
    assertTrue(overrideMap.isEmpty());
  }

}
