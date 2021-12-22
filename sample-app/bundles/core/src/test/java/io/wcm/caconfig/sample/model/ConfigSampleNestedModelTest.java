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
package io.wcm.caconfig.sample.model;

import static io.wcm.testing.mock.wcmio.caconfig.ContextPlugins.WCMIO_CACONFIG;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.caconfig.sample.config.ConfigSampleNested;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ConfigSampleNestedModelTest {

  private final AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .plugin(WCMIO_CACONFIG)
      .build();

  private Page page;

  @BeforeEach
  void setUp() {
    MockContextAwareConfig.registerAnnotationClasses(context, ConfigSampleNested.class);

    context.create().resource("/content/region/site",
        "sling:configRef", "/conf/region/site");

    page = context.create().page("/content/region/site/en");

    MockContextAwareConfig.writeConfiguration(context, "/content/region/site", ConfigSampleNested.class,
        "stringParam", "value1");
  }

  @Test
  void testContentComponent() {
    context.currentResource(context.create().resource(page, "content"));

    ConfigSampleNestedModel model = context.request().adaptTo(ConfigSampleNestedModel.class);
    assertNotNull(model);
    assertEquals("value1", model.getConfig().stringParam());
  }

  @Test
  void testStructureComponent() {
    context.currentResource(context.create().resource("/conf/app1/settings/wcm/templates/content/structure/jcr:content/root/content"));
    context.currentPage(page);

    ConfigSampleNestedModel model = context.request().adaptTo(ConfigSampleNestedModel.class);
    assertNotNull(model);
    assertEquals("value1", model.getConfig().stringParam());
  }

}
