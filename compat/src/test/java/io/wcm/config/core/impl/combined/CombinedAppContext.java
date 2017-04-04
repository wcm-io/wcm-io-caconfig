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
package io.wcm.config.core.impl.combined;

import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;

import io.wcm.config.core.impl.ApplicationAdapterFactory;
import io.wcm.config.core.impl.ApplicationFinderImpl;
import io.wcm.config.core.impl.ApplicationImplementationPicker;
import io.wcm.config.core.impl.ConfigurationAdapterFactory;
import io.wcm.config.core.impl.ConfigurationFinderStrategyBridge;
import io.wcm.config.core.impl.ParameterOverrideProviderBridge;
import io.wcm.config.core.impl.ParameterProviderBridge;
import io.wcm.config.core.override.impl.SystemPropertyOverrideProvider;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.aem.junit.AemContextCallback;

public final class CombinedAppContext {

  public static final String APP_ID = "/apps/app1";
  public static final String CONFIG_PAGE_TEMPLATE = "/apps/dummy/templates/config";
  public static final String STRUCTURE_PAGE_TEMPLATE = "/apps/dummy/templates/tools";

  private CombinedAppContext() {
    // static methods only
  }

  public static AemContext newAemContext() {
    return new AemContextBuilder()
        .plugin(CACONFIG)
        .afterSetUp(SETUP_CALLBACK)
        .build();
  }

  /**
   * Custom set up rules required in all unit tests.
   */
  private static final AemContextCallback SETUP_CALLBACK = new AemContextCallback() {

    @Override
    public void execute(AemContext context) throws Exception {

      context.registerService(ConfigurationFinderStrategy.class, new SampleConfigurationFinderStrategy());
      context.registerService(ParameterProvider.class, new SampleParameterProvider());

      // application detection
      context.registerInjectActivateService(new ApplicationFinderImpl());
      context.registerInjectActivateService(new ApplicationAdapterFactory());
      context.registerInjectActivateService(new ApplicationImplementationPicker());

      // bridge services
      context.registerInjectActivateService(new ConfigurationFinderStrategyBridge());
      context.registerInjectActivateService(new ParameterOverrideProviderBridge());
      context.registerInjectActivateService(new ParameterProviderBridge());

      // override providers
      context.registerInjectActivateService(new SystemPropertyOverrideProvider(),
          "enabled", true);

      // adapter factory
      context.registerInjectActivateService(new ConfigurationAdapterFactory());

    }
  };

}
