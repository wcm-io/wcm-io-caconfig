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
package io.wcm.config.core.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.config.api.Configuration;

@RunWith(MockitoJUnitRunner.class)
public class ConfigBindingsValueProviderTest {

  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private Bindings bindings;
  @Mock
  private Configuration config;

  private ConfigBindingsValueProvider underTest;

  @Before
  public void setUp() {
    underTest = new ConfigBindingsValueProvider();
    when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(true);
    when(bindings.get(SlingBindings.REQUEST)).thenReturn(request);
  }

  @Test
  public void testWithConfig() {
    when(request.adaptTo(Configuration.class)).thenReturn(config);
    underTest.addBindings(bindings);
    verify(bindings).put(ConfigBindingsValueProvider.BINDING_VARIABLE, config);
  }

  @Test
  public void testWithoutConfig() {
    underTest.addBindings(bindings);
    verify(bindings, never()).put(anyString(), any(Object.class));
  }

  @Test
  public void testNoRequest() {
    when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(false);
    underTest.addBindings(bindings);
    verify(bindings, never()).put(anyString(), any(Object.class));
  }

}
