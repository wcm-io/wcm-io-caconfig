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
package io.wcm.config.core.impl.application;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.config.core.management.Application;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("deprecation")
public class ApplicationFinderBridgeTest {

  private static final ApplicationInfo APP_1 = new ApplicationInfo("/apps/app1", "app1");
  private static final ApplicationInfo APP_2 = new ApplicationInfo("/apps/app2", "app2");

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private ApplicationFinder applicationFinder;
  @Mock
  private Resource resource;

  private io.wcm.config.core.management.ApplicationFinder underTest;

  @Before
  public void setUp() {
    when(applicationFinder.find(resource)).thenReturn(APP_1);
    when(applicationFinder.getAll()).thenReturn(ImmutableSortedSet.of(APP_1, APP_2));

    context.registerService(ApplicationFinder.class, applicationFinder);
    underTest = context.registerInjectActivateService(new ApplicationFinderBridge());
  }

  @Test
  public void testDelegation() {
    assertEquals(new Application("/apps/app1", "app1"), underTest.find(resource));
    assertEquals(ImmutableSortedSet.of(new Application("/apps/app1", "app1"), new Application("/apps/app2", "app2")), underTest.getAll());
  }

}
