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

import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.commit;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.deleteChildren;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.ensurePage;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.getOrCreateResource;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.replaceProperties;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.updatePageLastMod;

import java.util.regex.Pattern;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AEM-specific persistence strategy that gets only active if a context path is redirected to path
 * <code>/content/.../tools/config</code>.
 * In this case the configuration date is stored in a single page at /tools/config which can be easily activated by
 * editors via the authoring GUI, and the configuration can neatly be packaged together with the content.
 */
@Component(service = ConfigurationPersistenceStrategy.class,
    property = Constants.SERVICE_RANKING + ":Integer=2000")
@Designate(ocd = ToolsConfigPagePersistenceStrategy.Config.class)
public class ToolsConfigPagePersistenceStrategy implements ConfigurationPersistenceStrategy {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration AEM Tools Config Page Persistence Strategy",
      description = "Stores Context-Aware Configuration in a single AEM content page at /tools/config.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled",
        description = "Enable this persistence strategy.")
    boolean enabled() default false;
  }

  private static final String RELATIVE_CONFIG_PATH = "/tools/config/jcr:content";
  private static final Pattern CONFIG_PATH_PATTERN = Pattern.compile("^.*" + Pattern.quote(RELATIVE_CONFIG_PATH) + "(/.*)?$");
  private static final String DEFAULT_CONFIG_NODE_TYPE = NT_UNSTRUCTURED;

  private static final Logger log = LoggerFactory.getLogger(ToolsConfigPagePersistenceStrategy.class);

  private boolean enabled;

  @Activate
  void activate(Config value) {
    this.enabled = value.enabled();
  }

  @Override
  public Resource getResource(Resource resource) {
    if (!enabled || !isConfigPagePath(resource.getPath())) {
      return null;
    }
    return resource;
  }

  @Override
  public String getResourcePath(String resourcePath) {
    if (!enabled || !isConfigPagePath(resourcePath)) {
      return null;
    }
    return resourcePath;
  }

  @Override
  public boolean persistConfiguration(ResourceResolver resolver, String configResourcePath, ConfigurationPersistData data) {
    if (!enabled || !isConfigPagePath(configResourcePath)) {
      return false;
    }
    ensurePage(resolver, configResourcePath);
    getOrCreateResource(resolver, configResourcePath, DEFAULT_CONFIG_NODE_TYPE, data.getProperties());
    updatePageLastMod(resolver, configResourcePath);
    commit(resolver);
    return true;
  }

  @Override
  public boolean persistConfigurationCollection(ResourceResolver resolver, String configResourceCollectionParentPath, ConfigurationCollectionPersistData data) {
    if (!enabled || !isConfigPagePath(configResourceCollectionParentPath)) {
      return false;
    }
    ensurePage(resolver, configResourceCollectionParentPath);
    Resource configResourceParent = getOrCreateResource(resolver, configResourceCollectionParentPath, DEFAULT_CONFIG_NODE_TYPE, ValueMap.EMPTY);

    // delete existing children and create new ones
    deleteChildren(configResourceParent);
    for (ConfigurationPersistData item : data.getItems()) {
      String path = configResourceParent.getPath() + "/" + item.getCollectionItemName();
      getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, item.getProperties());
    }

    // if resource collection parent properties are given replace them as well
    if (data.getProperties() != null) {
      replaceProperties(configResourceParent, data.getProperties());
    }

    updatePageLastMod(resolver, configResourceCollectionParentPath);
    commit(resolver);
    return true;
  }

  @Override
  public boolean deleteConfiguration(ResourceResolver resolver, String configResourcePath) {
    if (!enabled || !isConfigPagePath(configResourcePath)) {
      return false;
    }
    Resource resource = resolver.getResource(configResourcePath);
    if (resource != null) {
      try {
        log.trace("! Delete resource {}", resource.getPath());
        resolver.delete(resource);
      }
      catch (PersistenceException ex) {
        throw new ConfigurationPersistenceException("Unable to delete configuration at " + configResourcePath, ex);
      }
    }
    updatePageLastMod(resolver, configResourcePath);
    commit(resolver);
    return true;
  }

  private boolean isConfigPagePath(String configPath) {
    return CONFIG_PATH_PATTERN.matcher(configPath).matches();
  }

}
