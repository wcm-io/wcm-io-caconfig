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
package io.wcm.caconfig.editor.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class ConfigNamesServletTest {

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private ConfigurationManager configManager;
  @Mock
  private ConfigurationData configData;

  private ConfigNamesServlet underTest;

  @Before
  public void setUp() {
    ConfigurationMetadata metadata1 = new ConfigurationMetadata("name1");
    metadata1.setLabel("label1");
    metadata1.setDescription("desc1");
    ConfigurationMetadata metadata2 = new ConfigurationMetadata("name2");
    metadata2.setCollection(true);
    ConfigurationMetadata metadata3 = new ConfigurationMetadata("name3");

    when(configManager.getConfigurationNames()).thenReturn(ImmutableSortedSet.of("name1", "name2", "name3"));
    when(configManager.getConfigurationMetadata("name1")).thenReturn(metadata1);
    when(configManager.getConfigurationMetadata("name2")).thenReturn(metadata2);
    when(configManager.getConfigurationMetadata("name3")).thenReturn(metadata3);

    when(configData.getResourcePath()).thenReturn("/path");

    when(configManager.getConfiguration(context.currentResource(), "name1")).thenReturn(configData);
    ConfigurationCollectionData configCollectionData = mock(ConfigurationCollectionData.class);
    when(configCollectionData.getItems()).thenReturn(ImmutableList.of(configData));
    when(configManager.getConfigurationCollection(context.currentResource(), "name2")).thenReturn(configCollectionData);

    context.registerService(ConfigurationManager.class, configManager);
    underTest = context.registerInjectActivateService(new ConfigNamesServlet());
  }

  @Test
  public void testResponse() throws Exception {
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "["
        + "{configName:'name1',label:'label1',description:'desc1',collection:false,exists:true},"
        + "{configName:'name2',collection=true,exists:true},"
        + "{configName:'name3',collection:false,exists:false}"
        + "]";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

}
