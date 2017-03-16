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
import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy;
import io.wcm.caconfig.extensions.persistence.example.ListConfig;
import io.wcm.caconfig.extensions.persistence.example.NestedConfig;
import io.wcm.caconfig.extensions.persistence.example.SimpleConfig;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;

public class ToolsConfigPagePersistenceStrategyTest {

  @Rule
  public AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .build();

  private Page contentPage;

  @Before
  public void setUp() throws Exception {
    context.registerInjectActivateService(new AbsoluteParentContextPathStrategy(),
        "levels", new int[] { 1, 3 },
        "contextPathRegex", "^/content(/.+)$",
        "configPathPatterns", new String[] { "/conf$1", "/content$1/tools/config/jcr:content" });
    context.registerInjectActivateService(new ToolsConfigPagePersistenceStrategy(),
        "enabled", true);

    context.create().page("/content/region1");
    context.create().page("/content/region1/site1");
    context.create().page("/content/region1/site1/en");
    contentPage = context.create().page("/content/region1/site1/en/page1");
  }

  @Test
  public void testGetResource() throws Exception {
    // write config
    writeConfiguration(context, contentPage.getPath(), SimpleConfig.class.getName(),
        "stringParam", "value1",
        "intParam", 123);

    // assert storage in page in /content/*/tools/config
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertNotNull(configPage);
    ValueMap props = configPage.getContentResource("sling:configs/" + SimpleConfig.class.getName()).getValueMap();
    assertEquals("value1", props.get("stringParam", String.class));
    assertEquals((Integer)123, props.get("intParam", Integer.class));

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
  public void testListConfig() throws Exception {
    // write config
    writeConfigurationCollection(context, contentPage.getPath(), ListConfig.class.getName(), ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value1", "intParam", 123),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value2", "intParam", 234)));

    // assert storage in page in /conf
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertNotNull(configPage);
    ValueMap props1 = configPage.getContentResource("sling:configs/" + ListConfig.class.getName() + "/item0").getValueMap();
    assertEquals("value1", props1.get("stringParam", String.class));
    assertEquals((Integer)123, props1.get("intParam", Integer.class));
    ValueMap props2 = configPage.getContentResource("sling:configs/" + ListConfig.class.getName() + "/item1").getValueMap();
    assertEquals("value2", props2.get("stringParam", String.class));
    assertEquals((Integer)234, props2.get("intParam", Integer.class));

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
  public void testNestedConfig() throws Exception {
    // TODO: does not work if both are registered?
    //context.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);

    // write config
    writeConfiguration(context, contentPage.getPath(), NestedConfig.class.getName(),
        "stringParam", "value1");
    writeConfiguration(context, contentPage.getPath(), NestedConfig.class.getName() + "/subConfig",
        "stringParam", "value2",
        "intParam", 234);
    writeConfigurationCollection(context, contentPage.getPath(), NestedConfig.class.getName() + "/subListConfig", ImmutableList.of(
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value3", "intParam", 345),
        (Map<String, Object>)ImmutableMap.<String, Object>of("stringParam", "value4", "intParam", 456)));

    // assert storage in page in /conf
    Page configPage = context.pageManager().getPage("/content/region1/site1/en/tools/config");
    assertNotNull(configPage);
    ValueMap props = configPage.getContentResource("sling:configs/" + NestedConfig.class.getName()).getValueMap();
    assertEquals("value1", props.get("stringParam", String.class));

    Resource subConfigResource = configPage.getContentResource("sling:configs/" + NestedConfig.class.getName() + "/subConfig");
    assertNotNull(subConfigResource);
    ValueMap subConifgProps = subConfigResource.getValueMap();
    assertEquals("value2", subConifgProps.get("stringParam", String.class));
    assertEquals((Integer)234, subConifgProps.get("intParam", Integer.class));

    Resource subListConfigResource1 = configPage.getContentResource("sling:configs/" + NestedConfig.class.getName() + "/subListConfig/item0");
    assertNotNull(subListConfigResource1);
    ValueMap subListConfigProps1 = subListConfigResource1.getValueMap();
    assertEquals("value3", subListConfigProps1.get("stringParam", String.class));
    assertEquals((Integer)345, subListConfigProps1.get("intParam", Integer.class));
    Resource subListConfigResource2 = configPage.getContentResource("sling:configs/" + NestedConfig.class.getName() + "/subListConfig/item1");
    assertNotNull(subListConfigResource2);
    ValueMap subListConfigProps2 = subListConfigResource2.getValueMap();
    assertEquals("value4", subListConfigProps2.get("stringParam", String.class));
    assertEquals((Integer)456, subListConfigProps2.get("intParam", Integer.class));

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


}
