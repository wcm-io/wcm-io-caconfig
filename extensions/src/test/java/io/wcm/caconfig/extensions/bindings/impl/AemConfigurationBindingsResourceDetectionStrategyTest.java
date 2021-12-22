/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.caconfig.extensions.bindings.impl;

import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import javax.script.Bindings;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationBindingsResourceDetectionStrategyMultiplexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class AemConfigurationBindingsResourceDetectionStrategyTest {

  private final AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .build();

  private ConfigurationBindingsResourceDetectionStrategyMultiplexer strategyMultiplexer;

  @Mock
  private Bindings bindings;

  @BeforeEach
  void setUp() throws Exception {
    context.registerInjectActivateService(AemConfigurationBindingsResourceDetectionStrategy.class);
    strategyMultiplexer = context.getService(ConfigurationBindingsResourceDetectionStrategyMultiplexer.class);
  }

  @Test
  void testWithoutRequest() {
    Resource resource = strategyMultiplexer.detectResource(bindings);
    assertNull(resource);
  }

  @Test
  void testWithoutCurrentPage() {
    when(bindings.get(SlingBindings.REQUEST)).thenReturn(context.request());

    Resource resource = strategyMultiplexer.detectResource(bindings);
    assertNull(resource);
  }

  @Test
  @SuppressWarnings("null")
  void testWithCurrentPage() {
    when(bindings.get(SlingBindings.REQUEST)).thenReturn(context.request());
    Page page = context.currentPage(context.create().page("/content/my-page"));

    Resource resource = strategyMultiplexer.detectResource(bindings);
    assertNotNull(resource);
    assertEquals(page.getPath(), resource.getPath());
  }

}
