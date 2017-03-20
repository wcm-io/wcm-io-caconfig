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
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.convertPersistenceException;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.deleteChildren;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.ensurePage;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.getOrCreateResource;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.replaceProperties;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.updatePageLastMod;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.management.multiplexer.ContextPathStrategyMultiplexer;
import org.apache.sling.caconfig.resource.spi.ConfigurationResourceResolvingStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
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

/**
 * AEM-specific persistence strategy that gets only active if a context path is redirected to path
 * <code>/content/.../tools/config</code>.
 * In this case the configuration date is stored in a single page at /tools/config which can be easily activated by
 * editors via the authoring GUI, and the configuration can neatly be packaged together with the content.
 */
@Component(service = { ConfigurationPersistenceStrategy2.class, ConfigurationResourceResolvingStrategy.class },
    property = Constants.SERVICE_RANKING + ":Integer=2000")
@Designate(ocd = ToolsConfigPagePersistenceStrategy.Config.class)
public class ToolsConfigPagePersistenceStrategy implements ConfigurationPersistenceStrategy2, ConfigurationResourceResolvingStrategy {

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
  private static final String PROPERTY_CONFIG_COLLECTION_INHERIT = "sling:configCollectionInherit";

  private static final Logger log = LoggerFactory.getLogger(ToolsConfigPagePersistenceStrategy.class);

  private boolean enabled;

  @Reference
  private ContextPathStrategyMultiplexer contextPathStrategy;


  // --- ConfigurationPersitenceStrategy ---

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
  public Resource getCollectionParentResource(Resource resource) {
    return getResource(resource);
  }

  @Override
  public Resource getCollectionItemResource(Resource resource) {
    return getResource(resource);
  }

