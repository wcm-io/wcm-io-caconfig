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
package io.wcm.config.core.persistence.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.caconfig.management.ContextPathStrategyMultiplexer;
import org.apache.sling.caconfig.resource.spi.ConfigurationResourceResolvingStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationInheritanceStrategy;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy;

/**
 * Common functionality for storing configuration in a configuration page.
 */
abstract class AbstractConfigPagePersistenceProvider implements ConfigurationResourceResolvingStrategy,
ConfigurationInheritanceStrategy, ConfigurationPersistenceStrategy {

  static final String CONFIG_RESOURCE_NAME = "config";
  static final String CONFIG_BUCKET_NAME = "sling:configs";
  static final String JCR_CONTENT = "jcr:content";


  // ---------- ConfigurationResourceResolvingStrategy ----------

  @Override
  public Resource getResource(Resource resource, String bucketName, String configName) {
    Iterator<Resource> resources = getResourceInheritanceChain(resource, bucketName, configName);
    if (resources != null && resources.hasNext()) {
      return resources.next();
    }
    return null;
  }

  @Override
  public Collection<Resource> getResourceCollection(Resource resource, String bucketName, String configName) {
    // TODO: implement resource collection handling
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Resource> getResourceInheritanceChain(Resource resource, String bucketName, final String configName) {
    if (!isEnabledAndParamsValid(resource, bucketName, configName)) {
      return null;
    }

    // find all matching items among all configured paths
    final ResourceResolver resourceResolver = resource.getResourceResolver();
    Iterator<String> paths = findConfigRefs(resource, bucketName);
    Iterator<Resource> matchingResources = IteratorUtils.transformedIterator(paths, new Transformer() {
      @Override
      public Object transform(Object input) {
        String path = (String)input;
        return resourceResolver.getResource(buildResourcePath(path, configName));
      }
    });
    return IteratorUtils.filteredIterator(matchingResources, PredicateUtils.notNullPredicate());
  }

  @Override
  public Collection<Iterator<Resource>> getResourceCollectionInheritanceChain(Resource resource, String bucketName, String configName) {
    if (!isEnabledAndParamsValid(resource, bucketName, configName)) {
      return null;
    }
    // TODO: implement resource collection handling
    return null;
  }

  @Override
  public String getResourcePath(Resource resource, String bucketName, String configName) {
    if (!isEnabledAndParamsValid(resource, bucketName, configName)) {
      return null;
    }
    Iterator<String> configRefs = findConfigRefs(resource, bucketName);
    if (configRefs.hasNext()) {
      return buildResourcePath(configRefs.next(), configName);
    }
    else {
      return null;
    }
  }

  @Override
  public String getResourceCollectionParentPath(Resource resource, String bucketName, String configName) {
    return getResourcePath(resource, bucketName, configName);
  }

  private boolean isEnabledAndParamsValid(final Resource contentResource, final String bucketName, final String configName) {
    return isEnabled()
        && contentResource != null
        // support only configuration buckets
        && StringUtils.equals(bucketName, CONFIG_BUCKET_NAME)
        && StringUtils.isNoneBlank(configName);
  }

  private String buildResourcePath(String path, String name) {
    return ResourceUtil.normalize(path + "/" + JCR_CONTENT + "/" + name);
  }

  @SuppressWarnings("unchecked")
  private Iterator<String> findConfigRefs(Resource startResource, final String bucketName) {
    // collect all context path resources without config ref, and expand to config page path
    Iterator<ContextResource> contextResources = getContextPathStrategy().findContextResources(startResource);
    return new FilterIterator(new TransformIterator(contextResources, new Transformer() {
      @Override
      public Object transform(Object input) {
        ContextResource contextResource = (ContextResource)input;
        if (contextResource.getConfigRef() == null) {
          return getConfigPagePath(contextResource.getResource().getPath());
        }
        return null;
      }
    }), PredicateUtils.notNullPredicate());
  }


  // ---------- ConfigurationInheritanceStrategy ----------

  @Override
  @SuppressWarnings("unchecked")
  public Resource getResource(Iterator<Resource> configResources) {
    if (!configResources.hasNext()) {
      return null;
    }

    Iterator<Resource> configPageResources = new FilterIterator(configResources, new Predicate() {
      @Override
      public boolean evaluate(Object object) {
        Resource resource = (Resource)object;
        return isConfigPagePath(resource.getPath());
      }
    });

    return getInheritedResourceInternal(configPageResources);
  }

  private Resource getInheritedResourceInternal(Iterator<Resource> configResources) {
    if (!configResources.hasNext()) {
      return null;
    }
    Resource primary = configResources.next();
    if (!configResources.hasNext()) {
      return primary;
    }
    Map<String, Object> mergedProps = getInheritedProperties(primary.getValueMap(), configResources);
    return new ConfigurationResourceWrapper(primary, new ValueMapDecorator(mergedProps));
  }

  private Map<String, Object> getInheritedProperties(Map<String, Object> parentProps, Iterator<Resource> inheritanceChain) {
    if (!inheritanceChain.hasNext()) {
      return parentProps;
    }
    Resource next = inheritanceChain.next();
    Map<String, Object> merged = new HashMap<>(next.getValueMap());
    merged.putAll(parentProps);
    return getInheritedProperties(merged, inheritanceChain);
  }


  // ---------- ConfigurationPersistenceStrategy ----------

  @Override
  public Resource getResource(Resource resource) {
    if (!isConfigPagePath(resource.getPath())) {
      return null;
    }
    return resource;
  }

  @Override
  public String getResourcePath(String resourcePath) {
    if (!isConfigPagePath(resourcePath)) {
      return null;
    }
    return resourcePath;
  }

  @Override
  public boolean persist(ResourceResolver resourceResolver, String configResourcePath, ConfigurationPersistData data) {
    if (!isConfigPagePath(configResourcePath)) {
      return false;
    }
    // TODO: implement peristence
    return false;
  }

  @Override
  public boolean persistCollection(ResourceResolver resourceResolver, String configResourceCollectionParentPath, ConfigurationCollectionPersistData data) {
    if (!isConfigPagePath(configResourceCollectionParentPath)) {
      return false;
    }
    // TODO: implement peristence
    return false;
  }


  // ---------- Internal ----------

  protected abstract boolean isEnabled();

  protected abstract String getConfigPagePath(String contextPath);

  protected abstract boolean isConfigPagePath(String configPath);

  protected abstract String getConfigPageTemplate();

  protected abstract String getStructurePageTemplate();

  protected abstract ContextPathStrategyMultiplexer getContextPathStrategy();

  /*
  @Override
  public final Map<String, Object> get(ResourceResolver resolver, String configurationId) {
    if (!isEnabled()) {
      return null;
    }
    String configPagePath = getConfigPagePath(configurationId);
    Page configPage = getPage(resolver, configPagePath);
    if (configPage != null) {
      if (log.isDebugEnabled()) {
        log.debug("Read config for {} from {}.", configurationId, configPage.getPath());
      }
      return getConfigMap(configPage);
    }
    return null;
  }

  @Override
  public final boolean store(ResourceResolver resolver, String configurationId, Map<String, Object> values)
      throws PersistenceException {
    if (!isEnabled()) {
      return false;
    }
    String configPagePath = getConfigPagePath(configurationId);
    Page configPage = getPage(resolver, configPagePath);
    if (configPage == null) {
      configPage = createPage(resolver, configPagePath, getConfigPageTemplate());
    }
    if (log.isDebugEnabled()) {
      log.debug("Store config for {} to {}.", configurationId, configPage.getPath());
    }
    storeValues(resolver, configPage, values);
    return true;
  }



  private Map<String, Object> getConfigMap(Page page) {
    Resource configResource = page.getContentResource(CONFIG_RESOURCE_NAME);
    if (configResource != null) {
      return configResource.getValueMap();
    }
    else {
      return ValueMap.EMPTY;
    }
  }

  private Page getPage(ResourceResolver resolver, String path) {
    Resource resource = resolver.getResource(path);
    if (resource != null) {
      return resource.adaptTo(Page.class);
    }
    return null;
  }

  private Page createPage(ResourceResolver resolver, String path, String template) throws PersistenceException {

    // ensure parent path hierarchy exists - if not create it using pages with structure template
    String parentPath = ResourceUtil.getParent(path);
    if (parentPath == null) {
      throw new RuntimeException("Unable to get parent path from: " + path);
    }
    if (resolver.getResource(parentPath) == null) {
      createPage(resolver, parentPath, getStructurePageTemplate());
    }

    // create path with given template
    String name = ResourceUtil.getName(path);
    PageManager pageManager = resolver.adaptTo(PageManager.class);
    try {
      return pageManager.create(parentPath, name, StringUtils.defaultString(template), name, true);
    }
    catch (WCMException ex) {
      throw new PersistenceException("Creating page at " + path + " failed.", ex);
    }
  }

  private void storeValues(ResourceResolver resolver, Page configPage, Map<String, Object> values) throws PersistenceException {
    try {
      ModifiableValueMap contentProps = configPage.getContentResource().adaptTo(ModifiableValueMap.class);

      // overwrite template path to make sure it used the template currently configured
      String configTemplate = getConfigPageTemplate();
      if (StringUtils.isNotEmpty(configTemplate)) {
        if (!StringUtils.equals(configTemplate, contentProps.get(NameConstants.PN_TEMPLATE, String.class))) {
          contentProps.put(NameConstants.PN_TEMPLATE, configTemplate);
        }
      }

      // write configuration data
      Resource configResource = configPage.getContentResource(CONFIG_RESOURCE_NAME);
      if (configResource != null) {
        resolver.delete(configResource);
      }
      configResource = resolver.create(configPage.getContentResource(), CONFIG_RESOURCE_NAME, values);

      // update last modified info
      contentProps.put(NameConstants.PN_LAST_MOD, Calendar.getInstance());
      contentProps.put(NameConstants.PN_LAST_MOD_BY, resolver.getAttribute(ResourceResolverFactory.USER));

      resolver.commit();
    }
    catch (PersistenceException ex) {
      throw new PersistenceException("Storing configuration values to " + configPage.getPath() + " failed.", ex);
    }
  }
   */

}
