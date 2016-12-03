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

import java.util.Hashtable;

import org.apache.sling.api.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

import io.wcm.caconfig.application.spi.ApplicationProvider;
import io.wcm.testing.mock.aem.junit.AemContext;

@SuppressWarnings("deprecation")
public class ApplicationProviderBridgeTest {

  @Rule
  public AemContext context = new AemContext();

  @Before
  public void setUp() {
    context.registerInjectActivateService(new ApplicationProviderBridge());
  }

  @After
  public void tearDown() {
    ApplicationProvider[] providers = context.getServices(ApplicationProvider.class, null);
    assertEquals(0, providers.length);
  }

  @Test
  public void testNoProviders() {
    ApplicationProvider[] providers = context.getServices(ApplicationProvider.class, null);
    assertEquals(0, providers.length);
  }

  @Test
  public void testOneProvider() {
    ServiceRegistration reg = registerProvider("/apps/app1");

    ApplicationProvider[] providers = context.getServices(ApplicationProvider.class, null);
    assertEquals(1, providers.length);
    assertEquals("/apps/app1", providers[0].getApplicationId());

    reg.unregister();
  }

  @Test
  public void testMultipleProviders() {
    ServiceRegistration reg1 = registerProvider("/apps/app1");
    ServiceRegistration reg2 = registerProvider("/apps/app2");
    ServiceRegistration reg3 = registerProvider("/apps/app3");

    ApplicationProvider[] providers = context.getServices(ApplicationProvider.class, null);
    assertEquals(3, providers.length);

    reg1.unregister();
    reg2.unregister();
    reg3.unregister();
  }

  private ServiceRegistration registerProvider(final String applicationId) {
    return context.bundleContext().registerService(io.wcm.config.spi.ApplicationProvider.class.getName(),
        new io.wcm.config.spi.ApplicationProvider() {
      @Override
      public String getApplicationId() {
        return applicationId;
      }
      @Override
      public String getLabel() {
        return applicationId + "-label";
      }
      @Override
      public boolean matches(Resource resource) {
        return true;
      }
    }, new Hashtable());
  }

}
