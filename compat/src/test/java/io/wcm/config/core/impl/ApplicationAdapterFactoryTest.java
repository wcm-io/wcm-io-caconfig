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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.wcm.config.core.management.Application;
import io.wcm.config.core.management.ApplicationFinder;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationAdapterFactoryTest {

  private final AemContext context = new AemContext();

  @Mock
  private ApplicationFinder applicationFinder;
  private Application application;
  private Resource resource;

  private ApplicationAdapterFactory underTest;

  @BeforeEach
  void setUp() {
    resource = context.create().resource("/content/test");
    context.currentResource(resource);

    application = new Application("app1", null);
    when(applicationFinder.find(resource)).thenReturn(application);

    context.registerService(ApplicationFinder.class, applicationFinder);
    underTest = context.registerInjectActivateService(new ApplicationAdapterFactory());
  }

  @Test
  void testApplicationResource() {
    assertSame(application, underTest.getAdapter(resource, Application.class));
    assertNull(underTest.getAdapter(resource, ApplicationFinder.class));
    assertNull(underTest.getAdapter(this, Application.class));
  }

  @Test
  void testApplicationRequest() {
    assertSame(application, underTest.getAdapter(context.request(), Application.class));

    context.currentResource((Resource)null);
    assertNull(underTest.getAdapter(context.request(), Application.class));
  }

  @Test
  void testApplicationInvalid() {
    assertNull(underTest.getAdapter(this, Application.class));
  }

}
