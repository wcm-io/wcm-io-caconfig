/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationResourceResolvingStrategyMultiplexer;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;

/**
 * <p>
 * This implementation of {@link ReferenceProvider} allows to resolve references of a given {@link Resource} to
 * context-aware configurations.
 * </p>
 * <p>
 * This is for example used by ActivationReferenceSearchServlet to resolve referenced content of pages during activation
 * of a page using AEM sites. Returning the configurations allows the editor to activate them along with the page
 * referring to them.
 * </p>
 * <p>
 * This component can be disabled by configuration, but its enabled by default.
 * </p>
 */
@Component(service = ReferenceProvider.class)
@Designate(ocd = ConfigurationReferenceProvider.Config.class)
public class ConfigurationReferenceProvider implements ReferenceProvider {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Reference Provider",
      description = "Allows to resolve references from resources to their Context-Aware configurations, for example during page activation.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled",
        description = "Enable this reference provider.")
    boolean enabled() default true;
  }

  static final String REFERENCE_TYPE = "caconfig";

  @org.osgi.service.component.annotations.Reference
  private ConfigurationManager configurationManager;

  @org.osgi.service.component.annotations.Reference
  private ConfigurationResourceResolvingStrategyMultiplexer configurationResourceResolvingStrategy;

  @org.osgi.service.component.annotations.Reference
  private ConfigurationAdmin configAdmin;

  private boolean enabled;

  private static final Logger log = LoggerFactory.getLogger(ConfigurationReferenceProvider.class);

  @Activate
  protected void activate(Config config) {
    enabled = config.enabled();
  }

  @Deactivate
  protected void deactivate() {
    enabled = false;
  }

  @Override
  public List<Reference> findReferences(Resource resource) {
    if (!enabled) {
      return Collections.emptyList();
    }

    Set<String> configurationNames = configurationManager.getConfigurationNames();
    List<Reference> references = new ArrayList<>(configurationNames.size());
    Collection<String> configurationBuckets = getBucketNames();

    for (String configurationName : configurationNames) {
      ConfigurationMetadata configurationMetadata = configurationManager.getConfigurationMetadata(configurationName);
      Iterator<Resource> configurationInheritanceChain = configurationResourceResolvingStrategy.getResourceInheritanceChain(resource, configurationBuckets, configurationName);

      while (configurationInheritanceChain != null && configurationInheritanceChain.hasNext()) {
        Resource configurationResource = configurationInheritanceChain.next();
        log.trace("Found reference {} for configuration {} and resource {}", configurationResource.getPath(), configurationName, resource.getPath());
        references.add(new Reference(getType(), getReferenceName(configurationMetadata), configurationResource,
            getLastModifiedOf(configurationResource)));
      }
    }

    log.debug("Found {} references for resource {}", references.size(), resource.getPath());

    return references;
  }

  private static String getReferenceName(ConfigurationMetadata configurationMetadata) {
    return StringUtils.defaultIfEmpty(configurationMetadata.getLabel(), configurationMetadata.getName());
  }

  private static long getLastModifiedOf(Resource configurationResource) {
    Page configurationPage = configurationResource.adaptTo(Page.class);

    if (configurationPage == null && StringUtils.equals(configurationResource.getName(), JcrConstants.JCR_CONTENT)) {
      configurationPage = configurationResource.getParent().adaptTo(Page.class);
    }

    Calendar lastModified;
    if (configurationPage != null && configurationPage.getLastModified() != null) {
      lastModified = configurationPage.getLastModified();
    }
    else {
      ValueMap properties = configurationResource.getValueMap();
      lastModified = properties.get(JcrConstants.JCR_LASTMODIFIED, Calendar.class);
    }

    return lastModified != null ? lastModified.getTimeInMillis() : 0;
  }

  private static String getType() {
    return REFERENCE_TYPE;
  }

  private Collection<String> getBucketNames() {
    // TODO: this is only a temporary workaround to collect the list of bucket names until https://issues.apache.org/jira/browse/SLING-7208 is available
    List<String> bucketNames = new ArrayList<>();
    bucketNames.add("sling:configs");

    try {
      Configuration configResolverConfig = configAdmin.getConfiguration("org.apache.sling.caconfig.impl.ConfigurationResolverImpl");
      if (configResolverConfig != null && configResolverConfig.getProperties() != null) {
        String[] addtlBucketNames = PropertiesUtil.toStringArray(configResolverConfig.getProperties().get("configBucketNames"));
        if (addtlBucketNames != null && addtlBucketNames.length > 0) {
          bucketNames.addAll(Arrays.asList(addtlBucketNames));
        }
      }
    }
    catch (IOException ex) {
      log.warn("Error accessing OSGi config.", ex);
    }

    return bucketNames;
  }

}
