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
package io.wcm.caconfig.application.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.caconfig.application.spi.annotations.Application;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAdapterFactoryTest {

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private ApplicationFinder applicationFinder;
  private ApplicationInfo application;
  private Resource resource;

  private ApplicationAdapterFactory underTest;

  @Before
  public void setUp() {
    resource = context.create().resource("/content/test");
    context.currentResource(resource);

    application = new ApplicationInfo("app1", null);
    when(applicationFinder.find(resource)).thenReturn(application);

    context.registerService(ApplicationFinder.class, applicationFinder);
    underTest = context.registerInjectActivateService(new ApplicationAdapterFactory());
  }

  @Test
  public void testApplicationResource() {
    assertSame(application, underTest.getAdapter(resource, ApplicationInfo.class));
    assertNull(underTest.getAdapter(resource, ApplicationFinder.class));
    assertNull(underTest.getAdapter(this, Application.class));
  }

  @Test
  public void testApplicationRequest() {
    assertSame(application, underTest.getAdapter(context.request(), ApplicationInfo.class));

    context.currentResource((Resource)null);
    assertNull(underTest.getAdapter(context.request(), ApplicationInfo.class));
  }

  @Test
  public void testApplicationInvalid() {
    assertNull(underTest.getAdapter(this, ApplicationInfo.class));
  }

}
