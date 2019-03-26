/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.caconfig.extensions.persistence.impl;

import static io.wcm.caconfig.extensions.persistence.impl.TestUtils.writeConfiguration;
import static io.wcm.caconfig.extensions.persistence.impl.TestUtils.writeConfigurationCollection;
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.hamcrest.ResourceMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy;
import io.wcm.caconfig.extensions.persistence.example.ListConfig;
import io.wcm.caconfig.extensions.persistence.example.ListNestedConfig;
import io.wcm.caconfig.extensions.persistence.example.NestedConfig;
import io.wcm.caconfig.extensions.persistence.example.SimpleConfig;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class ToolsConfigPagePersistenceStrategyTest {

  final AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .build();

  private Page contentPage;

  @BeforeEach
  void setUp() throws Exception {
    context.registerInjectActivateService(new AbsoluteParentContextPathStrategy(),
        "levels", new int[] { 1, 3 },
        "contextPathRegex", "^/content(/.+)$",
        "configPathPatterns", new String[] { "/conf$1", "/content$1/tools/config/jcr:content" });
    context.registerInjectActivateService(new ToolsConfigPagePersistenceStrategy(),
        "enabled", true,
        "configPageTemplate", "/apps/app1/templates/configEditor",
        "structurePageTemplate", "/apps/app1/templates/structurePage");

    context.create().resource("/apps/app1/templates/configEditor/jcr:content",
        PROPERTY_RESOURCE_TYPE, "app1/components/page/configEditor");

    context.create().page("/content/region1");
    context.create().page("/content/region1/site1");
    context.create().page("/content/region1/site1/en");
    contentPage = context.create().page("/content/region1/site1/en/page1");
  }

  @Test
  void testSimpleConfig() throws Exception {
    // write config
    writeConfiguration(context, contentPage.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    // assert storage in page in /content/*/tools/config
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertThat(configPage.getContentResource(), ResourceMatchers.props(
        NameConstants.PN_TEMPLATE, "/apps/app1/templates/configEditor",
        NameConstants.PN_TITLE, "config",
        PROPERTY_RESOURCE_TYPE, "app1/components/page/configEditor"));
    assertThat(configPage.getContentResource("sling:configs/" + SimpleConfig.class.getName()), ResourceMatchers.props(
        "stringParam", "value1",
        "intParam", 123));

    Page toolsPage = context.pageManager().getPage("/content/region1/site1/en/tools");
    assertThat(toolsPage.getContentResource(), ResourceMatchers.props(
        NameConstants.PN_TEMPLATE, "/apps/app1/templates/structurePage",
        NameConstants.PN_TITLE, "tools",
        PROPERTY_RESOURCE_TYPE, null));

    // read config
    SimpleConfig config = contentPage.getContentResource().adaptTo(ConfigurationBuilder.class).as(SimpleConfig.class);
    assertEquals("value1", config.stringParam());
    assertEquals(123, config.intParam());

    // delete
    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    configManager.deleteConfiguration(contentPage.getContentResource(), SimpleConfig.class.getName());
    config = contentPage.getContentResource().adaptTo(ConfigurationBuilder.class).as(SimpleConfig.class);
    assertNull(config.stringParam());
    assertEquals(5, config.intParam());
  }

  @Test
  void testListConfig() throws Exception {
    // write config
    writeConfigurationCollection(context, contentPage.getPath(), ListConfig.class.getName(), ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value1", "intParam", 123),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value2", "intParam", 234)));

    // assert storage in page in /content/*/tools/config
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertThat(configPage.getContentResource(), ResourceMatchers.props(
        NameConstants.PN_TEMPLATE, "/apps/app1/templates/configEditor",
        NameConstants.PN_TITLE, "config",
        PROPERTY_RESOURCE_TYPE, "app1/components/page/configEditor"));
    assertThat(configPage.getContentResource("sling:configs/" + ListConfig.class.getName() + "/item0"), ResourceMatchers.props(
        "stringParam", "value1",
        "intParam", 123));
    assertThat(configPage.getContentResource("sling:configs/" + ListConfig.class.getName() + "/item1"), ResourceMatchers.props(
        "stringParam", "value2",
        "intParam", 234));

    Page toolsPage = context.pageManager().getPage("/content/region1/site1/en/tools");
    assertThat(toolsPage.getContentResource(), ResourceMatchers.props(
        NameConstants.PN_TEMPLATE, "/apps/app1/templates/structurePage",
        NameConstants.PN_TITLE, "tools",
        PROPERTY_RESOURCE_TYPE, null));

    // read config
    List<ListConfig> configs = ImmutableList.copyOf(contentPage.getContentResource().adaptTo(ConfigurationBuilder.class).asCollection(ListConfig.class));
    assertEquals(2, configs.size());
    ListConfig config1 = configs.get(0);
    assertEquals("value1", config1.stringParam());
    assertEquals(123, config1.intParam());
    ListConfig config2 = configs.get(1);
    assertEquals("value2", config2.stringParam());
    assertEquals(234, config2.intParam());
  }

  @Test
  void testListConfig_Nested() throws Exception {
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);

    // write config
    writeConfigurationCollection(context, contentPage.getPath(), ListNestedConfig.class.getName(), ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value1", "intParam", 123),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value2", "intParam", 234)));
    writeConfigurationCollection(context, contentPage.getPath(), ListNestedConfig.class.getName() + "/item0/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value11"),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value12")));
    writeConfigurationCollection(context, contentPage.getPath(), ListNestedConfig.class.getName() + "/item1/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value21")));

    // assert storage in page in /content/*/tools/config
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertThat(configPage.getContentResource(), ResourceMatchers.props(
        NameConstants.PN_TEMPLATE, "/apps/app1/templates/configEditor",
        NameConstants.PN_TITLE, "config",
        PROPERTY_RESOURCE_TYPE, "app1/components/page/configEditor"));

    assertThat(configPage.getContentResource("sling:configs/" + ListNestedConfig.class.getName() + "/item0"),
        ResourceMatchers.props("stringParam", "value1", "intParam", 123));
    assertThat(configPage.getContentResource("sling:configs/" + ListNestedConfig.class.getName() + "/item0/subListConfig/item0"),
        ResourceMatchers.props("stringParam", "value11"));
    assertThat(configPage.getContentResource("sling:configs/" + ListNestedConfig.class.getName() + "/item0/subListConfig/item1"),
        ResourceMatchers.props("stringParam", "value12"));

    assertThat(configPage.getContentResource("sling:configs/" + ListNestedConfig.class.getName() + "/item1"),
        ResourceMatchers.props("stringParam", "value2"));
    assertThat(configPage.getContentResource("sling:configs/" + ListNestedConfig.class.getName() + "/item1/subListConfig/item0"),
        ResourceMatchers.props("stringParam", "value21"));

    // read config
    List<ListNestedConfig> configs = ImmutableList.copyOf(contentPage.getContentResource().adaptTo(ConfigurationBuilder.class)
        .asCollection(ListNestedConfig.class));
    assertEquals(2, configs.size());

    ListNestedConfig config1 = configs.get(0);
    assertEquals("value1", config1.stringParam());
    assertEquals(123, config1.intParam());
    assertEquals(2, config1.subListConfig().length);
    assertEquals("value11", config1.subListConfig()[0].stringParam());
    assertEquals("value12", config1.subListConfig()[1].stringParam());

    ListNestedConfig config2 = configs.get(1);
    assertEquals("value2", config2.stringParam());
    assertEquals(234, config2.intParam());
    assertEquals(1, config2.subListConfig().length);
    assertEquals("value21", config2.subListConfig()[0].stringParam());

    // update config collection items
    writeConfigurationCollection(context, contentPage.getPath(), ListNestedConfig.class.getName(), ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value1-new", "intParam", 123),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value2-new", "intParam", 234),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value3-new", "intParam", 345)));

    // read config
    configs = ImmutableList.copyOf(contentPage.getContentResource().adaptTo(ConfigurationBuilder.class)
        .asCollection(ListNestedConfig.class));
    assertEquals(3, configs.size());

    config1 = configs.get(0);
    assertEquals("value1-new", config1.stringParam());
    assertEquals(123, config1.intParam());
    assertEquals(2, config1.subListConfig().length);
    assertEquals("value11", config1.subListConfig()[0].stringParam());
    assertEquals("value12", config1.subListConfig()[1].stringParam());

    config2 = configs.get(1);
    assertEquals("value2-new", config2.stringParam());
    assertEquals(234, config2.intParam());
    assertEquals(1, config2.subListConfig().length);
    assertEquals("value21", config2.subListConfig()[0].stringParam());

    ListNestedConfig config3 = configs.get(2);
    assertEquals("value3-new", config3.stringParam());
    assertEquals(345, config3.intParam());
    assertEquals(0, config3.subListConfig().length);

  }

  @Test
  void testNestedConfig() throws Exception {
    // write config
    writeConfiguration(context, contentPage.getPath(), NestedConfig.class.getName(),
        "stringParam", "value1");
    writeConfiguration(context, contentPage.getPath(), NestedConfig.class.getName() + "/subConfig",
        "stringParam", "value2",
        "intParam", 234);
    writeConfigurationCollection(context, contentPage.getPath(), NestedConfig.class.getName() + "/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value3", "intParam", 345),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value4", "intParam", 456)));

    // assert storage in page in /content/*/tools/config
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertThat(configPage.getContentResource("sling:configs/" + NestedConfig.class.getName()), ResourceMatchers.props(
        "stringParam", "value1"));
    assertThat(configPage.getContentResource("sling:configs/" + NestedConfig.class.getName() + "/subConfig"), ResourceMatchers.props(
        "stringParam", "value2",
        "intParam", 234));
    assertThat(configPage.getContentResource("sling:configs/" + NestedConfig.class.getName() + "/subListConfig/item0"), ResourceMatchers.props(
        "stringParam", "value3",
        "intParam", 345));
    assertThat(configPage.getContentResource("sling:configs/" + NestedConfig.class.getName() + "/subListConfig/item1"), ResourceMatchers.props(
        "stringParam", "value4",
        "intParam", 456));

    // read config
    NestedConfig config = contentPage.getContentResource().adaptTo(ConfigurationBuilder.class).as(NestedConfig.class);
    assertEquals("value1", config.stringParam());

    SimpleConfig subConfig = config.subConfig();
    assertEquals("value2", subConfig.stringParam());
    assertEquals(234, subConfig.intParam());

    List<ListConfig> subListConfigs = ImmutableList.copyOf(config.subListConfig());
    assertEquals(2, subListConfigs.size());
    ListConfig subListConfig1 = subListConfigs.get(0);
    assertEquals("value3", subListConfig1.stringParam());
    assertEquals(345, subListConfig1.intParam());
    ListConfig subListConfig2 = subListConfigs.get(1);
    assertEquals("value4", subListConfig2.stringParam());
    assertEquals(456, subListConfig2.intParam());
  }

  @Test
  void testSimpleConfigWithCQLastModified() throws Exception {
    context.create().page("/content/region2");
    context.create().page("/content/region2/site2");
    context.create().page("/content/region2/site2/en");
    context.create().page("/content/region2/site2/en/tools");

    Page contentPageWithCQLastModified = context.create().page("/content/region2/site2/en/page2");

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.HOUR, -1);
    context.create().page("/content/region2/site2/en/tools/config", "/apps/app1/templates/configEditor", ImmutableValueMap.builder()
        .put(NameConstants.PN_PAGE_LAST_MOD, cal)
        .build());

    // write config
    writeConfiguration(context, contentPageWithCQLastModified.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    Page configPage = context.pageManager().getPage("/content/region2/site2/en/tools/config");

    // check that cq:lastModified is updated while saving data
    ValueMap properties = configPage.getContentResource().getValueMap();
    assertTrue(properties.containsKey(NameConstants.PN_PAGE_LAST_MOD));
    Calendar changedDate = properties.get(NameConstants.PN_PAGE_LAST_MOD, Calendar.class);
    assertTrue(cal.before(changedDate));

    // assert storage in page in /content/*/tools/config
    assertThat(configPage.getContentResource("sling:configs/" + SimpleConfig.class.getName()), ResourceMatchers.props(
        "stringParam", "value1",
        "intParam", 123));

    // read config
    SimpleConfig config = contentPageWithCQLastModified.getContentResource().adaptTo(ConfigurationBuilder.class).as(SimpleConfig.class);
    assertEquals("value1", config.stringParam());
    assertEquals(123, config.intParam());

    // delete
    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    configManager.deleteConfiguration(contentPageWithCQLastModified.getContentResource(), SimpleConfig.class.getName());
    config = contentPageWithCQLastModified.getContentResource().adaptTo(ConfigurationBuilder.class).as(SimpleConfig.class);
    assertNull(config.stringParam());
    assertEquals(5, config.intParam());
  }

}
