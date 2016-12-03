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

import static org.apache.sling.api.adapter.AdapterFactory.ADAPTABLE_CLASSES;
import static org.apache.sling.api.adapter.AdapterFactory.ADAPTER_CLASSES;

import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.config.api.Configuration;
import io.wcm.config.core.impl.util.AdaptableUtil;

/**
 * AdapterFactory that adapts resources to effective configurations and applications.
 */
@Component(immediate = true, service = AdapterFactory.class, property = {
    ADAPTABLE_CLASSES + "=org.apache.sling.api.SlingHttpServletRequest",
    ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
    ADAPTER_CLASSES + "=io.wcm.config.api.Configuration",
    "adapter.condition=If a configuration can be found for the current/given resource or it's parents."
})
public final class ConfigurationAdapterFactory implements AdapterFactory {

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private ConfigurationResourceResolver configResourceResolver;

  @SuppressWarnings("unchecked")
  @Override
  public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
    if (type == Configuration.class) {
      Resource resource = AdaptableUtil.getResource(adaptable);
      if (resource != null) {
        return (AdapterType)getConfiguration(resource);
      }
    }
    return null;
  }

  private Configuration getConfiguration(Resource resource) {
    // get config via ConfigurationManager because ConfigurationResolver currently does not support default values for ValueMaps
    ConfigurationData configData = configManager.getConfiguration(resource, ParameterProviderBridge.DEFAULT_CONFIG_NAME);
    if (configData == null) {
      return null;
    }
    String contextPath = configResourceResolver.getContextPath(resource);
    return new ConfigurationImpl(contextPath, configData.getEffectiveValues());
  }

}
