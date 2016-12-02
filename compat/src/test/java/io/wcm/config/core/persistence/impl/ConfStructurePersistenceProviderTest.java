/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.config.core.persistence.impl;

import static io.wcm.config.core.persistence.impl.AbstractConfigPagePersistenceProvider.CONFIG_RESOURCE_NAME;
import static io.wcm.config.core.persistence.impl.ConfStructurePersistenceProvider.CONF_ROOT_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableMap;

import io.wcm.config.spi.ParameterPersistenceProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class ConfStructurePersistenceProviderTest {

  private static final String CONFIG_ID = "/content/site1";
  private static final String CONFIG_PAGE_TEMPLATE = "/apps/dummy/templates/config";
  private static final String STRUCTURE_PAGE_TEMPLATE = "/apps/dummy/templates/tools";

  @Rule
  public final AemContext context = new AemContext();

  private ParameterPersistenceProvider underTest;

  @Before
  public void setUp() {
    underTest = context.registerInjectActivateService(new ConfStructurePersistenceProvider(),
        "enabled", true,
        "configPageTemplate", CONFIG_PAGE_TEMPLATE,
        "structurePageTemplate", STRUCTURE_PAGE_TEMPLATE);
  }

  @Test
  public void testGetNoPage() {
    assertNull(underTest.get(context.resourceResolver(), CONFIG_ID));
  }

  @Test
  public void testGetPageNoConfigResource() {
    context.create().page(CONF_ROOT_PATH + CONFIG_ID, CONFIG_PAGE_TEMPLATE);
    assertEquals(ImmutableMap.of(), underTest.get(context.resourceResolver(), CONFIG_ID));
  }

  @Test
  public void testGetStoreGetValues() throws PersistenceException {
    long currentTime = Calendar.getInstance().getTimeInMillis();

    Page configPage = context.create().page(CONF_ROOT_PATH + CONFIG_ID, CONFIG_PAGE_TEMPLATE);

    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();
    context.resourceResolver().create(configPage.getContentResource(), CONFIG_RESOURCE_NAME, props);
    context.resourceResolver().commit();

    assertEquals(props, ImmutableValueMap.copyOf(underTest.get(context.resourceResolver(), CONFIG_ID)));

    ValueMap newProps = ImmutableValueMap.builder()
        .put("props1", "value2")
        .put("props3", "value3")
        .build();
    assertTrue(underTest.store(context.resourceResolver(), CONFIG_ID, newProps));

    assertEquals(newProps, ImmutableValueMap.copyOf(underTest.get(context.resourceResolver(), CONFIG_ID)));

    // check last modified
    ValueMap pageProps = configPage.getProperties();
    Calendar lastModified = pageProps.get(NameConstants.PN_LAST_MOD, Calendar.class);
    assertTrue(lastModified.getTimeInMillis() >= currentTime);
  }

  @Test
  public void testNoPageStoreGetValues() throws PersistenceException {
    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();
    underTest.store(context.resourceResolver(), CONFIG_ID, props);

    assertEquals(props, ImmutableValueMap.copyOf(underTest.get(context.resourceResolver(), CONFIG_ID)));

    assertNotNull(context.pageManager().getPage(CONF_ROOT_PATH + CONFIG_ID));
  }

  @Test
  public void testNoParentPageStore() throws PersistenceException {
    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();
    assertTrue(underTest.store(context.resourceResolver(), "/content/site2", props));
    assertNotNull(context.pageManager().getPage(CONF_ROOT_PATH + "/content/site2"));
  }

  @Test
  public void testDisabled() throws PersistenceException {
    underTest = context.registerInjectActivateService(new ConfStructurePersistenceProvider(),
        "enabled", false);

    Page configPage = context.create().page(CONF_ROOT_PATH + CONFIG_ID, CONFIG_PAGE_TEMPLATE);

    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();
    context.create().resource(configPage.getContentResource() + "/" + CONFIG_RESOURCE_NAME, props);
    context.resourceResolver().commit();

    assertNull(underTest.get(context.resourceResolver(), CONFIG_ID));
    assertFalse(underTest.store(context.resourceResolver(), CONFIG_ID, props));
  }

  @Test
  public void testStoreMultipleLevels() throws PersistenceException {
    Page level1Page;
    Page level2Page;
    Page level3Page;

    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();

    assertTrue(underTest.store(context.resourceResolver(), "/content/level1/level2/level3", props));

    level3Page = context.pageManager().getPage("/conf/content/level1/level2/level3");
    assertNotNull(level3Page);
    assertEquals(CONFIG_PAGE_TEMPLATE, level3Page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));

    level2Page = context.pageManager().getPage("/conf/content/level1/level2");
    assertNotNull(level2Page);
    assertEquals(STRUCTURE_PAGE_TEMPLATE, level2Page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));

    level1Page = context.pageManager().getPage("/conf/content/level1");
    assertNotNull(level1Page);
    assertEquals(STRUCTURE_PAGE_TEMPLATE, level1Page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));


    assertTrue(underTest.store(context.resourceResolver(), "/content/level1/level2", props));

    level3Page = context.pageManager().getPage("/conf/content/level1/level2/level3");
    assertNotNull(level3Page);
    assertEquals(CONFIG_PAGE_TEMPLATE, level3Page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));

    level2Page = context.pageManager().getPage("/conf/content/level1/level2");
    assertNotNull(level2Page);
    assertEquals(CONFIG_PAGE_TEMPLATE, level2Page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));

    level1Page = context.pageManager().getPage("/conf/content/level1");
    assertNotNull(level1Page);
    assertEquals(STRUCTURE_PAGE_TEMPLATE, level1Page.getProperties().get(NameConstants.PN_TEMPLATE, String.class));

  }

}
