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
public class AbsoluteParentContextPathStrategyTest {

  @Rule
  public AemContext context = new AemContext();

  protected Resource level1;
  protected Resource level2;
  protected Resource level3;
  protected Resource level4;

  @Before
  public void setUp() throws Exception {
    level1 = context.create().page("/content/region1").adaptTo(Resource.class);
    level2 = context.create().page("/content/region1/site1").adaptTo(Resource.class);
    level3 = context.create().page("/content/region1/site1/en").adaptTo(Resource.class);
    level4 = context.create().page("/content/region1/site1/en/page1").adaptTo(Resource.class);
  }

  @Test
  public void testWithInvalidConfig() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new AbsoluteParentContextPathStrategy());

    assertNoResult(context, underTest.findContextResources(level4));
  }

  @Test
  public void testWithLevels() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new AbsoluteParentContextPathStrategy(),
        "levels", new int[] { 1, 3 });

    assertResult(context, underTest.findContextResources(level4),
        "/content/region1/site1/en", "/conf/region1/site1/en",
        "/content/region1", "/conf/region1");

    assertResult(context, underTest.findContextResources(level3),
        "/content/region1/site1/en", "/conf/region1/site1/en",
        "/content/region1", "/conf/region1");

    assertResult(context, underTest.findContextResources(level2),
        "/content/region1", "/conf/region1");

    assertResult(context, underTest.findContextResources(level1),
        "/content/region1", "/conf/region1");
  }

  @Test
  public void testWithAlternativePatterns() {
    ContextPathStrategy underTest = context.registerInjectActivateService(new AbsoluteParentContextPathStrategy(),
        "levels", new int[] { 1, 3 },
        "contextPathRegex", "^(/content/.+)$",
        "contextPathBlacklistRegex", "^.*/region\\d+?$",
        "configPathPatterns", new String[] { "/conf/test1$1", "/conf/test2$1" });

    assertResult(context, underTest.findContextResources(level4),
        "/content/region1/site1/en", "/conf/test2/content/region1/site1/en",
        "/content/region1/site1/en", "/conf/test1/content/region1/site1/en");
  }

}
