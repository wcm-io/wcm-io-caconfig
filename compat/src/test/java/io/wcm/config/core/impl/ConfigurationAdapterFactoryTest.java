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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.ImmutableMap;

import io.wcm.config.api.Configuration;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigurationAdapterFactoryTest {

  private final AemContext context = new AemContext();

  @Mock
  private Resource resource;
  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private ConfigurationData configData;

  @Mock
  private ConfigurationManager configManager;
  @Mock
  private ConfigurationResourceResolver configResourceResolver;

  private ConfigurationAdapterFactory underTest;

  private static final Map<String, Object> PROPS = ImmutableMap.<String, Object>of(
      "param1", "value1",
      "param2", 555);

  @BeforeEach
  void setUp() {
    when(request.getResource()).thenReturn(resource);

    when(configManager.getConfiguration(resource, ParameterProviderBridge.DEFAULT_CONFIG_NAME)).thenReturn(configData);
    when(configResourceResolver.getContextPath(resource)).thenReturn("/context/path");
    when(configData.getEffectiveValues()).thenReturn(new ValueMapDecorator(PROPS));

    context.registerService(ConfigurationManager.class, configManager);
    context.registerService(ConfigurationResourceResolver.class, configResourceResolver);
    underTest = context.registerInjectActivateService(new ConfigurationAdapterFactory());
  }

  @Test
  void testConfigurationResource() {
    Configuration config = underTest.getAdapter(resource, Configuration.class);
    assertEquals("/context/path", config.getConfigurationId());
    assertEquals(PROPS, config);
  }

  @Test
  void testConfigurationRequest() {
    Configuration config = underTest.getAdapter(request, Configuration.class);
    assertEquals("/context/path", config.getConfigurationId());
    assertEquals(PROPS, config);
  }

  @Test
  void testConfigurationInvalid() {
    assertNull(underTest.getAdapter(this, Configuration.class));
  }

}
