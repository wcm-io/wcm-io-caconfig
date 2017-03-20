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

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.sling.api.resource.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.wcm.config.spi.ApplicationProvider;
import io.wcm.sling.commons.osgi.RankedServices;

/**
 * Bridges deprecated ApplicationProvider implementations to
 * {@link io.wcm.caconfig.application.spi.ApplicationProvider}.
 */
@Component(immediate = true)
@SuppressWarnings("deprecation")
public final class ApplicationProviderBridge {

  @Reference(service = ApplicationProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
      bind = "bindApplicationProvider", unbind = "unbindApplicationProvider")
  private final RankedServices<ApplicationProvider> applicationProviders = new RankedServices<>();
  private ConcurrentMap<Object, ServiceRegistration> bridgeServices = new ConcurrentHashMap<>();

  private BundleContext bundleContext;

  void bindApplicationProvider(final ApplicationProvider service, Map<String, Object> props) {
    applicationProviders.bind(service, props);

    ServiceRegistration serviceReg = bundleContext.registerService(io.wcm.caconfig.application.spi.ApplicationProvider.class.getName(),
        new io.wcm.caconfig.application.spi.ApplicationProvider() {

          @Override
          public String getApplicationId() {
            return service.getApplicationId();
          }

          @Override
          public String getLabel() {
            return service.getLabel();
          }

          @Override
          public boolean matches(Resource resource) {
            return service.matches(resource);
          }

        }, new Hashtable<String, Object>());
    bridgeServices.put(service, serviceReg);
  }

  void unbindApplicationProvider(final ApplicationProvider service, Map<String, Object> props) {
    applicationProviders.unbind(service, props);

    ServiceRegistration serviceReg = bridgeServices.remove(service);
    if (serviceReg != null) {
      serviceReg.unregister();
    }
  }

  @Activate
  private void activate(BundleContext bc) {
    this.bundleContext = bc;
  }

}
