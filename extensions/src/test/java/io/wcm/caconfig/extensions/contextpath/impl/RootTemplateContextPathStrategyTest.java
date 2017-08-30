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
package io.wcm.caconfig.extensions.contextpath.impl;

import static io.wcm.caconfig.extensions.contextpath.impl.TestUtils.assertNoResult;
import static io.wcm.caconfig.extensions.contextpath.impl.TestUtils.assertResult;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.spi.ContextPathStrategy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class RootTemplateContextPathStrategyTest {

  private static final String TEMPLATE_1 = "/apps/app1/templates/template1";
  private static final String TEMPLATE_2 = "/apps/app1/templates/template2";

  @Rule
  public AemContext context = new AemContext();

  private Resource level1;
  private Resource level2;
  private Resource level3;
  private Resource level4;

  @Before
  public void setUp() {
    level1 = context.create().page("/content/region1").adaptTo(Resource.class);
    level2 = context.create().page("/content/region1/site1", TEMPLATE_1).adaptTo(Resource.class);
    level3 = context.create().page("/content/region1/site1/en", TEMPLATE_2).adaptTo(Resource.class);
    level4 = context.create().page("/content/region1/site1/en/page1").adaptTo(Resource.class);
  }

  @Test
  public void testWithInvalidConfig() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new RootTemplateContextPathStrategy());

    assertNoResult(underTest.findContextResources(level4));
  }

  @Test
  public void testWithTemplate() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new RootTemplateContextPathStrategy(),
        "templatePaths", new String[] { TEMPLATE_1 });

    assertResult(underTest.findContextResources(level4),
        "/content/region1/site1", "/conf/region1/site1",
        "/content/region1", "/conf/region1");

    assertResult(underTest.findContextResources(level3),
        "/content/region1/site1", "/conf/region1/site1",
        "/content/region1", "/conf/region1");

    assertResult(underTest.findContextResources(level2),
        "/content/region1/site1", "/conf/region1/site1",
        "/content/region1", "/conf/region1");

    assertResult(underTest.findContextResources(level1),
        "/content/region1", "/conf/region1");
  }

  @Test
  public void testWithAlternativePatterns() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new RootTemplateContextPathStrategy(),
        "templatePaths", new String[] { TEMPLATE_1 },
        "contextPathRegex", "^(/content/.+)$",
        "configPathPatterns", new String[] { "/conf/test1$1", "/conf/test2$1" });

    assertResult(underTest.findContextResources(level4),
        "/content/region1/site1", "/conf/test2/content/region1/site1",
        "/content/region1/site1", "/conf/test1/content/region1/site1",
        "/content/region1", "/conf/test2/content/region1",
        "/content/region1", "/conf/test1/content/region1");
  }

  @Test
  public void testWithTemplate_TemplatMatchAllLevels() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new RootTemplateContextPathStrategy(),
        "templatePaths", new String[] { TEMPLATE_1, TEMPLATE_2 },
        "templateMatchAllLevels", true);

    assertResult(underTest.findContextResources(level4),
        "/content/region1/site1/en", "/conf/region1/site1/en",
        "/content/region1/site1", "/conf/region1/site1");

    assertResult(underTest.findContextResources(level3),
        "/content/region1/site1/en", "/conf/region1/site1/en",
        "/content/region1/site1", "/conf/region1/site1");

    assertResult(underTest.findContextResources(level2),
        "/content/region1/site1", "/conf/region1/site1");

    assertResult(underTest.findContextResources(level1));
  }

}
