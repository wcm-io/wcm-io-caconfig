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

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.caconfig.management.multiplexer.ContextPathStrategyMultiplexer;
import org.apache.sling.caconfig.resource.spi.ConfigurationResourceResolvingStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationInheritanceStrategy;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceAccessDeniedException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy2;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

import io.wcm.config.core.impl.ParameterProviderBridge;

/**
 * Persistence provider to provide compatibility to old wcm.io Configuration storage concept:
 * - Automatically detects configuration references in /tools/config pages below context resources
 * - Implements automatic property inheritance between those
 * - Reads and stores singleton configuration data with a fixed config name "config" from this page
 * - No other config names and no config resource collections are supported
 */
@Component(immediate = true, service = {
    ConfigurationResourceResolvingStrategy.class, ConfigurationInheritanceStrategy.class, ConfigurationPersistenceStrategy2.class
},
    property = Constants.SERVICE_RANKING + ":Integer=2000")
@Designate(ocd = ToolsConfigPagePersistenceProvider.Config.class)
public final class ToolsConfigPagePersistenceProvider implements ConfigurationResourceResolvingStrategy,
    ConfigurationInheritanceStrategy, ConfigurationPersistenceStrategy2 {

  private static final String RELATIVE_CONFIG_PATH = "/tools/config";
  private static final Pattern CONFIG_PATH_PATTERN = Pattern.compile("^.*" + RELATIVE_CONFIG_PATH + "(/.*)?$");
  private static final String CONFIG_BUCKET_NAME = "sling:configs";
  private static final String JCR_CONTENT = "jcr:content";

  @ObjectClassDefinition(name = "wcm.io Configuration Compatibility: Persistence Provider /tools/config Pages",
      description = "Bridge implementation to support wcm.io-style Configuration in /tools/config pages as compatibility mode.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Enable compatibility mode for /tools/config Pages.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Service Ranking", description = "Priority of parameter persistence providers (higher = higher priority).")
    int service_ranking() default 2000;

    @AttributeDefinition(name = "Config Template", description = "Template that is used for a configuration page.")
    String configPageTemplate();

    @AttributeDefinition(name = "Structure Template", description = "Template that is used for the tools page.")
    String structurePageTemplate();

  }

  @Reference
  private ContextPathStrategyMultiplexer contextPathStrategy;

  private volatile Config config;

  private static final Logger log = LoggerFactory.getLogger(ToolsConfigPagePersistenceProvider.class);

  @Activate
  void activate(Config cfg) {
    this.config = cfg;
  }


  // ---------- ConfigurationResourceResolvingStrategy ----------

  @Override
  public Resource getResource(Resource resource, Collection<String> bucketNames, String configName) {
    Iterator<Resource> resources = getResourceInheritanceChain(resource, bucketNames, configName);
    if (resources != null && resources.hasNext()) {
      return resources.next();
    }
    return null;
  }

  @Override
  public Collection<Resource> getResourceCollection(Resource resource, Collection<String> bucketNames, String configName) {
    // not supported for compat mode
    return null;
  }

  @Override
  public Iterator<Resource> getResourceInheritanceChain(Resource resource, Collection<String> bucketNames, final String configName) {
    if (!isEnabledAndParamsValid(resource, bucketNames, configName)) {
      return null;
    }

    // find all matching items among all configured paths
    final ResourceResolver resourceResolver = resource.getResourceResolver();
    Iterator<String> paths = findConfigRefs(resource);
    return getResourceInheritanceChainInternal(configName, paths, resourceResolver);
  }

  @SuppressWarnings("unchecked")
  private Iterator<Resource> getResourceInheritanceChainInternal(final String configName,
      final Iterator<String> paths, final ResourceResolver resourceResolver) {

    // find all matching items among all configured paths
    Iterator<Resource> matchingResources = IteratorUtils.transformedIterator(paths, new Transformer() {
      @Override
      public Object transform(Object input) {
        String configPath = buildResourcePath((String)input, configName);
        Resource resource = resourceResolver.getResource(configPath);
        if (resource != null) {
          log.trace("+ Found matching config resource for inheritance chain: {}", configPath);
        }
        else {
          log.trace("- No matching config resource for inheritance chain: {}", configPath);
        }
        return resource;
      }
    });
    Iterator<Resource> result = IteratorUtils.filteredIterator(matchingResources, PredicateUtils.notNullPredicate());
    if (result.hasNext()) {
      return result;
    }
    else {
      return null;
    }
  }

  @Override
  public Collection<Iterator<Resource>> getResourceCollectionInheritanceChain(final Resource resource, final Collection<String> bucketNames,
      final String configName) {
    // not supported for compat mode
    return null;
  }

  @Override
  public String getResourcePath(Resource resource, String bucketNames, String configName) {
    if (!isEnabledAndParamsValid(resource, Collections.singleton(bucketNames), configName)) {
      return null;
    }
    Iterator<String> configRefs = findConfigRefs(resource);
    if (configRefs.hasNext()) {
      String configPath = buildResourcePath(configRefs.next(), configName);
      log.trace("+ Building configuration path for name '{}' for resource {}: {}", configName, resource.getPath(), configPath);
      return configPath;
    }
    else {
      log.trace("- No configuration path for name '{}' found for resource {}", configName, resource.getPath());
      return null;
    }
  }

  @Override
  public String getResourceCollectionParentPath(Resource resource, String bucketName, String configName) {
    return getResourcePath(resource, bucketName, configName);
  }

  private boolean isEnabledAndParamsValid(final Resource contentResource, final Collection<String> bucketNames, final String configName) {
    return config.enabled()
        && contentResource != null
        // support only configuration buckets
        && bucketNames.contains(CONFIG_BUCKET_NAME)
        // support only the one config name that is used for mapping wcm.io config parameters
        && StringUtils.equals(configName, ParameterProviderBridge.DEFAULT_CONFIG_NAME);
  }

  private String buildResourcePath(String path, String name) {
    return ResourceUtil.normalize(path + "/" + JCR_CONTENT + "/" + name);
  }

  @SuppressWarnings("unchecked")
  private Iterator<String> findConfigRefs(Resource startResource) {
    // collect all context path resources without config ref, and expand to config page path
    Iterator<ContextResource> contextResources = contextPathStrategy.findContextResources(startResource);
    return new FilterIterator(new TransformIterator(contextResources, new Transformer() {
      @Override
      public Object transform(Object input) {
        ContextResource contextResource = (ContextResource)input;
        if (contextResource.getConfigRef() == null) {
          String configPath = getConfigPagePath(contextResource.getResource().getPath());
          log.trace("+ Found reference for context path {}: {}", contextResource.getResource().getPath(), configPath);
          return configPath;
        }
        return null;
      }
    }), PredicateUtils.notNullPredicate());
  }


  // ---------- ConfigurationInheritanceStrategy ----------

  @Override
  @SuppressWarnings("unchecked")
  public Resource getResource(Iterator<Resource> configResources) {
    if (!config.enabled() || !configResources.hasNext()) {
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
    log.trace("! Property inheritance: Merge with properties from {}", next.getPath());
    Map<String, Object> merged = new HashMap<>(next.getValueMap());
    merged.putAll(parentProps);
    return getInheritedProperties(merged, inheritanceChain);
  }


  // ---------- ConfigurationPersistenceStrategy ----------

  @Override
  public Resource getResource(Resource resource) {
    if (!config.enabled() || !isConfigPagePath(resource.getPath())) {
      return null;
    }
    return resource;
  }

  @Override
  public Resource getCollectionParentResource(Resource resource) {
    return getResource(resource);
  }


  @Override
  public Resource getCollectionItemResource(Resource resource) {
    return getResource(resource);
  }

  @Override
  public String getResourcePath(String resourcePath) {
    if (!config.enabled() || !isConfigPagePath(resourcePath)) {
      return null;
    }
    return resourcePath;
  }

  @Override
  public String getCollectionParentResourcePath(String resourcePath) {
    return getResourcePath(resourcePath);
  }


  @Override
  public String getCollectionItemResourcePath(String resourcePath) {
    return getResourcePath(resourcePath);
  }


  @Override
  public String getConfigName(String configName, String relatedConfigPath) {
    if (!config.enabled() || (relatedConfigPath != null && !isConfigPagePath(relatedConfigPath))) {
      return null;
    }
    return configName;
  }


  @Override
  public String getCollectionParentConfigName(String configName, String relatedConfigPath) {
    return getResourcePath(configName);
  }


  @Override
  public String getCollectionItemConfigName(String configName, String relatedConfigPath) {
    return getResourcePath(configName);
  }

  @Override
  public boolean persistConfiguration(ResourceResolver resourceResolver, String configResourcePath, ConfigurationPersistData data) {
    if (!config.enabled() || !isConfigPagePath(configResourcePath)) {
      return false;
    }

    // check of config page exists - and create it otherwise
    String configPagePath = getConfigPagePathFromConfigResourcePath(configResourcePath);
    if (configPagePath == null) {
      return false;
    }
    ensurePage(resourceResolver, configPagePath, config.configPageTemplate());

    // store config data
    getOrCreateResource(resourceResolver, configResourcePath, data.getProperties());
    updatePageLastMod(resourceResolver, configPagePath);
    commit(resourceResolver, configResourcePath);
    return false;
  }

  @Override
  public boolean persistConfigurationCollection(ResourceResolver resourceResolver, String configResourceCollectionParentPath,
      ConfigurationCollectionPersistData data) {
    // not supported for compat mode
    return false;
  }

  @Override
  public boolean deleteConfiguration(ResourceResolver resourceResolver, String configResourcePath) {
    if (!config.enabled() || !isConfigPagePath(configResourcePath)) {
      return false;
    }

    String configPagePath = getConfigPagePathFromConfigResourcePath(configResourcePath);
    if (configPagePath == null) {
      return false;
    }

    Resource resource = resourceResolver.getResource(configResourcePath);
    if (resource != null) {
      try {
        log.trace("! Delete resource {}", resource.getPath());
        resourceResolver.delete(resource);
      }
      catch (PersistenceException ex) {
        throw convertPersistenceException("Unable to delete configuration at " + configResourcePath, ex);
      }
    }
    updatePageLastMod(resourceResolver, configPagePath);
    commit(resourceResolver, configResourcePath);
    return true;
  }

  private String getConfigPagePathFromConfigResourcePath(String configResourcePath) {
    int index = StringUtils.indexOf(configResourcePath, "/jcr:content/");
    if (index <= 0) {
      return null;
    }
    return StringUtils.substring(configResourcePath, 0, index);
  }

  private void ensurePage(ResourceResolver resourceResolver, String pagePath, String template) {
    if (pagePath == null) {
      return;
    }
    Resource resource = resourceResolver.getResource(pagePath);
    if (resource != null) {
      return;
    }

    String parentPath = ResourceUtil.getParent(pagePath);
    String pageName = ResourceUtil.getName(pagePath);
    ensurePage(resourceResolver, ResourceUtil.getParent(pagePath), config.structurePageTemplate());
    Resource parentResource = resourceResolver.getResource(parentPath);
    try {
      if (log.isTraceEnabled()) {
        log.trace("! Create cq:Page node at {}", pagePath);
      }
      // create page directly via Sling API instead of PageManager because page name may contain dots (.)
      Map<String, Object> props = new HashMap<>();
      props.put(JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_PAGE);
      Resource pageResource = resourceResolver.create(parentResource, pageName, props);
      props = new HashMap<String, Object>();
      props.put(JcrConstants.JCR_PRIMARYTYPE, "cq:PageContent");
      props.put(JcrConstants.JCR_TITLE, pageName);
      props.put(NameConstants.PN_TEMPLATE, template);
      resourceResolver.create(pageResource, JcrConstants.JCR_CONTENT, props);
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to create config page at " + pagePath, ex);
    }
  }

  private Resource getOrCreateResource(ResourceResolver resourceResolver, String path, Map<String, Object> properties) {
    try {
      Resource resource = ResourceUtil.getOrCreateResource(resourceResolver, path, (String)null, (String)null, false);
      if (properties != null) {
        replaceProperties(resource, properties);
      }
      return resource;
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to persist configuration to " + path, ex);
    }
  }

  private void replaceProperties(Resource resource, Map<String, Object> properties) {
    if (log.isTraceEnabled()) {
      log.trace("! Store properties for resource {}: {}", resource.getPath(), MapUtil.traceOutput(properties));
    }
    ModifiableValueMap modValueMap = resource.adaptTo(ModifiableValueMap.class);
    if (modValueMap == null) {
      throw new ConfigurationPersistenceAccessDeniedException("No write access: Unable to store configuration data to " + resource.getPath() + ".");
    }
    // remove all existing properties that are not filtered
    Set<String> propertyNamesToRemove = new HashSet<>(modValueMap.keySet());
    PropertiesFilterUtil.removeIgnoredProperties(propertyNamesToRemove);
    for (String propertyName : propertyNamesToRemove) {
      modValueMap.remove(propertyName);
    }
    modValueMap.putAll(properties);
  }

  private void updatePageLastMod(ResourceResolver resourceResolver, String configPagePath) {
    Resource contentResource = resourceResolver.getResource(configPagePath + "/jcr:content");
    if (contentResource != null) {
      ModifiableValueMap contentProps = contentResource.adaptTo(ModifiableValueMap.class);
      if (contentProps == null) {
        throw new ConfigurationPersistenceAccessDeniedException("No write access: Unable to update page " + configPagePath + ".");
      }
      contentProps.put(NameConstants.PN_LAST_MOD, Calendar.getInstance());
      contentProps.put(NameConstants.PN_LAST_MOD_BY, resourceResolver.getAttribute(ResourceResolverFactory.USER));
    }
  }

  private void commit(ResourceResolver resourceResolver, String relatedResourcePath) {
    try {
      resourceResolver.commit();
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to persist configuration changes to " + relatedResourcePath, ex);
    }
  }


  // ---------- Internal ----------

  private String getConfigPagePath(String contextPath) {
    return contextPath + RELATIVE_CONFIG_PATH;
  }

  private boolean isConfigPagePath(String configPath) {
    return CONFIG_PATH_PATTERN.matcher(configPath).matches();
  }

  private ConfigurationPersistenceException convertPersistenceException(String message, PersistenceException ex) {
    if (StringUtils.equals(ex.getCause().getClass().getName(), "javax.jcr.AccessDeniedException")) {
      // detect if commit failed due to read-only access to repository
      return new ConfigurationPersistenceAccessDeniedException("No write access: " + message, ex);
    }
    return new ConfigurationPersistenceException(message, ex);
  }

}
