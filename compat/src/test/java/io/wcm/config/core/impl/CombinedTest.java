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
package io.wcm.config.core.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationResolver;
import org.apache.sling.caconfig.impl.ConfigurationInheritanceStrategyMultiplexer;
import org.apache.sling.caconfig.impl.ConfigurationResolverImpl;
import org.apache.sling.caconfig.impl.def.DefaultConfigurationInheritanceStrategy;
import org.apache.sling.caconfig.impl.def.DefaultConfigurationPersistenceStrategy;
import org.apache.sling.caconfig.impl.metadata.ConfigurationMetadataProviderMultiplexer;
import org.apache.sling.caconfig.impl.override.ConfigurationOverrideManager;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.impl.ConfigurationManagerImpl;
import org.apache.sling.caconfig.management.impl.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.caconfig.management.impl.ContextPathStrategyMultiplexerImpl;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.caconfig.resource.impl.ConfigurationResourceResolverImpl;
import org.apache.sling.caconfig.resource.impl.ConfigurationResourceResolvingStrategyMultiplexer;
import org.apache.sling.caconfig.resource.impl.def.DefaultConfigurationResourceResolvingStrategy;
import org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.text.Text;
import com.google.common.collect.ImmutableSet;

import io.wcm.caconfig.application.impl.ApplicationAdapterFactory;
import io.wcm.caconfig.application.impl.ApplicationFinderImpl;
import io.wcm.caconfig.application.impl.ApplicationImplementationPicker;
import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.core.override.impl.SystemPropertyOverrideProvider;
import io.wcm.config.core.persistence.impl.ToolsConfigPagePersistenceProvider;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

/**
 * Test all configuration services in combination.
 */
public class CombinedTest {

  private static final String APP_ID = "/apps/app1";
  private static final String CONFIG_ID = "/content/region1/site1/en";

  private static final Parameter<String> PROP_1 = ParameterBuilder.create("prop1", String.class, APP_ID).build();
  private static final Parameter<String> PROP_2 = ParameterBuilder.create("prop2", String.class, APP_ID).build();
  private static final Parameter<String> PROP_3 = ParameterBuilder.create("prop3", String.class, APP_ID).build();

  @Rule
  public final AemContext context = new AemContext();

  @Before
  public void setUp() throws Exception {
    registerConfigurationResolver(context);
    context.registerInjectActivateService(new ConfigurationMetadataProviderMultiplexer());
    context.registerInjectActivateService(new ConfigurationManagerImpl());

    // app-specific services
    context.registerService(ConfigurationFinderStrategy.class, new SampleConfigurationFinderStrategy());
    context.registerService(ParameterProvider.class, new SampleParameterProvider());

    // application detection
    context.registerInjectActivateService(new ApplicationFinderImpl());
    context.registerInjectActivateService(new ApplicationAdapterFactory());
    context.registerInjectActivateService(new ApplicationImplementationPicker());
    context.registerInjectActivateService(new io.wcm.config.core.impl.application.ApplicationImplementationPicker());

    // bridge services
    context.registerInjectActivateService(new ConfigurationFinderStrategyBridge());
    context.registerInjectActivateService(new ParameterOverrideProviderBridge());
    context.registerInjectActivateService(new ParameterProviderBridge());
    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        "enabled", true);

    // override providers
    context.registerInjectActivateService(new SystemPropertyOverrideProvider(),
        "enabled", true);

    // adapter factory
    context.registerInjectActivateService(new ConfigurationAdapterFactory());

    // mount sample content
    context.load().json("/combined-test-content.json", "/content");
    context.currentPage(CONFIG_ID);
  }

  @Test
  public void testConfigWithInheritance() {
    Resource resource = context.request().getResource();
    Configuration config = resource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertEquals("value1-l3", config.get(PROP_1));
    assertEquals("value2-l2", config.get(PROP_2));
    assertEquals("value3-l1", config.get(PROP_3));
  }

  @Test
  public void testWriteReadConfig() {
    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    Resource contextResource = context.resourceResolver().getResource(CONFIG_ID);
    configManager.persistConfiguration(contextResource, ParameterProviderBridge.DEFAULT_CONFIG_NAME,
        new ConfigurationPersistData(ImmutableValueMap.of(
            PROP_3.getName(), "value3-new")));

    Resource resource = context.request().getResource();
    Configuration config = resource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertEquals("value1-l2", config.get(PROP_1));
    assertEquals("value2-l2", config.get(PROP_2));
    assertEquals("value3-new", config.get(PROP_3));
  }

  private static class SampleParameterProvider implements ParameterProvider {

    private static final Set<Parameter<?>> PARAMS = ImmutableSet.<Parameter<?>>builder()
        .add(PROP_1)
        .add(PROP_2)
        .add(PROP_3)
        .build();

    @Override
    public Set<Parameter<?>> getParameters() {
      return PARAMS;
    }

  }

  private static class SampleConfigurationFinderStrategy implements ConfigurationFinderStrategy {

    @Override
    public String getApplicationId() {
      return "sample";
    }

    @Override
    public Iterator<String> findConfigurationIds(Resource resource) {
      List<String> configurationIds = new ArrayList<>();
      addAbsoluteParent(configurationIds, resource, 3);
      addAbsoluteParent(configurationIds, resource, 2);
      addAbsoluteParent(configurationIds, resource, 1);
      return configurationIds.iterator();
    }

    private void addAbsoluteParent(List<String> configurationIds, Resource resource, int absoluteParent) {
      String configurationId = Text.getAbsoluteParent(resource.getPath(), absoluteParent);
      if (StringUtils.isNotEmpty(configurationId)) {
        configurationIds.add(configurationId);
      }
    }

  }


  /**
   * Register all services for {@link ConfigurationResourceResolver}.
   * @param context Sling context
   */
  private static ConfigurationResourceResolver registerConfigurationResourceResolver(AemContext context) {
    context.registerInjectActivateService(new DefaultContextPathStrategy());
    context.registerInjectActivateService(new ContextPathStrategyMultiplexerImpl());
    context.registerInjectActivateService(new DefaultConfigurationResourceResolvingStrategy());
    context.registerInjectActivateService(new ConfigurationResourceResolvingStrategyMultiplexer());
    return context.registerInjectActivateService(new ConfigurationResourceResolverImpl());
  }

  /**
   * Register all services for {@link ConfigurationResolver}.
   * @param context Sling context
   */
  private static ConfigurationResolver registerConfigurationResolver(AemContext context) {
    registerConfigurationResourceResolver(context);
    context.registerInjectActivateService(new DefaultConfigurationPersistenceStrategy());
    context.registerInjectActivateService(new ConfigurationPersistenceStrategyMultiplexer());
    context.registerInjectActivateService(new DefaultConfigurationInheritanceStrategy());
    context.registerInjectActivateService(new ConfigurationInheritanceStrategyMultiplexer());
    context.registerInjectActivateService(new ConfigurationOverrideManager());
    return context.registerInjectActivateService(new ConfigurationResolverImpl());
  }

}
