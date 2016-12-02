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
package io.wcm.config.core.persistence.impl;

import static io.wcm.config.core.persistence.impl.AbstractConfigPagePersistenceProvider.CONFIG_RESOURCE_NAME;
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
public class ToolsConfigPagePersistenceProviderTest {

  private static final String CONFIG_ID = "/content/site1";
  private static final String CONFIG_PAGE_TEMPLATE = "/apps/dummy/templates/config";
  private static final String STRUCTURE_PAGE_TEMPLATE = "/apps/dummy/templates/tools";

  @Rule
  public final AemContext context = new AemContext();

  private ParameterPersistenceProvider underTest;

  @Before
  public void setUp() {
    context.create().page("/content", STRUCTURE_PAGE_TEMPLATE);
    context.create().page("/content/site1", STRUCTURE_PAGE_TEMPLATE);

    underTest = context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(), ImmutableValueMap.builder()
        .put(ToolsConfigPagePersistenceProvider.PROPERTY_ENABLED, true)
        .put(ToolsConfigPagePersistenceProvider.PROPERTY_CONFIG_PAGE_TEMPLATE, CONFIG_PAGE_TEMPLATE)
        .put(ToolsConfigPagePersistenceProvider.PROPERTY_STRUCTURE_PAGE_TEMPLATE, STRUCTURE_PAGE_TEMPLATE)
        .build());
  }

  @Test
  public void testGetNoPage() {
    assertNull(underTest.get(context.resourceResolver(), CONFIG_ID));
  }

  @Test
  public void testGetPageNoConfigResource() {
    context.create().page(CONFIG_ID + "/tools", STRUCTURE_PAGE_TEMPLATE);
    context.create().page(CONFIG_ID + "/tools/config", CONFIG_PAGE_TEMPLATE);
    assertEquals(ImmutableMap.of(), underTest.get(context.resourceResolver(), CONFIG_ID));
  }

  @Test
  public void testGetStoreGetValues() throws PersistenceException {
    long currentTime = Calendar.getInstance().getTimeInMillis();

    context.create().page(CONFIG_ID + "/tools", STRUCTURE_PAGE_TEMPLATE);
    Page configPage = context.create().page(CONFIG_ID + "/tools/config", CONFIG_PAGE_TEMPLATE);

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

    assertNotNull(context.pageManager().getPage(CONFIG_ID + ToolsConfigPagePersistenceProvider.RELATIVE_CONFIG_PATH));
  }

  @Test
  public void testNoParentPageStore() throws PersistenceException {
    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();
    assertTrue(underTest.store(context.resourceResolver(), "/content/site2", props));
    assertNotNull(context.pageManager().getPage("/content/site2" + ToolsConfigPagePersistenceProvider.RELATIVE_CONFIG_PATH));
  }

  @Test
  public void testDisabled() throws PersistenceException {
    underTest = context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(), ImmutableValueMap.builder()
        .put(ToolsConfigPagePersistenceProvider.PROPERTY_ENABLED, false)
        .build());

    context.create().page(CONFIG_ID + "/tools", STRUCTURE_PAGE_TEMPLATE);
    Page configPage = context.create().page(CONFIG_ID + "/tools/config", CONFIG_PAGE_TEMPLATE);

    ValueMap props = ImmutableValueMap.builder()
        .put("props1", "value1")
        .put("props2", 55L)
        .build();
    context.create().resource(configPage.getContentResource() + "/" + CONFIG_RESOURCE_NAME, props);
    context.resourceResolver().commit();

    assertNull(underTest.get(context.resourceResolver(), CONFIG_ID));
    assertFalse(underTest.store(context.resourceResolver(), CONFIG_ID, props));
  }

}
