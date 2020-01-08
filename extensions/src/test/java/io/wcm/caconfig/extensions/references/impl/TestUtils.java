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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.impl.metadata.AnnotationClassParser;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationMetadataProvider;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.reference.Reference;

import io.wcm.testing.mock.aem.junit5.AemContext;

@SuppressWarnings("null")
final class TestUtils {

  private TestUtils() {
    // static methods only
  }

  public static void applyConfig(AemContext context, Page page, String name, ValueMap props) {
    ConfigurationManager configManager = context.getService(ConfigurationManager.class);
    Resource contextResource = page.adaptTo(Resource.class);
    if (contextResource == null) {
      throw new RuntimeException("No page resource: " + page.getPath());
    }
    configManager.persistConfiguration(contextResource, name, new ConfigurationPersistData(props));
  }

  public static void assetReferences(List<Reference> references, String... paths) {
    assertEquals(paths.length, references.size(), "number of references");
    for (int i = 0; i < paths.length; i++) {
      assertEquals(paths[i], references.get(i).getResource().getPath(), "reference #" + i);
    }
  }

  public static void registerConfigurations(AemContext context, Class<?>... configurationClasses) {
    DummyConfigurationMetadataProvider metadataProvider = new DummyConfigurationMetadataProvider();
    for (Class<?> clazz : configurationClasses) {
      metadataProvider.addConfigurationClass(clazz);
    }

    context.registerService(ConfigurationMetadataProvider.class, metadataProvider);
  }

  private static class DummyConfigurationMetadataProvider implements ConfigurationMetadataProvider {

    private final Map<String, ConfigurationMetadata> metadata = new HashMap<>();

    void addConfigurationClass(Class<?> cls) {
      metadata.put(AnnotationClassParser.getConfigurationName(cls), AnnotationClassParser.buildConfigurationMetadata(cls));
    }

    @NotNull
    @Override
    public SortedSet<String> getConfigurationNames() {
      return new TreeSet<>(metadata.keySet());
    }

    @Nullable
    @Override
    public ConfigurationMetadata getConfigurationMetadata(String s) {
      return metadata.get(s);
    }

  }

}
