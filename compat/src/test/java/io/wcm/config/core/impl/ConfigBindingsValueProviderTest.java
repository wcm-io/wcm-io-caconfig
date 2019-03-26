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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.config.api.Configuration;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class ConfigBindingsValueProviderTest {

  private final AemContext context = new AemContext();

  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private Bindings bindings;
  @Mock
  private Configuration config;

  private ConfigBindingsValueProvider underTest;

  @BeforeEach
  void setUp() {
    when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(true);
    when(bindings.get(SlingBindings.REQUEST)).thenReturn(request);
    when(request.adaptTo(Configuration.class)).thenReturn(config);
  }

  @Test
  void testWithConfig() {
    underTest = context.registerInjectActivateService(new ConfigBindingsValueProvider(), "enabled", true);
    underTest.addBindings(bindings);
    verify(bindings).put(ConfigBindingsValueProvider.BINDING_VARIABLE, config);
  }

  @Test
  void testWithoutConfig() {
    when(request.adaptTo(Configuration.class)).thenReturn(null);
    underTest = context.registerInjectActivateService(new ConfigBindingsValueProvider(), "enabled", true);
    underTest.addBindings(bindings);
    verify(bindings, never()).put(anyString(), any(Object.class));
  }

  @Test
  void testNoRequest() {
    underTest = context.registerInjectActivateService(new ConfigBindingsValueProvider(), "enabled", true);
    when(bindings.containsKey(SlingBindings.REQUEST)).thenReturn(false);
    underTest.addBindings(bindings);
    verify(bindings, never()).put(anyString(), any(Object.class));
  }

  @Test
  void testDisabled() {
    underTest = context.registerInjectActivateService(new ConfigBindingsValueProvider(), "enabled", false);
    underTest.addBindings(bindings);
    verify(bindings, never()).put(anyString(), any(Object.class));
  }

}
