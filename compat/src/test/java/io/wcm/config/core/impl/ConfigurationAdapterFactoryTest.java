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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.config.api.Configuration;


@RunWith(MockitoJUnitRunner.class)
@Ignore // TODO: fix unit test
public class ConfigurationAdapterFactoryTest {

  @Mock
  private Resource resource;
  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private Configuration configuration;

  @InjectMocks
  private ConfigurationAdapterFactory underTest;

  @Before
  public void setUp() {
    when(request.getResource()).thenReturn(resource);
  }

  @Test
  public void testConfigurationResource() {
    assertSame(configuration, underTest.getAdapter(resource, Configuration.class));
    assertNull(underTest.getAdapter(this, Configuration.class));
  }

  @Test
  public void testConfigurationRequest() {
    assertSame(configuration, underTest.getAdapter(request, Configuration.class));

    when(request.getResource()).thenReturn(null);
    assertNull(underTest.getAdapter(request, Configuration.class));
  }

  @Test
  public void testConfigurationInvalid() {
    assertNull(underTest.getAdapter(this, Configuration.class));
  }

}