  @Override
  public String getResourcePath(String resourcePath) {
    if (!enabled || !isConfigPagePath(resourcePath)) {
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
  public String getConfigName(String configName, Resource relatedConfigResource) {
    if (!enabled || (relatedConfigResource != null && !isConfigPagePath(relatedConfigResource.getPath()))) {
      return null;
    }
    return configName;
  }

  @Override
  public String getCollectionParentConfigName(String configName, Resource relatedConfigResource) {
    return getConfigName(configName, relatedConfigResource);
  }

  @Override
  public String getCollectionItemConfigName(String configName, Resource relatedConfigResource) {
    return getConfigName(configName, relatedConfigResource);
  }

  @Override
  public boolean persistConfiguration(ResourceResolver resolver, String configResourcePath, ConfigurationPersistData data) {
    if (!enabled || !isConfigPagePath(configResourcePath)) {
      return false;
    }
    String path = getResourcePath(configResourcePath);
    ensurePage(resolver, path);

    getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, data.getProperties());

    updatePageLastMod(resolver, path);
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
        throw convertPersistenceException("Unable to delete configuration at " + configResourcePath, ex);
      }
    }
    updatePageLastMod(resolver, configResourcePath);
    commit(resolver);
    return true;
  }

  private boolean isConfigPagePath(String configPath) {
    return CONFIG_PATH_PATTERN.matcher(configPath).matches();
  }


  // --- ConfigurationResourceResolvingStrategy ---

  /**
   * Searches the resource hierarchy upwards for all config references and returns them.
   */
  @SuppressWarnings("unchecked")
  private Iterator<String> findConfigRefs(final Resource startResource, final Collection<String> bucketNames) {

    // collect all context path resources (but filter out those without config reference)
    final Iterator<ContextResource> contextResources = new FilterIterator(contextPathStrategy.findContextResources(startResource),
        new Predicate() {
          @Override
          public boolean evaluate(Object object) {
            ContextResource contextResource = (ContextResource)object;
            return StringUtils.isNotBlank(contextResource.getConfigRef());
          }
        });

    // get config resource path for each context resource, filter out items where not reference could be resolved
    final Iterator<String> configPaths = new TransformIterator(contextResources, new Transformer() {
      @Override
      public Object transform(Object input) {
        final ContextResource contextResource = (ContextResource)input;
        String val = checkPath(contextResource, contextResource.getConfigRef(), bucketNames);
        if (val != null) {
          log.trace("+ Found reference for context path {}: {}", contextResource.getResource().getPath(), val);
        }
        return val;
      }
    });
    return new FilterIterator(configPaths, PredicateUtils.notNullPredicate());
  }

  private String checkPath(final ContextResource contextResource, final String checkRef, final Collection<String> bucketNames) {
    // combine full path if relativeRef is present
    String ref = ResourceUtil.normalize(checkRef);

    for (String bucketName : bucketNames) {
      String notAllowedPostfix = "/" + bucketName;
      if (ref != null && ref.endsWith(notAllowedPostfix)) {
        log.warn("Ignoring reference to {} from {} - Probably misconfigured as it ends with '{}'",
            contextResource.getConfigRef(), contextResource.getResource().getPath(), notAllowedPostfix);
        ref = null;
      }
    }

    if (ref != null && !CONFIG_PATH_PATTERN.matcher(ref).matches()) {
      log.debug("Ignoring reference to {} from {} - not in allowed paths.",
          contextResource.getConfigRef(), contextResource.getResource().getPath());
      ref = null;
    }

    return ref;
  }

  private boolean isEnabledAndParamsValid(final Resource contentResource, final Collection<String> bucketNames, final String configName) {
    return enabled && contentResource != null;
  }

  private String buildResourcePath(String path, String name) {
    return ResourceUtil.normalize(path + "/" + name);
  }

  @Override
  public Resource getResource(final Resource contentResource, final Collection<String> bucketNames, final String configName) {
    Iterator<Resource> resources = getResourceInheritanceChain(contentResource, bucketNames, configName);
    if (resources != null && resources.hasNext()) {
      return resources.next();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Iterator<Resource> getResourceInheritanceChainInternal(final Collection<String> bucketNames, final String configName,
      final Iterator<String> paths, final ResourceResolver resourceResolver) {

    // find all matching items among all configured paths
    Iterator<Resource> matchingResources = IteratorUtils.transformedIterator(paths, new Transformer() {

      @Override
      public Object transform(Object input) {
        String path = (String)input;
        for (String bucketName : bucketNames) {
          final String name = bucketName + "/" + configName;
          final String configPath = buildResourcePath(path, name);
          Resource resource = resourceResolver.getResource(configPath);
          if (resource != null) {
            log.trace("+ Found matching config resource for inheritance chain: {}", configPath);
            return resource;
          }
          else {
            log.trace("- No matching config resource for inheritance chain: {}", configPath);
          }
        }
        return null;
      }
    });
    Iterator<Resource> result = IteratorUtils.filteredIterator(matchingResources, PredicateUtils.notNullPredicate());
    if (result.hasNext()) {
      return result;
    }
    return null;
  }

  @Override
  public Iterator<Resource> getResourceInheritanceChain(Resource contentResource, Collection<String> bucketNames, String configName) {
    if (!isEnabledAndParamsValid(contentResource, bucketNames, configName)) {
      return null;
    }
    final ResourceResolver resourceResolver = contentResource.getResourceResolver();

    Iterator<String> paths = findConfigRefs(contentResource, bucketNames);
    return getResourceInheritanceChainInternal(bucketNames, configName, paths, resourceResolver);
  }

  private Collection<Resource> getResourceCollectionInternal(final Collection<String> bucketNames, final String configName,
      Iterator<String> paths, ResourceResolver resourceResolver) {

    final Map<String, Resource> result = new LinkedHashMap<>();

    boolean inherit = false;
    while (paths.hasNext()) {
      final String path = paths.next();

      Resource item = null;
      for (String bucketName : bucketNames) {
        String name = bucketName + "/" + configName;
        String configPath = buildResourcePath(path, name);
        item = resourceResolver.getResource(configPath);
        if (item == null) {
          log.trace("- No collection parent resource found: {}", configPath);
        }
      }

      if (item != null) {
        log.trace("o Check children of collection parent resource: {}", item.getPath());
        if (item.hasChildren()) {
          for (Resource child : item.getChildren()) {
            if (isValidResourceCollectionItem(child)
                && !result.containsKey(child.getName())) {
              log.trace("+ Found collection resource item {}", child.getPath());
              result.put(child.getName(), child);
            }
          }
        }

        // check collection inheritance mode on current level - should we check on next-highest level as well?
        final ValueMap valueMap = item.getValueMap();
        inherit = valueMap.get(PROPERTY_CONFIG_COLLECTION_INHERIT, false);
        if (!inherit) {
          break;
        }
      }
    }

    return result.values();
  }

  @Override
  public Collection<Resource> getResourceCollection(final Resource contentResource, final Collection<String> bucketNames, final String configName) {
    if (!isEnabledAndParamsValid(contentResource, bucketNames, configName)) {
      return null;
    }
    Iterator<String> paths = findConfigRefs(contentResource, bucketNames);
    Collection<Resource> result = getResourceCollectionInternal(bucketNames, configName, paths, contentResource.getResourceResolver());
    if (!result.isEmpty()) {
      return result;
    }
    else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Collection<Iterator<Resource>> getResourceCollectionInheritanceChain(final Resource contentResource,
      final Collection<String> bucketNames, final String configName) {
    if (!isEnabledAndParamsValid(contentResource, bucketNames, configName)) {
      return null;
    }
    final ResourceResolver resourceResolver = contentResource.getResourceResolver();
    final List<String> paths = IteratorUtils.toList(findConfigRefs(contentResource, bucketNames));

    // get resource collection with respect to collection inheritance
    Collection<Resource> resourceCollection = getResourceCollectionInternal(bucketNames, configName, paths.iterator(), resourceResolver);

    // get inheritance chain for each item found
    // yes, this resolves the closest item twice, but is the easiest solution to combine both logic aspects
    Iterator<Iterator<Resource>> result = IteratorUtils.transformedIterator(resourceCollection.iterator(), new Transformer() {

      @Override
      public Object transform(Object input) {
        Resource item = (Resource)input;
        return getResourceInheritanceChainInternal(bucketNames, configName + "/" + item.getName(), paths.iterator(), resourceResolver);
      }
    });
    if (result.hasNext()) {
      return IteratorUtils.toList(result);
    }
    else {
      return null;
    }
  }

  private boolean isValidResourceCollectionItem(Resource resource) {
    // do not include jcr:content nodes in resource collection list
    return !StringUtils.equals(resource.getName(), "jcr:content");
  }

  @Override
  public String getResourcePath(Resource contentResource, String bucketName, String configName) {
    if (!isEnabledAndParamsValid(contentResource, Collections.singleton(bucketName), configName)) {
      return null;
    }
    String name = bucketName + "/" + configName;

    Iterator<String> configPaths = this.findConfigRefs(contentResource, Collections.singleton(bucketName));
    if (configPaths.hasNext()) {
      String configPath = buildResourcePath(configPaths.next(), name);
      log.trace("+ Building configuration path for name '{}' for resource {}: {}", name, contentResource.getPath(), configPath);
      return configPath;
    }
    else {
      log.trace("- No configuration path for name '{}' found for resource {}", name, contentResource.getPath());
      return null;
    }
  }

  @Override
  public String getResourceCollectionParentPath(Resource contentResource, String bucketName, String configName) {
    return getResourcePath(contentResource, bucketName, configName);
  }

}
