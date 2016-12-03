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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableList;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationFinderStrategyBridgeTest {

  private static final String APP_1 = "/apps/app1";
  private static final String APP_2 = "/apps/app2";

  @Rule
  public AemContext context = new AemContext();

  @Mock
  private ApplicationFinder applicationFinder;
  @Mock
  private ConfigurationFinderStrategy configurationFinderStrategy1;
  @Mock
  private ConfigurationFinderStrategy configurationFinderStrategy2;
  private Resource resourceApp1;
  private Resource resourceNoApp;

  private ConfigurationFinderStrategyBridge underTest;

  @Before
  public void setUp() {
    context.registerService(ApplicationFinder.class, applicationFinder);
    context.registerService(ConfigurationFinderStrategy.class, configurationFinderStrategy1, Constants.SERVICE_RANKING, 100);
    context.registerService(ConfigurationFinderStrategy.class, configurationFinderStrategy2, Constants.SERVICE_RANKING, 200);
    underTest = context.registerInjectActivateService(new ConfigurationFinderStrategyBridge());

    resourceApp1 = context.create().resource("/content/app1/site1/page1");
    resourceNoApp = context.create().resource("/content/other/page1");

    when(applicationFinder.find(resourceApp1)).thenReturn(new ApplicationInfo(APP_1, APP_1));

    when(configurationFinderStrategy1.findConfigurationIds(any(Resource.class)))
    .thenReturn(ImmutableList.of("/content/app1/site1", "/content/app1").iterator());
    when(configurationFinderStrategy1.getApplicationId()).thenReturn(APP_1);
    when(configurationFinderStrategy2.findConfigurationIds(any(Resource.class))).thenReturn(ImmutableList.of("/content/other").iterator());
    when(configurationFinderStrategy2.getApplicationId()).thenReturn(APP_2);
  }

  @Test
  public void testResourceApp1() {
    List<ContextResource> result = ImmutableList.copyOf(underTest.findContextResources(resourceApp1));
    assertEquals(2, result.size());
    assertEquals("/content/app1/site1", result.get(0).getResource().getPath());
    assertEquals("/content/app1", result.get(1).getResource().getPath());
  }

  @Test
  public void testResourceNoApp() {
    List<ContextResource> result = ImmutableList.copyOf(underTest.findContextResources(resourceNoApp));
    assertEquals(3, result.size());
    assertEquals("/content/app1/site1", result.get(0).getResource().getPath());
    assertEquals("/content/app1", result.get(1).getResource().getPath());
    assertEquals("/content/other", result.get(2).getResource().getPath());
  }

}
