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
package io.wcm.caconfig.editor.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;
import io.wcm.caconfig.editor.impl.ConfigNamesServlet;
import io.wcm.caconfig.editor.impl.ConfigPersistServlet;
import io.wcm.caconfig.editor.impl.EditorConfig;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class EditorConfigurationTest {

  private final AemContext context = new AemContext();

  private static final String SAMPLE_PATH = "/sample/path";
  private static final String DEFAULT_LANGUAGE = "en";

  private EditorConfiguration underTest;

  @Mock
  private ConfigurationResourceResolver configResourceResolver;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(new EditorConfig());
    context.registerService(ConfigurationResourceResolver.class, configResourceResolver);

    Resource contentResource = context.create().resource(SAMPLE_PATH);
    context.currentResource(contentResource);

    when(configResourceResolver.getContextPath(contentResource)).thenReturn(SAMPLE_PATH);

    underTest = AdaptTo.notNull(context.request(), EditorConfiguration.class);
  }

  @Test
  void testProperties() {
    assertEquals(SAMPLE_PATH + "." + ConfigNamesServlet.SELECTOR + ".json", underTest.getConfigNamesUrl());
    assertEquals(SAMPLE_PATH + "." + ConfigDataServlet.SELECTOR + ".json", underTest.getConfigDataUrl());
    assertEquals(SAMPLE_PATH + "." + ConfigPersistServlet.SELECTOR + ".json", underTest.getConfigPersistUrl());
    assertEquals(SAMPLE_PATH, underTest.getContextPath());
    assertEquals(DEFAULT_LANGUAGE, underTest.getLanguage());
    assertTrue(underTest.isEnabled());
  }

}
