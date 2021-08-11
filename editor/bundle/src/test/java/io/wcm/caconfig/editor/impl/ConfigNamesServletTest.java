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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import io.wcm.caconfig.editor.ConfigurationEditorFilter;
import io.wcm.sling.commons.caservice.impl.ContextAwareServiceResolverImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ConfigNamesServletTest {

  private final AemContext context = new AemContext();

  @Mock
  private ConfigurationManager configManager;
  @Mock
  private ConfigurationResourceResolver configurationResourceResolver;
  @Mock
  private ConfigurationData configData;

  @BeforeEach
  void setUp() {
    context.currentResource(context.create().resource("/content/test"));

    ConfigurationMetadata metadata1 = new ConfigurationMetadata("name1", ImmutableList.<PropertyMetadata<?>>of(), false)
        .label("B-label1")
        .description("desc1");
    ConfigurationMetadata metadata2 = new ConfigurationMetadata("name2", ImmutableList.<PropertyMetadata<?>>of(), true)
        .label("A-label2");
    ConfigurationMetadata metadata3 = new ConfigurationMetadata("name3", ImmutableList.<PropertyMetadata<?>>of(), false)
        .label("C-label3");

    when(configManager.getConfigurationNames()).thenReturn(ImmutableSortedSet.of("name1", "name2", "name3"));
    when(configManager.getConfigurationMetadata("name1")).thenReturn(metadata1);
    when(configManager.getConfigurationMetadata("name2")).thenReturn(metadata2);
    when(configManager.getConfigurationMetadata("name3")).thenReturn(metadata3);

    when(configManager.getConfiguration(context.currentResource(), "name1")).thenReturn(configData);
    ConfigurationCollectionData configCollectionData = mock(ConfigurationCollectionData.class);
    when(configCollectionData.getItems()).thenReturn(ImmutableList.of(configData));
    when(configManager.getConfigurationCollection(context.currentResource(), "name2")).thenReturn(configCollectionData);

    when(configurationResourceResolver.getContextPath(context.currentResource())).thenReturn("/context/path");

    context.registerService(ConfigurationManager.class, configManager);
    context.registerService(ConfigurationResourceResolver.class, configurationResourceResolver);
    context.registerInjectActivateService(new EditorConfig());
  }

  @Test
  void testResponse() throws Exception {
    ConfigNamesServlet underTest = context.registerInjectActivateService(new ConfigNamesServlet());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection=true,exists:true,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithInheritedOverriddenExists() throws Exception {

    when(configData.getResourcePath()).thenReturn("/path");
    when(configData.isInherited()).thenReturn(true);
    when(configData.isOverridden()).thenReturn(true);

    ConfigNamesServlet underTest = context.registerInjectActivateService(new ConfigNamesServlet());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection=true,exists:true,inherited:true,overridden:true,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',collection:false,exists:true,inherited:true,overridden:true,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithOverriddenOnly() throws Exception {

    when(configData.getResourcePath()).thenReturn(null);
    when(configData.isInherited()).thenReturn(false);
    when(configData.isOverridden()).thenReturn(true);

    ConfigNamesServlet underTest = context.registerInjectActivateService(new ConfigNamesServlet());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection=true,exists:true,inherited:false,overridden:true,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',collection:false,exists:true,inherited:false,overridden:true,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testResponseWithFiltering() throws Exception {
    context.registerService(ConfigurationEditorFilter.class, new ConfigurationEditorFilter() {
      @Override
      public boolean allowAdd(@NotNull String configName) {
        return !StringUtils.equals(configName, "name3");
      }
    });
    context.registerInjectActivateService(new ContextAwareServiceResolverImpl());
    context.registerInjectActivateService(new ConfigurationEditorFilterService());
    ConfigNamesServlet underTest = context.registerInjectActivateService(new ConfigNamesServlet());

    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{contextPath:'/context/path',configNames:["
        + "{configName:'name2',label:'A-label2',collection=true,exists:true,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name1',label:'B-label1',description:'desc1',collection:false,exists:false,inherited:false,overridden:false,allowAdd:true},"
        + "{configName:'name3',label:'C-label3',collection:false,exists:false,inherited:false,overridden:false,allowAdd:false}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

}
