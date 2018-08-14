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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;
import io.wcm.caconfig.editor.impl.ConfigNamesServlet;
import io.wcm.caconfig.editor.impl.ConfigPersistServlet;
import io.wcm.caconfig.editor.impl.EditorConfig;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("null")
public class EditorConfigurationTest {

  @Rule
  public AemContext context = new AemContext();

  private static final String SAMPLE_PATH = "/sample/path";

  @Mock
  private Resource contentResource;
  @Mock
  private ConfigurationResourceResolver configResourceResolver;

  private EditorConfiguration underTest;

  @Before
  public void setUp() {
    when(contentResource.getPath()).thenReturn(SAMPLE_PATH);
    when(configResourceResolver.getContextPath(contentResource)).thenReturn(SAMPLE_PATH);
    EditorConfig editorConfig = context.registerInjectActivateService(new EditorConfig());
    underTest = new EditorConfiguration(contentResource, configResourceResolver, editorConfig);
  }

  @Test
  public void testProperties() {
    assertEquals(SAMPLE_PATH + "." + ConfigNamesServlet.SELECTOR + ".json", underTest.getConfigNamesUrl());
    assertEquals(SAMPLE_PATH + "." + ConfigDataServlet.SELECTOR + ".json", underTest.getConfigDataUrl());
    assertEquals(SAMPLE_PATH + "." + ConfigPersistServlet.SELECTOR + ".json", underTest.getConfigPersistUrl());
    assertEquals(SAMPLE_PATH, underTest.getContextPath());
    assertTrue(underTest.isEnabled());
  }

}
