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
package io.wcm.caconfig.extensions.references.impl;

import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.caconfig.impl.metadata.AnnotationClassParser;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationMetadataProvider;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;
import com.google.common.collect.ImmutableMap;

import io.wcm.caconfig.extensions.persistence.impl.PagePersistenceStrategy;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

public class ConfigurationReferenceProviderTest {

  @Rule
  public AemContext aemContext = new AemContextBuilder()
      .beforeSetUp(new AemContextCallback() {
        @Override
        public void execute(AemContext ctx) {
          // also find sling:configRef props in cq:Page/jcr:content nodes
          MockOsgi.setConfigForPid(ctx.bundleContext(), "org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy",
              "configRefResourceNames", new String[] { "jcr:content", "." });
        }
      })
      .plugin(CACONFIG)
      .build();

  private static final ValueMap CONFIGURATION_A = new ValueMapDecorator(ImmutableMap.of("key", "foo"));
  private static final ValueMap CONFIGURATION_B = new ValueMapDecorator(ImmutableMap.of("key", "bar"));


  @Before
  public void setup() {
    aemContext.create().resource("/conf");
    Page page1 = aemContext.create().page("/content/site1/page", "pagetemplate", ImmutableMap.of("sling:configRef", "/conf/test/site1"));
    Page page2 = aemContext.create().page("/content/site2/page", "pagetemplate", ImmutableMap.of("sling:configRef", "/conf/test/site2"));

    TestConfigurationMetadataProvider metadataProvider = new TestConfigurationMetadataProvider();
    metadataProvider.addConfigurationClass(ConfigurationA.class);
    metadataProvider.addConfigurationClass(ConfigurationB.class);

    aemContext.registerInjectActivateService(new PagePersistenceStrategy(), "enabled", true);
    aemContext.registerService(ConfigurationMetadataProvider.class, metadataProvider);

    applyConfig(page1, "configA", CONFIGURATION_A); // 1 config on page1
    applyConfig(page2, "configA", CONFIGURATION_A); // 2 configs on page2
    applyConfig(page2, "configB", CONFIGURATION_B);
  }

  @Test
  public void testReferencesOfPage1() {
    ReferenceProvider referenceProvider = new ConfigurationReferenceProvider();
    aemContext.registerInjectActivateService(referenceProvider);
    List<Reference> references = referenceProvider.findReferences(aemContext.resourceResolver().getResource("/content/site1/page"));
    assertEquals(1, references.size());
  }

  @Test
  public void testReferencesOfPage2() {
    ReferenceProvider referenceProvider = new ConfigurationReferenceProvider();
    aemContext.registerInjectActivateService(referenceProvider);
    List<Reference> references = referenceProvider.findReferences(aemContext.resourceResolver().getResource("/content/site2/page"));
    assertEquals(2, references.size());
  }

  @Test
  public void testDisabled() {
    ReferenceProvider referenceProvider = new ConfigurationReferenceProvider();
    aemContext.registerInjectActivateService(referenceProvider, "enabled", false);
    List<Reference> references = referenceProvider.findReferences(aemContext.resourceResolver().getResource("/content/site1/page"));
    assertEquals(0, references.size());
  }

  private void applyConfig(Page p, String name, ValueMap values) {
    ConfigurationManager configManager = aemContext.getService(ConfigurationManager.class);
    Resource contextResource = p.adaptTo(Resource.class);
    configManager.persistConfiguration(contextResource, name, new ConfigurationPersistData(values));
  }

  private static class TestConfigurationMetadataProvider implements ConfigurationMetadataProvider {

    private final Map<String, ConfigurationMetadata> metadata = new HashMap<>();

    void addConfigurationClass(Class<?> cls) {
      metadata.put(AnnotationClassParser.getConfigurationName(cls), AnnotationClassParser.buildConfigurationMetadata(cls));
    }

    @Nonnull
    @Override
    public SortedSet<String> getConfigurationNames() {
      return new TreeSet<>(metadata.keySet());
    }

    @CheckForNull
    @Override
    public ConfigurationMetadata getConfigurationMetadata(String s) {
      return metadata.get(s);
    }
  }

}
