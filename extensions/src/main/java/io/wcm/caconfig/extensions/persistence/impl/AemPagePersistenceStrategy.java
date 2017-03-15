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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

/**
 * AEM-specific persistence strategy that has higher precedence than the default strategy from Sling,
 * but lower precedence that the persistence strategy that is part of AEM since version 6.3.
 * <p>
 * It supports reading configurations from cq:Page nodes in /conf, the configuration is read from the jcr:content child
 * node. Unlike the persistence strategy in AEM 6.3 this also supports writing configuration to /conf.
 * </p>
 */
@Component(service = ConfigurationPersistenceStrategy.class,
    property = Constants.SERVICE_RANKING + ":Integer=500")
public class AemPagePersistenceStrategy implements ConfigurationPersistenceStrategy {

  private static final String DEFAULT_CONFIG_NODE_TYPE = NT_UNSTRUCTURED;
  private static final String DEFAULT_FOLDER_NODE_TYPE = "sling:Folder";
  private static final Pattern PAGE_PATH_PATTERN = Pattern.compile("^(.*)/" + Pattern.quote(JCR_CONTENT) + "(/.*)?$");

  private static final Logger log = LoggerFactory.getLogger(AemPagePersistenceStrategy.class);

  @Override
  public Resource getResource(Resource resource) {
    if (isInsidePage(resource.getPath())) {
      return resource;
    }
    return resource.getChild(JCR_CONTENT);
  }

  @Override
  public String getResourcePath(String resourcePath) {
    if (isInsidePage(resourcePath)) {
      return resourcePath;
    }
    return resourcePath + "/" + JCR_CONTENT;
  }

  private boolean isInsidePage(String path) {
    return PAGE_PATH_PATTERN.matcher(path).matches();
  }

  @Override
  public boolean persistConfiguration(ResourceResolver resolver, String configResourcePath,
      ConfigurationPersistData data) {
    String path = getResourcePath(configResourcePath);
    ensurePage(resolver, path);
    getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, data.getProperties());
    updatePageLastMod(resolver, path);
    commit(resolver);
    return true;
  }

  @Override
  public boolean persistConfigurationCollection(ResourceResolver resolver, String configResourceCollectionParentPath,
      ConfigurationCollectionPersistData data) {
    Resource configResourceParent = getOrCreateResource(resolver, configResourceCollectionParentPath, DEFAULT_CONFIG_NODE_TYPE, ValueMap.EMPTY);

    // delete existing children and create new ones
    deleteChildren(configResourceParent);
    for (ConfigurationPersistData item : data.getItems()) {
      String path = getResourcePath(configResourceParent.getPath() + "/" + item.getCollectionItemName());
      ensurePage(resolver, path);
      getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, item.getProperties());
    }

    // if resource collection parent properties are given replace them as well
    if (data.getProperties() != null) {
      replaceProperties(configResourceParent, data.getProperties());
    }

    commit(resolver);
    return true;
  }

  @Override
  public boolean deleteConfiguration(ResourceResolver resolver, String configResourcePath) {
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

  private void ensurePage(ResourceResolver resolver, String configResourcePath) {
    Matcher matcher = PAGE_PATH_PATTERN.matcher(configResourcePath);
    if (!matcher.matches()) {
      return;
    }
    String pagePath = matcher.group(1);
    Resource resource = resolver.getResource(pagePath);
    if (resource != null) {
      return;
    }
    // ensure parent folders exist
    String parentPath = ResourceUtil.getParent(pagePath);
    getOrCreateResource(resolver, parentPath, DEFAULT_FOLDER_NODE_TYPE, null);
    try {
      if (log.isTraceEnabled()) {
        log.trace("! Create cq:Page node at {}", pagePath);
      }
      PageManager pageManager = resolver.adaptTo(PageManager.class);
      pageManager.create(ResourceUtil.getParent(pagePath), ResourceUtil.getName(pagePath),
          null, ResourceUtil.getName(pagePath), false);
    }
    catch (WCMException ex) {
      throw new ConfigurationPersistenceException("Unable to create config page at " + pagePath, ex);
    }
  }

  private Resource getOrCreateResource(ResourceResolver resolver, String path, String defaultNodeType, Map<String, Object> properties) {
    try {
      Resource resource = ResourceUtil.getOrCreateResource(resolver, path, defaultNodeType, defaultNodeType, false);
      if (properties != null) {
        replaceProperties(resource, properties);
      }
      return resource;
    }
    catch (PersistenceException ex) {
      throw new ConfigurationPersistenceException("Unable to create resource at " + path, ex);
    }
  }

  private void deleteChildren(Resource resource) {
    ResourceResolver resolver = resource.getResourceResolver();
    try {
      for (Resource child : resource.getChildren()) {
        resolver.delete(child);
      }
    }
    catch (PersistenceException ex) {
      throw new ConfigurationPersistenceException("Unable to remove children from " + resource.getPath(), ex);
    }
  }

  private void replaceProperties(Resource resource, Map<String, Object> properties) {
    if (log.isTraceEnabled()) {
      log.trace("! Store properties for resource {}: {}", resource.getPath(), properties);
    }
    ModifiableValueMap modValueMap = resource.adaptTo(ModifiableValueMap.class);
    // remove all existing properties that do not have jcr: namespace
    for (String propertyName : new HashSet<>(modValueMap.keySet())) {
      if (StringUtils.startsWith(propertyName, "jcr:")) {
        continue;
      }
      modValueMap.remove(propertyName);
    }
    modValueMap.putAll(properties);
  }

  private void updatePageLastMod(ResourceResolver resolver, String configResourcePath) {
    Matcher matcher = PAGE_PATH_PATTERN.matcher(configResourcePath);
    if (!matcher.matches()) {
      return;
    }
    String pagePath = matcher.group(1);
    Resource contentResource = resolver.getResource(pagePath + "/" + JCR_CONTENT);
    if (contentResource != null) {
      ModifiableValueMap contentProps = contentResource.adaptTo(ModifiableValueMap.class);
      contentProps.put(NameConstants.PN_LAST_MOD, Calendar.getInstance());
      contentProps.put(NameConstants.PN_LAST_MOD_BY, resolver.getAttribute(ResourceResolverFactory.USER));
    }
  }

  private void commit(ResourceResolver resolver) {
    try {
      resolver.commit();
    }
    catch (PersistenceException ex) {
      throw new ConfigurationPersistenceException("Unable to save configuration: " + ex.getMessage(), ex);
    }
  }


}
