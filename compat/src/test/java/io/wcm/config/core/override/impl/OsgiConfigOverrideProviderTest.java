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
import java.util.Hashtable;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.service.component.ComponentContext;

@RunWith(MockitoJUnitRunner.class)
public class OsgiConfigOverrideProviderTest {

  @Mock
  private ComponentContext componentContext;
  private Dictionary<String, Object> config;

  @Before
  public void setUp() {
    config = new Hashtable<>();
    config.put(OsgiConfigOverrideProvider.PROPERTY_OVERRIDES, new String[] {
        "[default]param1=value1",
        "[/config1]param2=value2"
    });
    when(componentContext.getProperties()).thenReturn(config);
  }

  @Test
  public void testEnabled() {
    OsgiConfigOverrideProvider provider = new OsgiConfigOverrideProvider();
    config.put(OsgiConfigOverrideProvider.PROPERTY_ENABLED, true);
    provider.activate(componentContext);

    Map<String,String> overrideMap = provider.getOverrideMap();
    assertEquals("value1", overrideMap.get("[default]param1"));
    assertEquals("value2", overrideMap.get("[/config1]param2"));
  }

  @Test
  public void testDisabled() {
    OsgiConfigOverrideProvider provider = new OsgiConfigOverrideProvider();
    config.put(OsgiConfigOverrideProvider.PROPERTY_ENABLED, false);
    provider.activate(componentContext);

    Map<String, String> overrideMap = provider.getOverrideMap();
    assertTrue(overrideMap.isEmpty());
  }

}
