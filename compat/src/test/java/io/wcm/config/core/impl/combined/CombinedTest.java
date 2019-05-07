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
package io.wcm.config.core.impl.combined;

import static io.wcm.config.core.impl.combined.CombinedAppContext.CONFIG_PAGE_TEMPLATE;
import static io.wcm.config.core.impl.combined.CombinedAppContext.STRUCTURE_PAGE_TEMPLATE;
import static io.wcm.config.core.impl.combined.SampleParameterProvider.PROP_1;
import static io.wcm.config.core.impl.combined.SampleParameterProvider.PROP_2;
import static io.wcm.config.core.impl.combined.SampleParameterProvider.PROP_3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;

import io.wcm.config.api.Configuration;
import io.wcm.config.core.impl.ParameterProviderBridge;
import io.wcm.config.core.persistence.impl.ToolsConfigPagePersistenceProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * Test all configuration services in combination.
 */
@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class CombinedTest {

  private static final String CONTEXT_RESOURCE_PATH = "/content/region1/site1/en";

  private final AemContext context = CombinedAppContext.newAemContext();

  @BeforeEach
  void setUp() throws Exception {
    // mount sample content
    context.load().json("/combined-test-content.json", "/content");
    context.currentPage(CONTEXT_RESOURCE_PATH);
  }

  @Test
  void testConfigWithInheritance() {
    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        "enabled", true,
        "configPageTemplate", CONFIG_PAGE_TEMPLATE,
        "structurePageTemplate", STRUCTURE_PAGE_TEMPLATE);

    Resource resource = context.request().getResource();
    Configuration config = resource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertEquals("value1-l3", config.get(PROP_1));
    assertEquals("value2-l2", config.get(PROP_2));
    assertEquals("value3-l1", config.get(PROP_3));
  }

  @Test
  void testConfigWithInheritance_Disabled() {
    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        "enabled", false);

    Resource resource = context.request().getResource();
    Configuration config = resource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertNull(config.get(PROP_1));
    assertNull(config.get(PROP_2));
    assertNull(config.get(PROP_3));
  }

  @Test
  void testWriteReadConfig() {
    long currentTime = Calendar.getInstance().getTimeInMillis();

    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        "enabled", true,
        "configPageTemplate", CONFIG_PAGE_TEMPLATE,
        "structurePageTemplate", STRUCTURE_PAGE_TEMPLATE);

    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    Resource contextResource = context.resourceResolver().getResource(CONTEXT_RESOURCE_PATH);
    configManager.persistConfiguration(contextResource, ParameterProviderBridge.DEFAULT_CONFIG_NAME,
        new ConfigurationPersistData(ImmutableValueMap.of(
            PROP_3.getName(), "value3-new")));

    Resource resource = context.request().getResource();
    Configuration config = resource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertEquals("value1-l2", config.get(PROP_1));
    assertEquals("value2-l2", config.get(PROP_2));
    assertEquals("value3-new", config.get(PROP_3));

    // check last modified
    Page configPage = context.pageManager().getPage(CONTEXT_RESOURCE_PATH + "/tools/config");
    ValueMap pageProps = configPage.getProperties();
    Calendar lastModified = pageProps.get(NameConstants.PN_LAST_MOD, Calendar.class);
    assertTrue(lastModified.getTimeInMillis() >= currentTime);
  }

  @Test
  void testWriteReadConfig_NewPath() {
    long currentTime = Calendar.getInstance().getTimeInMillis();

    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        "enabled", true,
        "configPageTemplate", CONFIG_PAGE_TEMPLATE,
        "structurePageTemplate", STRUCTURE_PAGE_TEMPLATE);

    Resource contextResource = context.create().resource("/content/region1/site2/en");

    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    configManager.persistConfiguration(contextResource, ParameterProviderBridge.DEFAULT_CONFIG_NAME,
        new ConfigurationPersistData(ImmutableValueMap.of(
            PROP_3.getName(), "value3-new")));

    Configuration config = contextResource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertEquals("value1-l1", config.get(PROP_1));
    assertEquals("value2-l1", config.get(PROP_2));
    assertEquals("value3-new", config.get(PROP_3));

    Page toolsPage = context.pageManager().getPage("/content/region1/site2/en/tools");
    assertEquals(STRUCTURE_PAGE_TEMPLATE, toolsPage.getProperties().get(NameConstants.PN_TEMPLATE));

    Page configPage = context.pageManager().getPage("/content/region1/site2/en/tools/config");
    assertEquals(CONFIG_PAGE_TEMPLATE, configPage.getProperties().get(NameConstants.PN_TEMPLATE));

    // check last modified
    ValueMap pageProps = configPage.getProperties();
    Calendar lastModified = pageProps.get(NameConstants.PN_LAST_MOD, Calendar.class);
    assertTrue(lastModified.getTimeInMillis() >= currentTime);
  }

}
