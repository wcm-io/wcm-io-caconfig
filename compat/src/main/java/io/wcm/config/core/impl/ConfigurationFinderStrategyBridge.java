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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.caconfig.resource.spi.ContextPathStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.config.core.management.Application;
import io.wcm.config.core.management.ApplicationFinder;
import io.wcm.config.spi.ConfigurationFinderStrategy;

/**
 * Bridges configuration finder strategies to a caconfig context path strategy.
 */
@Component(service = ContextPathStrategy.class, immediate = true)
public class ConfigurationFinderStrategyBridge implements ContextPathStrategy {

  @Reference(service = ConfigurationFinderStrategy.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
      bind = "bindConfigurationFinderStrategy", unbind = "unbindConfigurationFinderStrategy")
  private RankedServices<ConfigurationFinderStrategy> configurationFinderStrategies = new RankedServices<>(Order.ASCENDING);

  @Reference
  private ApplicationFinder applicationFinder;

  private static final Logger log = LoggerFactory.getLogger(ConfigurationFinderStrategyBridge.class);

  @Override
  public Iterator<ContextResource> findContextResources(Resource resource) {
    String applicationId = findApplicationId(resource);
    ResourceResolver resourceResolver = resource.getResourceResolver();

    List<ContextResource> result = new ArrayList<>();
    for (ConfigurationFinderStrategy item : configurationFinderStrategies) {
      if (matchesApplicationId(applicationId, item.getApplicationId())) {
        Iterator<String> contextResourcePaths = item.findConfigurationIds(resource);
        while (contextResourcePaths.hasNext()) {
          String contextResourcePath = contextResourcePaths.next();
          Resource contextResource = resourceResolver.getResource(contextResourcePath);
          if (contextResource != null) {
            log.trace("+ Found context path {}, configRef {}", resource.getPath(), null);
            result.add(new ContextResource(contextResource, null));
          }
        }
      }
    }

    return result.iterator();
  }

  private String findApplicationId(Resource resource) {
    Application application = applicationFinder.find(resource);
    if (application != null) {
      return application.getApplicationId();
    }
    return null;
  }

  private boolean matchesApplicationId(String expected, String actual) {
    if (expected == null) {
      return true;
    }
    else {
      return StringUtils.equals(expected, actual);
    }
  }

  void bindConfigurationFinderStrategy(ConfigurationFinderStrategy service, Map<String, Object> props) {
    configurationFinderStrategies.bind(service, props);
  }

  void unbindConfigurationFinderStrategy(ConfigurationFinderStrategy service, Map<String, Object> props) {
    configurationFinderStrategies.unbind(service, props);
  }

}
