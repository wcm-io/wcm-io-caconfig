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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import io.wcm.config.core.management.Application;
import io.wcm.config.spi.ApplicationProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationFinderImplTest {

  @Rule
  public AemContext context = new AemContext();

  private Resource resource;

  @Mock
  private ApplicationProvider applicationProvider1;
  private static final Map<String, Object> SERVICE_PROPS_1 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 1L,
          Constants.SERVICE_RANKING, 10);
  private static final String APPLICATION_ID_1 = "app1";
  private static final String APPLICATION_LABEL_1 = "Application #1";

  @Mock
  private ApplicationProvider applicationProvider2;
  private static final Map<String, Object> SERVICE_PROPS_2 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 2L,
          Constants.SERVICE_RANKING, 5);
  private static final String APPLICATION_ID_2 = "app2";
  private static final String APPLICATION_LABEL_2 = "Application #2";

  private ApplicationFinderImpl underTest;

  @Before
  public void setUp() {
    resource = context.create().resource("/any/path");

    context.registerService(ApplicationProvider.class, applicationProvider1, SERVICE_PROPS_1);
    context.registerService(ApplicationProvider.class, applicationProvider2, SERVICE_PROPS_2);

    when(applicationProvider1.getApplicationId()).thenReturn(APPLICATION_ID_1);
    when(applicationProvider1.getLabel()).thenReturn(APPLICATION_LABEL_1);
    when(applicationProvider1.matches(resource)).thenReturn(true);

    when(applicationProvider2.getApplicationId()).thenReturn(APPLICATION_ID_2);
    when(applicationProvider2.getLabel()).thenReturn(APPLICATION_LABEL_2);
    when(applicationProvider2.matches(resource)).thenReturn(false);

    underTest = context.registerInjectActivateService(new ApplicationFinderImpl());
  }

  @Test
  public void testFind() {
    Application app = underTest.find(resource);
    assertNotNull(app);
    assertEquals(APPLICATION_ID_1, app.getApplicationId());
    assertEquals(APPLICATION_LABEL_1, app.getLabel());

    verify(applicationProvider2, times(1)).matches(resource);
  }

  @Test
  public void testGetAll() {
    Set<Application> allApps = underTest.getAll();
    Application[] apps = allApps.toArray(new Application[allApps.size()]);
    assertEquals(2, apps.length);
    assertEquals(APPLICATION_ID_1, apps[0].getApplicationId());
    assertEquals(APPLICATION_ID_2, apps[1].getApplicationId());
  }

}
