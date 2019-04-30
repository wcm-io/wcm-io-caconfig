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

import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;
import static io.wcm.caconfig.extensions.persistence.testcontext.PersistenceTestUtils.writeConfigurationCollection;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;

import io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig;
import io.wcm.caconfig.extensions.persistence.impl.PagePersistenceStrategy;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ConfigDataServletDeeplyNestedTest {

  private final AemContext context = new AemContextBuilder()
      .beforeSetUp(new AemContextCallback() {
        @Override
        public void execute(@NotNull AemContext ctx) {
          // also find sling:configRef props in cq:Page/jcr:content nodes
          MockOsgi.setConfigForPid(ctx.bundleContext(), "org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy",
              "configRefResourceNames", new String[] { "jcr:content", "." });
          // AEM-specific configuration management settings
          MockOsgi.setConfigForPid(ctx.bundleContext(), "org.apache.sling.caconfig.management.impl.ConfigurationManagementSettingsImpl",
              "ignorePropertyNameRegex", new String[] { "^(jcr|cq):.+", "^" + PROPERTY_RESOURCE_TYPE + "$" },
              "configCollectionPropertiesResourceNames", new String[] { "jcr:content", "." });
        }
      })
      .plugin(CACONFIG)
      .build();

  private ConfigDataServlet underTest;

  private Page page;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(new EditorConfig());
    underTest = context.registerInjectActivateService(new ConfigDataServlet());

    // enable AEM page persistence strategy for this test
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);

    // create sample content page with config reference
    page = context.create().page("/content/mypage", null,
        "sling:configRef", "/conf/myconf");
    context.currentPage(page);
  }

  @Test
  void testDeeplyNestedConfig_WCON60_FooterConfig() throws Exception {
    MockContextAwareConfig.registerAnnotationClasses(context, FooterConfig.class);

    context.request().setParameterMap(ImmutableValueMap.of(
        RP_CONFIGNAME, FooterConfig.class.getName()));
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig',"
        + "'overridden':false,'inherited':false,'properties':"
        + "[{'name':'menu','metadata':{},"
        + "'nestedConfigCollection':{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig/jcr:content/menu','items':[]}}]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testDeeplyNestedConfig_WCON60_MenuConfig() throws Exception {
    MockContextAwareConfig.registerAnnotationClasses(context, FooterConfig.class);

    // create menu item
    writeConfigurationCollection(context, page.getPath(), FooterConfig.class.getName() + "/jcr:content/menu", ImmutableList.of(
        ImmutableValueMap.of()));

    context.request().setParameterMap(ImmutableValueMap.of(
        RP_CONFIGNAME, FooterConfig.class.getName() + "/jcr:content/menu",
        "collection", true));
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig/jcr:content/menu',"
        + "'items':[{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig/jcr:content/menu',"
        + "'collectionItemName':'item0','overridden':false,'inherited':false,"
        + "'properties':[{'name':'links','metadata':{},"
        + "'nestedConfigCollection':{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig/jcr:content/menu/item0/links','items':[]}}]}],"
        + "'newItem':{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig/jcr:content/menu','overridden':false,"
        + "'properties':[{'name':'links','metadata':{},"
        + "'nestedConfigCollection':{'configName':'io.wcm.caconfig.extensions.persistence.example.wcon60.FooterConfig/jcr:content/menu/links','items':[]}}]}}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

}
