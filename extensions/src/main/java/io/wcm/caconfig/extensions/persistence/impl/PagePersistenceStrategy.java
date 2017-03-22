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
package io.wcm.caconfig.extensions.persistence.impl;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.commit;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.containsJcrContent;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.convertPersistenceException;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.deleteChildren;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.ensurePage;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.getOrCreateResource;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.replaceProperties;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.updatePageLastMod;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy2;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AEM-specific persistence strategy that has higher precedence than the default strategy from Sling,
 * but lower precedence that the persistence strategy that is part of AEM since version 6.3.
 * <p>
 * It supports reading configurations from cq:Page nodes in /conf, the configuration is read from the jcr:content child
 * node. Unlike the persistence strategy in AEM 6.3 this also supports writing configuration to /conf.
 * </p>
 */
@Component(service = ConfigurationPersistenceStrategy2.class,
    property = Constants.SERVICE_RANKING + ":Integer=500")
@Designate(ocd = PagePersistenceStrategy.Config.class)
public class PagePersistenceStrategy implements ConfigurationPersistenceStrategy2 {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Persistence Strategy: AEM Page",
      description = "Stores Context-Aware Configuration in AEM pages instead of simple resources.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled",
        description = "Enable this persistence strategy.")
    boolean enabled() default false;
  }

  private static final String DEFAULT_CONFIG_NODE_TYPE = NT_UNSTRUCTURED;

  private static final Logger log = LoggerFactory.getLogger(PagePersistenceStrategy.class);

  private boolean enabled;

  @Activate
  void activate(Config value) {
    this.enabled = value.enabled();
  }

  @Override
  public Resource getResource(Resource resource) {
    if (!enabled) {
      return null;
    }
    if (containsJcrContent(resource.getPath())) {
      return resource;
    }
    return resource.getChild(JCR_CONTENT);
  }

  @Override
  public Resource getCollectionParentResource(Resource resource) {
    if (!enabled) {
      return null;
    }
    return resource;
  }

  @Override
  public Resource getCollectionItemResource(Resource resource) {
    return getResource(resource);
  }

  @Override
  public String getResourcePath(String resourcePath) {
    if (!enabled) {
      return null;
    }
    if (containsJcrContent(resourcePath)) {
      return resourcePath;
    }
    return resourcePath + "/" + JCR_CONTENT;
  }

  @Override
  public String getCollectionParentResourcePath(String resourcePath) {
    if (!enabled) {
      return null;
    }
    return resourcePath;
  }

  @Override
  public String getCollectionItemResourcePath(String resourcePath) {
    return getResourcePath(resourcePath);
  }

  @Override
  public String getConfigName(String configName, String relatedConfigPath) {
    if (!enabled) {
      return null;
    }
    if (containsJcrContent(configName)) {
      return configName;
    }
    return configName + "/" + JCR_CONTENT;
  }

  @Override
  public String getCollectionParentConfigName(String configName, String relatedConfigPath) {
    if (!enabled) {
      return null;
    }
    return configName;
  }

  @Override
  public String getCollectionItemConfigName(String configName, String relatedConfigPath) {
    return getConfigName(configName, relatedConfigPath);
  }

  @Override
  public boolean persistConfiguration(ResourceResolver resolver, String configResourcePath, ConfigurationPersistData data) {
    if (!enabled) {
      return false;
    }
    String path = getResourcePath(configResourcePath);
    ensurePage(resolver, path);

    getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, data.getProperties());

    updatePageLastMod(resolver, path);
    commit(resolver, configResourcePath);
    return true;
  }

  @Override
  public boolean persistConfigurationCollection(ResourceResolver resolver, String configResourceCollectionParentPath, ConfigurationCollectionPersistData data) {
    if (!enabled) {
      return false;
    }
    String parentPath = getCollectionParentResourcePath(configResourceCollectionParentPath);
    Resource configResourceParent = getOrCreateResource(resolver, parentPath, DEFAULT_CONFIG_NODE_TYPE, ValueMap.EMPTY);

    // delete existing children and create new ones
    deleteChildren(configResourceParent);
    for (ConfigurationPersistData item : data.getItems()) {
      String path = getCollectionItemResourcePath(parentPath + "/" + item.getCollectionItemName());
      ensurePage(resolver, path);
      getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, item.getProperties());
      updatePageLastMod(resolver, path);
    }

    // if resource collection parent properties are given replace them as well
    if (data.getProperties() != null) {
      replaceProperties(configResourceParent, data.getProperties());
    }

    commit(resolver, configResourceCollectionParentPath);
    return true;
  }

  @Override
  public boolean deleteConfiguration(ResourceResolver resolver, String configResourcePath) {
    if (!enabled) {
      return false;
    }
    Resource resource = resolver.getResource(configResourcePath);
    if (resource != null) {
      try {
        log.trace("! Delete resource {}", resource.getPath());
        resolver.delete(resource);
      }
      catch (PersistenceException ex) {
        throw convertPersistenceException("Unable to delete configuration at " + configResourcePath, ex);
      }
    }
    updatePageLastMod(resolver, configResourcePath);
    commit(resolver, configResourcePath);
    return true;
  }

}
