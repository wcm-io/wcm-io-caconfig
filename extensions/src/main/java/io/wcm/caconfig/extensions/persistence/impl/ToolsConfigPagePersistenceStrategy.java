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
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.deleteChildrenNotInCollection;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.deletePageOrResource;
import static io.wcm.caconfig.extensions.persistence.impl.PersistenceUtils.ensureContainingPage;
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

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.TransformIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.management.ConfigurationManagementSettings;
import org.apache.sling.caconfig.management.multiplexer.ContextPathStrategyMultiplexer;
import org.apache.sling.caconfig.resource.spi.ConfigurationResourceResolvingStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceStrategy2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageManagerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * AEM-specific persistence strategy that gets only active if a context path is redirected to path
 * <code>/content/.../tools/config</code>.
 * In this case the configuration date is stored in a single page at /tools/config which can be easily activated by
 * editors via the authoring GUI, and the configuration can neatly be packaged together with the content.
 */
@Component(service = { ConfigurationPersistenceStrategy2.class, ConfigurationResourceResolvingStrategy.class })
@Designate(ocd = ToolsConfigPagePersistenceStrategy.Config.class)
public class ToolsConfigPagePersistenceStrategy implements ConfigurationPersistenceStrategy2, ConfigurationResourceResolvingStrategy {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Persistence Strategy: Tools Config Page",
      description = "Stores Context-Aware Configuration in a single AEM content page at /tools/config.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled",
        description = "Enable this persistence strategy.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Config Template",
        description = "Template that is used for a configuration page.")
    String configPageTemplate();

    @AttributeDefinition(name = "Structure Template",
        description = "Template that is used for the tools page.")
    String structurePageTemplate();

    @AttributeDefinition(name = "Service Ranking",
        description = "Priority of persistence strategy (higher = higher priority).")
    int service_ranking() default 2000;

  }

  private static final String RELATIVE_CONFIG_PATH = "/tools/config/jcr:content";
  private static final Pattern CONFIG_PATH_PATTERN = Pattern.compile("^.*" + Pattern.quote(RELATIVE_CONFIG_PATH) + "(/.*)?$");
  private static final String DEFAULT_CONFIG_NODE_TYPE = NT_UNSTRUCTURED;
  private static final String PROPERTY_CONFIG_COLLECTION_INHERIT = "sling:configCollectionInherit";

  private static final Logger log = LoggerFactory.getLogger(ToolsConfigPagePersistenceStrategy.class);

  private boolean enabled;
  private Config config;

  @Reference
  private ContextPathStrategyMultiplexer contextPathStrategy;
  @Reference
  private ConfigurationManagementSettings configurationManagementSettings;
  @Reference
  private PageManagerFactory pageManagerFactory;

  // --- ConfigurationPersitenceStrategy ---

  @Activate
  void activate(Config value) {
    this.enabled = value.enabled();
    this.config = value;
  }

  @Override
  public Resource getResource(@NotNull Resource resource) {
    if (!enabled || !isConfigPagePath(resource.getPath())) {
      return null;
    }
    return resource;
  }

  @Override
  public Resource getCollectionParentResource(@NotNull Resource resource) {
    return getResource(resource);
  }

  @Override
  public Resource getCollectionItemResource(@NotNull Resource resource) {
    return getResource(resource);
  }

  @Override
  public String getResourcePath(@NotNull String resourcePath) {
    if (!enabled || !isConfigPagePath(resourcePath)) {
      return null;
    }
    return resourcePath;
  }

  @Override
  public String getCollectionParentResourcePath(@NotNull String resourcePath) {
    return getResourcePath(resourcePath);
  }

  @Override
  public String getCollectionItemResourcePath(@NotNull String resourcePath) {
    return getResourcePath(resourcePath);
  }

  @Override
  public String getConfigName(@NotNull String configName, @Nullable String relatedConfigPath) {
    if (!enabled || (relatedConfigPath != null && !isConfigPagePath(relatedConfigPath))) {
      return null;
    }
    return configName;
  }

  @Override
  public String getCollectionParentConfigName(@NotNull String configName, @Nullable String relatedConfigPath) {
    return getConfigName(configName, relatedConfigPath);
  }

  @Override
  public String getCollectionItemConfigName(@NotNull String configName, @Nullable String relatedConfigPath) {
    return getConfigName(configName, relatedConfigPath);
  }

  @Override
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public boolean persistConfiguration(@NotNull ResourceResolver resolver, @NotNull String configResourcePath,
      @NotNull ConfigurationPersistData data) {
    if (!enabled || !isConfigPagePath(configResourcePath)) {
      return false;
    }
    String path = getResourcePath(configResourcePath);
    ensureContainingPage(resolver, path, config.configPageTemplate(), null, config.structurePageTemplate(), configurationManagementSettings);

    getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, data.getProperties(), configurationManagementSettings);

    PageManager pageManager = pageManagerFactory.getPageManager(resolver);
    updatePageLastMod(resolver, pageManager, path);
    commit(resolver, configResourcePath);
    return true;
  }

  @Override
  public boolean persistConfigurationCollection(@NotNull ResourceResolver resolver, @NotNull String configResourceCollectionParentPath,
      @NotNull ConfigurationCollectionPersistData data) {
    if (!enabled || !isConfigPagePath(configResourceCollectionParentPath)) {
      return false;
    }
    ensureContainingPage(resolver, configResourceCollectionParentPath, config.configPageTemplate(), null, config.structurePageTemplate(),
        configurationManagementSettings);
    Resource configResourceParent = getOrCreateResource(resolver, configResourceCollectionParentPath, DEFAULT_CONFIG_NODE_TYPE, ValueMap.EMPTY,
        configurationManagementSettings);

    // delete existing children no longer in the list
    deleteChildrenNotInCollection(configResourceParent, data);
    for (ConfigurationPersistData item : data.getItems()) {
      String path = configResourceParent.getPath() + "/" + item.getCollectionItemName();
      getOrCreateResource(resolver, path, DEFAULT_CONFIG_NODE_TYPE, item.getProperties(), configurationManagementSettings);
    }

    // if resource collection parent properties are given replace them as well
    if (data.getProperties() != null) {
      replaceProperties(configResourceParent, data.getProperties(), configurationManagementSettings);
    }

    PageManager pageManager = pageManagerFactory.getPageManager(resolver);
    updatePageLastMod(resolver, pageManager, configResourceCollectionParentPath);
    commit(resolver, configResourceCollectionParentPath);
    return true;
  }

  @Override
  public boolean deleteConfiguration(@NotNull ResourceResolver resolver, @NotNull String configResourcePath) {
    if (!enabled || !isConfigPagePath(configResourcePath)) {
      return false;
    }
    Resource resource = resolver.getResource(configResourcePath);
    if (resource != null) {
      deletePageOrResource(resource);
    }
    PageManager pageManager = pageManagerFactory.getPageManager(resolver);
    updatePageLastMod(resolver, pageManager, configResourcePath);
    commit(resolver, configResourcePath);
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
  private Iterator<String> findConfigRefs(@NotNull final Resource startResource, @NotNull final Collection<String> bucketNames) {

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
        log.debug("Ignoring reference to {} from {} - Probably misconfigured as it ends with '{}'",
            contextResource.getConfigRef(), contextResource.getResource().getPath(), notAllowedPostfix);
        ref = null;
      }
    }

    return ref;
  }

  @SuppressWarnings("unused")
  private boolean isEnabledAndParamsValid(final Resource contentResource, final Collection<String> bucketNames, final String configName) {
    return enabled && contentResource != null;
  }

  private String buildResourcePath(String path, String name) {
    return ResourceUtil.normalize(path + "/" + name);
  }

  @Override
  public Resource getResource(@NotNull final Resource contentResource, @NotNull final Collection<String> bucketNames, @NotNull final String configName) {
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
  public Iterator<Resource> getResourceInheritanceChain(@NotNull Resource contentResource, @NotNull Collection<String> bucketNames,
      @NotNull String configName) {
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
        if (item != null) {
          break;
        }
        else {
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
  public Collection<Resource> getResourceCollection(@NotNull final Resource contentResource, @NotNull final Collection<String> bucketNames,
      @NotNull final String configName) {
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
  public Collection<Iterator<Resource>> getResourceCollectionInheritanceChain(@NotNull final Resource contentResource,
      @NotNull final Collection<String> bucketNames, @NotNull final String configName) {
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
  public String getResourcePath(@NotNull Resource contentResource, @NotNull String bucketName, @NotNull String configName) {
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
  public String getResourceCollectionParentPath(@NotNull Resource contentResource, @NotNull String bucketName, @NotNull String configName) {
    return getResourcePath(contentResource, bucketName, configName);
  }

}
