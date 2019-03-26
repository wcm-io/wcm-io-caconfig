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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.hamcrest.ResourceMatchers;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.caconfig.extensions.persistence.example.ListConfig;
import io.wcm.caconfig.extensions.persistence.example.ListNestedConfig;
import io.wcm.caconfig.extensions.persistence.example.NestedConfig;
import io.wcm.caconfig.extensions.persistence.example.SimpleConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@SuppressWarnings("null")
class PagePersistenceStrategyTest {

  final AemContext context = new AemContextBuilder()
      .beforeSetUp(new AemContextCallback() {
        @Override
        public void execute(AemContext ctx) {
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

  private Page contentPage;

  @BeforeEach
  void setUp() throws Exception {
    context.create().resource("/conf");
    contentPage = context.create().page("/content/test/site1", "/apps/app1/templates/template1",
        ImmutableMap.<String, Object>of("sling:configRef", "/conf/test/site1"));
  }

  @Test
  void testSimpleConfig() throws Exception {
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);

    // write config
    writeConfiguration(context, contentPage.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    // assert storage in page in /conf
    Page configPage = context.pageManager().getPage("/conf/test/site1/sling:configs/" + SimpleConfig.class.getName());
    assertThat(configPage.getContentResource(), ResourceMatchers.props("stringParam", "value1", "intParam", 123));

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
  void testSimpleConfig_Disabled() throws Exception {
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", false);

    // write config
    writeConfiguration(context, contentPage.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    // assert storage in page in /conf
    Page configPage = context.pageManager().getPage("/conf/test/site1/sling:configs/" + SimpleConfig.class.getName());
    assertNull(configPage);

    // read config
    SimpleConfig config = contentPage.getContentResource().adaptTo(ConfigurationBuilder.class).as(SimpleConfig.class);
    assertEquals("value1", config.stringParam());
    assertEquals(123, config.intParam());
  }

  @Test
  void testListConfig() throws Exception {
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);

    // write config
    writeConfigurationCollection(context, contentPage.getPath(), ListConfig.class.getName(), ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value1", "intParam", 123),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value2", "intParam", 234)
    ),
        ImmutableMap.<String, Object>of("sling:configCollectionInherit", true));

    // assert storage in page in /conf
    Page parentPage = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListConfig.class.getName());
    assertNotNull(parentPage);
    assertTrue(parentPage.getContentResource().getValueMap().get("sling:configCollectionInherit", false));

    Page configPage1 = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListConfig.class.getName() + "/item0");
    assertThat(configPage1.getContentResource(), ResourceMatchers.props("stringParam", "value1", "intParam", 123));

    Page configPage2 = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListConfig.class.getName() + "/item1");
    assertThat(configPage2.getContentResource(), ResourceMatchers.props("stringParam", "value2", "intParam", 234));

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
    writeConfigurationCollection(context, contentPage.getPath(), ListNestedConfig.class.getName() + "/item0/jcr:content/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value11"),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value12")));
    writeConfigurationCollection(context, contentPage.getPath(), ListNestedConfig.class.getName() + "/item1/jcr:content/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value21")));

    // assert storage in page in /conf
    assertNotNull(context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListNestedConfig.class.getName()));

    Page configPage1 = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListNestedConfig.class.getName() + "/item0");
    assertThat(configPage1.getContentResource(), ResourceMatchers.props("stringParam", "value1", "intParam", 123));
    assertThat(configPage1.getContentResource("subListConfig/item0"), ResourceMatchers.props("stringParam", "value11"));
    assertThat(configPage1.getContentResource("subListConfig/item1"), ResourceMatchers.props("stringParam", "value12"));
    assertNull(context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListNestedConfig.class.getName() + "/item0/jcr:content/subListConfig"));

    Page configPage2 = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListNestedConfig.class.getName() + "/item1");
    assertThat(configPage2.getContentResource(), ResourceMatchers.props("stringParam", "value2", "intParam", 234));
    assertThat(configPage2.getContentResource("subListConfig/item0"), ResourceMatchers.props("stringParam", "value21"));

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
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);

    // write config
    writeConfiguration(context, contentPage.getPath(), NestedConfig.class.getName(),
        "stringParam", "value1");
    writeConfiguration(context, contentPage.getPath(), NestedConfig.class.getName() + "/jcr:content/subConfig",
        "stringParam", "value2",
        "intParam", 234);
    writeConfigurationCollection(context, contentPage.getPath(), NestedConfig.class.getName() + "/jcr:content/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value3", "intParam", 345),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value4", "intParam", 456)));

    // assert storage in page in /conf
    Page configPage = context.pageManager().getPage("/conf/test/site1/sling:configs/" + NestedConfig.class.getName());
    assertThat(configPage.getContentResource(), ResourceMatchers.props("stringParam", "value1"));
    assertThat(configPage.getContentResource("subConfig"), ResourceMatchers.props("stringParam", "value2", "intParam", 234));
    assertThat(configPage.getContentResource("subListConfig/item0"), ResourceMatchers.props("stringParam", "value3", "intParam", 345));
    assertThat(configPage.getContentResource("subListConfig/item1"), ResourceMatchers.props("stringParam", "value4", "intParam", 456));

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
  void testSimpleConfig_ResourceType() throws Exception {
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true,
        "resourceType", "app1/components/page/config");

    // write config
    writeConfiguration(context, contentPage.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    // assert storage in page in /conf
    Page configPage = context.pageManager().getPage("/conf/test/site1/sling:configs/" + SimpleConfig.class.getName());
    assertThat(configPage.getContentResource(), ResourceMatchers.props("stringParam", "value1", "intParam", 123,
        PROPERTY_RESOURCE_TYPE, "app1/components/page/config"));

  }

  @Test
  void testListConfig_ResourceType() throws Exception {
    context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true,
        "resourceType", "app1/components/page/config");

    // write config
    writeConfigurationCollection(context, contentPage.getPath(), ListConfig.class.getName(), ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value1", "intParam", 123),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value2", "intParam", 234)),
        ImmutableMap.<String, Object>of("sling:configCollectionInherit", true));

    // assert storage in page in /conf
    Page parentPage = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListConfig.class.getName());
    assertNotNull(parentPage);
    assertTrue(parentPage.getContentResource().getValueMap().get("sling:configCollectionInherit", false));
    assertThat(parentPage.getContentResource(), ResourceMatchers.props(PROPERTY_RESOURCE_TYPE, "app1/components/page/config"));

    Page configPage1 = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListConfig.class.getName() + "/item0");
    assertThat(configPage1.getContentResource(), ResourceMatchers.props("stringParam", "value1", "intParam", 123));
    assertThat(configPage1.getContentResource(), ResourceMatchers.props(PROPERTY_RESOURCE_TYPE, "app1/components/page/config"));

    Page configPage2 = context.pageManager().getPage("/conf/test/site1/sling:configs/" + ListConfig.class.getName() + "/item1");
    assertThat(configPage2.getContentResource(), ResourceMatchers.props("stringParam", "value2", "intParam", 234));
    assertThat(configPage2.getContentResource(), ResourceMatchers.props(PROPERTY_RESOURCE_TYPE, "app1/components/page/config"));

  }

}
