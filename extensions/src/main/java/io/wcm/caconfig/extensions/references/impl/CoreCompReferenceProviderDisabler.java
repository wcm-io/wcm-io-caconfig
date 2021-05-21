/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.caconfig.extensions.references.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * With Adobe Core Components 2.12.0, an own implementation of a context-aware reference provider was added
 * to the core components. It implements a much simplified logic compared to {@link ConfigurationReferenceProvider}
 * and does not properly detect the last modification date if one of the wcm.io persistence strategies
 * is used which stores the configuration in AEM pages that can be properly activated.
 * <p>
 * To avoid conflicts with the two implementations trying to achieve the same, this services checks if
 * the Core Component reference provider is present, and deactivates it.
 * </p>
 * <p>
 * Unfortunately there is no better way, as the Core Component reference provider does not support to be disabled via
 * OSGi configuration, and the AEM WCM Core implementation does not sort the reference providers by service ranking.
 * If both reference providers are active at the same time it's a matter of luck who "wins".
 * </p>
 * <p>
 * The implementation of this service is heavily inspired by the "Component Disabler" of ACS AEM Commons.
 * </p>
 */
/*
 * There is not unit test for this class - it's too difficult in mock context.
 */
@Component(service = EventHandler.class, property = {
    EventConstants.EVENT_TOPIC + "=org/osgi/framework/BundleEvent/STARTED",
    EventConstants.EVENT_TOPIC + "=org/osgi/framework/BundleEvent/REGISTERED"
})
@Designate(ocd = CoreCompReferenceProviderDisabler.Config.class)
public class CoreCompReferenceProviderDisabler implements EventHandler {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Reference Provider Core Component Disabler",
      description = "Disables the context-aware configuration reference provider from Adobe Core Components, which "
          + "does not reliably detect the last modification date of configuration stored in AEM pages.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled",
        description = "Enable to automatic disable the reference provider from Core Components.")
    boolean enabled() default true;

  }

  static final String CORECOMP_REFERENCE_PROVIDER = "com.adobe.cq.wcm.core.components.internal.services.CaConfigReferenceProvider";

  @Reference
  private ServiceComponentRuntime scr;

  private BundleContext bundleContext;
  private boolean enabled;

  @Activate
  void activate(BundleContext context, Config config) {
    this.bundleContext = context;
    enabled = config.enabled();
    disableComponent();
  }

  @Override
  public void handleEvent(Event event) {
    // we don't care about the event, just make sure the component we want to disable is still disabled
    disableComponent();
  }

  private void disableComponent() {
    if (!enabled) {
      return;
    }
    for (Bundle bundle : bundleContext.getBundles()) {
      ComponentDescriptionDTO dto = scr.getComponentDescriptionDTO(bundle, CORECOMP_REFERENCE_PROVIDER);
      if (dto != null && scr.isComponentEnabled(dto)) {
        scr.disableComponent(dto);
      }
    }
  }

}
