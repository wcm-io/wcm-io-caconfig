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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;

import io.wcm.config.api.Configuration;
import io.wcm.config.core.management.Application;
import io.wcm.config.core.management.ApplicationFinder;
import io.wcm.config.core.management.ConfigurationFinder;

/**
 * AdapterFactory that adapts resources to effective configurations and applications.
 */
@Component(immediate = true, metatype = false)
@Service(AdapterFactory.class)
@Properties({
  @Property(name = ADAPTABLE_CLASSES, value = {
      "org.apache.sling.api.SlingHttpServletRequest",
      "org.apache.sling.api.resource.Resource"
  }),
  @Property(name = ADAPTER_CLASSES, value = {
      "io.wcm.config.api.Configuration",
      "io.wcm.config.core.management.Application"
  }),
  @Property(name = "adapter.condition", value = "If a configuration can be found for the current/given resource or it's parents.")
})
public final class ConfigurationAdapterFactory implements AdapterFactory {

  @Reference
  private ConfigurationFinder configurationFinder;
  @Reference
  private ApplicationFinder applicationFinder;

  @SuppressWarnings("unchecked")
  @Override
  public <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
    if (type == Configuration.class) {
      Resource resource = AdaptableUtil.getResource(adaptable);
      if (resource != null) {
        return (AdapterType)configurationFinder.find(resource);
      }
    }
    else if (type == Application.class) {
      Resource resource = AdaptableUtil.getResource(adaptable);
      if (resource != null) {
        return (AdapterType)applicationFinder.find(resource);
      }
    }
    return null;
  }

}
