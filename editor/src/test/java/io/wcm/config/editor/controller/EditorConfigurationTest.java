/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.config.editor.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import io.wcm.config.api.Configuration;
import io.wcm.config.core.management.ParameterPersistence;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.Page;

@RunWith(MockitoJUnitRunner.class)
public class EditorConfigurationTest {

  private static final String SAMPLE_PATH = "/sample/path";

  @Mock
  private Page currentPage;
  @Mock
  private Resource contentResource;
  @Mock
  private Configuration configuration;

  private EditorConfiguration underTest;

  @Before
  public void setUp() {
    when(currentPage.getContentResource()).thenReturn(contentResource);
    when(contentResource.getPath()).thenReturn(SAMPLE_PATH);
    when(configuration.getConfigurationId()).thenReturn("Test Configuration");
    underTest = new EditorConfiguration(currentPage, configuration);
  }

  @Test
  public void testProperties() {
    assertEquals(SAMPLE_PATH + ".configProvider.json", underTest.getProviderUrl());
    assertEquals(ParameterPersistence.PN_LOCKED_PARAMETER_NAMES, underTest.getLockedNamesAttributeName());
    assertEquals("Test Configuration", underTest.getConfigurationId());
  }

}
