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
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import io.wcm.caconfig.application.impl.ApplicationFinderImpl;
import io.wcm.caconfig.application.impl.ApplicationImplementationPicker;
import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.core.management.ParameterPersistence;
import io.wcm.config.core.management.ParameterPersistenceData;
import io.wcm.config.core.management.impl.ConfigurationFinderImpl;
import io.wcm.config.core.management.impl.ParameterOverrideImpl;
import io.wcm.config.core.management.impl.ParameterPersistenceImpl;
import io.wcm.config.core.management.impl.ParameterResolverImpl;
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
  private static final Parameter<String> PROP_4 = ParameterBuilder.create("prop4", String.class, APP_ID)
      .defaultOsgiConfigProperty(SampleOsgiConfiguration.class.getName() + ":prop4").build();

  @Rule
  public final AemContext context = new AemContext();

  @Before
  public void setUp() throws Exception {

    // app-specific services
    context.registerService(SampleOsgiConfiguration.class, new SampleOsgiConfiguration(),
        ImmutableValueMap.of("prop4", "value4-osgi"));
    context.registerService(ConfigurationFinderStrategy.class, new SampleConfigurationFinderStrategy());
    context.registerService(ParameterProvider.class, new SampleParameterProvider());

    // persistence providers
    context.registerInjectActivateService(new ToolsConfigPagePersistenceProvider(),
        ImmutableValueMap.of("enabled", true));

    // override providers
    context.registerInjectActivateService(new SystemPropertyOverrideProvider(),
        ImmutableValueMap.of("enabled", true));

    // management services
    context.registerInjectActivateService(new ApplicationFinderImpl());
    context.registerInjectActivateService(new ParameterOverrideImpl());
    context.registerInjectActivateService(new ParameterPersistenceImpl());
    context.registerInjectActivateService(new ParameterResolverImpl());
    context.registerInjectActivateService(new ConfigurationFinderImpl());

    // adapter factory
    context.registerInjectActivateService(new ConfigurationAdapterFactory());

    // models implementation picker
    context.registerInjectActivateService(new ApplicationImplementationPicker());

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
    assertEquals("value4-osgi", config.get(PROP_4));
  }

  @Test
  public void testWriteReadConfig() throws PersistenceException {
    ParameterPersistence persistence = context.getService(ParameterPersistence.class);
    persistence.storeData(context.resourceResolver(), CONFIG_ID,
        new ParameterPersistenceData(ImmutableValueMap.of(PROP_3.getName(), "value3-new"), ImmutableSortedSet.<String>of()),
        true);

    Resource resource = context.request().getResource();
    Configuration config = resource.adaptTo(Configuration.class);

    assertNotNull(config);
    assertEquals("value1-l3", config.get(PROP_1));
    assertEquals("value2-l2", config.get(PROP_2));
    assertEquals("value3-new", config.get(PROP_3));
    assertEquals("value4-osgi", config.get(PROP_4));
  }


  private static class SampleParameterProvider implements ParameterProvider {

    private static final Set<Parameter<?>> PARAMS = ImmutableSet.<Parameter<?>>builder()
        .add(PROP_1)
        .add(PROP_2)
        .add(PROP_3)
        .add(PROP_4).build();

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

  @Component(immediate = true, metatype = true)
  @Property(name = "prop4")
  private static class SampleOsgiConfiguration {
    // no methods
  }

}
