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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationInjectResourceDetectionStrategyMultiplexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class AemConfigurationInjectResourceDetectionStrategyTest {

  private final AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .build();

  private ConfigurationInjectResourceDetectionStrategyMultiplexer strategyMultiplexer;

  @BeforeEach
  void setUp() throws Exception {
    context.registerInjectActivateService(AemConfigurationInjectResourceDetectionStrategy.class);
    strategyMultiplexer = context.getService(ConfigurationInjectResourceDetectionStrategyMultiplexer.class);
  }

  @Test
  void testWithoutCurrentPage() {
    Resource resource = strategyMultiplexer.detectResource(context.request());
    assertNull(resource);
  }

  @Test
  @SuppressWarnings("null")
  void testWithCurrentPage() {
    Page page = context.currentPage(context.create().page("/content/my-page"));

    Resource resource = strategyMultiplexer.detectResource(context.request());
    assertNotNull(resource);
    assertEquals(page.getPath(), resource.getPath());
  }

}
