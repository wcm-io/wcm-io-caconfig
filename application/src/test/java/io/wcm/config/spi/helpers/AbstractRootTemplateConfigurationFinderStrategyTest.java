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
package io.wcm.config.spi.helpers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.testing.mock.aem.junit.AemContext;

public class AbstractRootTemplateConfigurationFinderStrategyTest {

  private static final String APP_ID = "/apps/app1";
  private static final String TEMPLATE_STRUCTURE = "/apps/app1/templates/structure";
  private static final String TEMPLATE_SITEROOT = "/apps/app1/templates/siteRoot";
  private static final String TEMPLATE_CONTENT = "/apps/app1/templates/content";

  @Rule
  public AemContext context = new AemContext();

  private ConfigurationFinderStrategy underTest;

  @Before
  public void setUp() {
    underTest = new AbstractRootTemplateConfigurationFinderStrategy(APP_ID, 1, 4, TEMPLATE_SITEROOT) {
      // nothing to override
    };
  }

  @Test
  public void testGetApplicationId() {
    assertEquals(APP_ID, underTest.getApplicationId());
  }

  @Test
  public void testFindConfigurationIds() {
    context.create().page("/content");
    context.create().page("/content/region1", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1/en", TEMPLATE_SITEROOT);
    context.create().page("/content/region1/country1/en/page1", TEMPLATE_CONTENT);
    context.create().resource("/content/region1/country1/en/page1/jcr:content/node1/node11");

    assertConfigurationIds("/", new String[0]);
    assertConfigurationIds("/content", new String[0]);

    assertConfigurationIds("/content/region1",
        "/content/region1");

    assertConfigurationIds("/content/region1/country1",
        "/content/region1",
        "/content/region1/country1");

    assertConfigurationIds("/content/region1/country1/en",
        "/content/region1",
        "/content/region1/country1",
        "/content/region1/country1/en");

    assertConfigurationIds("/content/region1/country1/en/page1",
        "/content/region1",
        "/content/region1/country1",
        "/content/region1/country1/en");

    assertConfigurationIds("/content/region1/country1/en/page1/jcr:content/node1/node11",
        "/content/region1",
        "/content/region1/country1",
        "/content/region1/country1/en");
  }

  @Test
  public void testFindConfigurationIds_WithMissingPageInHierarchy() {
    context.create().page("/content");
    context.create().page("/content/region1", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1/en", TEMPLATE_SITEROOT);
    context.create().page("/content/region1/country1/en/page1", TEMPLATE_CONTENT);
    context.create().resource("/content/region1/country1/en/page1/jcr:content/node1/node11");

    assertConfigurationIds("/content/region1/country1/en/page1/jcr:content/node1/node11",
        "/content/region1",
        "/content/region1/country1/en");
  }

  @Test
  public void testFindConfigurationIds_MaxLevel() {
    context.create().page("/content");
    context.create().page("/content/region1", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1/sub1", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1/sub1/sub2", TEMPLATE_STRUCTURE);
    context.create().page("/content/region1/country1/sub1/sub2/sub3", TEMPLATE_STRUCTURE);

    assertConfigurationIds("/content/region1/country1/sub1/sub2/sub3",
        "/content/region1",
        "/content/region1/country1",
        "/content/region1/country1/sub1",
        "/content/region1/country1/sub1/sub2");
  }

  private void assertConfigurationIds(String resourcePath, String... configurationIds) {
    List<String> expectedConfigurationIds = ImmutableList.copyOf(configurationIds);
    Resource resource = context.resourceResolver().getResource(resourcePath);
    List<String> detectedConfigurationIds = ImmutableList.copyOf(underTest.findConfigurationIds(resource));
    assertEquals(expectedConfigurationIds, detectedConfigurationIds);
  }

}
