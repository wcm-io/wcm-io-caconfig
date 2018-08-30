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
import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.caconfig.management.ConfigurationManagementSettings;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceAccessDeniedException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

final class PersistenceUtils {

  private static final String DEFAULT_FOLDER_NODE_TYPE = "sling:Folder";
  private static final String DEFAULT_FOLDER_NODE_TYPE_IN_PAGE = JcrConstants.NT_UNSTRUCTURED;
  private static final Pattern PAGE_PATH_PATTERN = Pattern.compile("^(.*)/" + Pattern.quote(JCR_CONTENT) + "(/.*)?$");
  private static final Pattern JCR_CONTENT_PATTERN = Pattern.compile("^(.*/)?" + Pattern.quote(JCR_CONTENT) + "(/.*)?$");

  private static final Logger log = LoggerFactory.getLogger(PersistenceUtils.class);

  private PersistenceUtils() {
    // static methods only
  }

  public static boolean containsJcrContent(String path) {
    if (path == null) {
      return false;
    }
    return JCR_CONTENT_PATTERN.matcher(path).matches();
  }

  /**
   * Ensure that a containing page exists for the given path inside a content page.
   * If no containing page exists a page is created with the path before /jcr:content/*.
   * If the path does not contain /jcr:content nothing is done.
   * @param resolver Resource resolver
   * @param configResourcePath Configuration resource path
   * @param resourceType Resource type for page (if not template is set)
   * @param configurationManagementSettings Configuration management settings
   */
  public static void ensureContainingPage(ResourceResolver resolver, String configResourcePath,
      String resourceType, ConfigurationManagementSettings configurationManagementSettings) {
    ensureContainingPage(resolver, configResourcePath, null, resourceType, null, configurationManagementSettings);
  }

  /**
   * Ensure that a containing page exists for the given path inside a content page.
   * If no containing page exists a page is created with the path before /jcr:content/*.
   * If the path does not contain /jcr:content nothing is done.
   * @param resolver Resource resolver
   * @param configResourcePath Configuration resource path
   * @param template Template for page
   * @param resourceType Resource type for page (if not template is set)
   * @param parentTemplate Template for parent/intermediate pages
   * @param configurationManagementSettings Configuration management settings
   */
  public static void ensureContainingPage(ResourceResolver resolver, String configResourcePath,
      String template, String resourceType, String parentTemplate,
      ConfigurationManagementSettings configurationManagementSettings) {
    Matcher matcher = PAGE_PATH_PATTERN.matcher(configResourcePath);
    if (!matcher.matches()) {
      return;
    }
    String pagePath = matcher.group(1);
    ensurePage(resolver, pagePath, template, resourceType, parentTemplate, configurationManagementSettings);
  }

  /**
   * Ensure that a page at the given path exists, if the path is not already contained in a page.
   * @param resolver Resource resolver
   * @param pagePath Page path
   * @param resourceType Resource type for page (if not template is set)
   * @param configurationManagementSettings Configuration management settings
   * @return Resource for AEM page or resource inside a page.
   */
  public static Resource ensurePageIfNotContainingPage(ResourceResolver resolver, String pagePath,
      String resourceType, ConfigurationManagementSettings configurationManagementSettings) {
    Matcher matcher = PAGE_PATH_PATTERN.matcher(pagePath);
    if (matcher.matches()) {
      return getOrCreateResource(resolver, pagePath, DEFAULT_FOLDER_NODE_TYPE_IN_PAGE, null, configurationManagementSettings);
    }
    return ensurePage(resolver, pagePath, null, resourceType, null, configurationManagementSettings);
  }

  private static Resource ensurePage(ResourceResolver resolver, String pagePath,
      String template, String resourceType, String parentTemplate,
      ConfigurationManagementSettings configurationManagementSettings) {
    // check if page or resource already exists
    Resource resource = resolver.getResource(pagePath);
    if (resource != null) {
      return resource;
    }

    // ensure parent page or resource exists
    String parentPath = ResourceUtil.getParent(pagePath);
    String pageName = ResourceUtil.getName(pagePath);
    Resource parentResource;
    if (StringUtils.isNotEmpty(parentTemplate)) {
      parentResource = ensurePage(resolver, parentPath, parentTemplate, null, parentTemplate, configurationManagementSettings);
    }
    else {
      parentResource = getOrCreateResource(resolver, parentPath, DEFAULT_FOLDER_NODE_TYPE, null, configurationManagementSettings);
    }

    // create page
    return createPage(resolver, parentResource, pageName, template, resourceType);
  }

  private static Resource createPage(ResourceResolver resolver, Resource parentResource, String pageName,
      String template, String resourceType) {
    String pagePath = parentResource.getPath() + "/" + pageName;
    log.trace("! Create cq:Page node at {}", pagePath);
    try {
      // create page directly via Sling API instead of PageManager because page name may contain dots (.)
      Map<String, Object> props = new HashMap<>();
      props.put(JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_PAGE);
      Resource pageResource = resolver.create(parentResource, pageName, props);

      // create jcr:content node
      props = new HashMap<>();
      props.put(JcrConstants.JCR_PRIMARYTYPE, "cq:PageContent");
      if (StringUtils.isNotEmpty(template)) {
        applyPageTemplate(resolver, props, pageName, template);
      }
      if (StringUtils.isNotEmpty(resourceType) && props.get(PROPERTY_RESOURCE_TYPE) == null) {
        props.put(PROPERTY_RESOURCE_TYPE, resourceType);
      }
      resolver.create(pageResource, JCR_CONTENT, props);

      return pageResource;
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to create config page at " + pagePath, ex);
    }
  }

  private static void applyPageTemplate(ResourceResolver resolver, Map<String, Object> props, String pageName, String template) {
    // set template
    props.put(NameConstants.PN_TEMPLATE, template);

    // also set title for author when template is set
    props.put(JcrConstants.JCR_TITLE, pageName);

    // get sling:resourceType from template definition
    Resource templateContentResource = resolver.getResource(template + "/" + JCR_CONTENT);
    if (templateContentResource != null) {
      props.put(PROPERTY_RESOURCE_TYPE, templateContentResource.getValueMap().get(PROPERTY_RESOURCE_TYPE, String.class));
    }
  }

  public static Resource getOrCreateResource(ResourceResolver resolver, String path, String defaultNodeType, Map<String, Object> properties,
      ConfigurationManagementSettings configurationManagementSettings) {
    try {
      Resource resource = ResourceUtil.getOrCreateResource(resolver, path, defaultNodeType, defaultNodeType, false);
      if (properties != null) {
        replaceProperties(resource, properties, configurationManagementSettings);
      }
      return resource;
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to create resource at " + path, ex);
    }
  }

  /**
   * Delete children that are no longer contained in list of collection items.
   * @param resource Parent resource
   * @param data List of collection items
   */
  public static void deleteChildrenNotInCollection(Resource resource, ConfigurationCollectionPersistData data) {
    Set<String> collectionItemNames = data.getItems().stream()
        .map(item -> item.getCollectionItemName())
        .collect(Collectors.toSet());

    for (Resource child : resource.getChildren()) {
      if (!collectionItemNames.contains(child.getName()) && !StringUtils.equals(JCR_CONTENT, child.getName())) {
        deletePageOrResource(child);
      }
    }
  }

  @SuppressWarnings({ "unused", "null" })
  public static void replaceProperties(Resource resource, Map<String, Object> properties,
      ConfigurationManagementSettings configurationManagementSettings) {
    if (log.isTraceEnabled()) {
      log.trace("! Store properties for resource {}: {}", resource.getPath(), properties);
    }
    ModifiableValueMap modValueMap = resource.adaptTo(ModifiableValueMap.class);
    if (modValueMap == null) {
      throw new ConfigurationPersistenceAccessDeniedException("No write access: Unable to store configuration data to " + resource.getPath() + ".");
    }

    // remove all existing properties that are not filtered
    Set<String> propertyNamesToRemove = new HashSet<>(modValueMap.keySet());
    PropertiesFilterUtil.removeIgnoredProperties(propertyNamesToRemove, configurationManagementSettings);
    for (String propertyName : propertyNamesToRemove) {
      modValueMap.remove(propertyName);
    }

    modValueMap.putAll(properties);
  }

  @SuppressWarnings({ "unused", "null" })
  public static void updatePageLastMod(ResourceResolver resolver, String configResourcePath) {
    PageManager pageManager = resolver.adaptTo(PageManager.class);
    Page page = pageManager.getContainingPage(configResourcePath);
    if (page == null) {
      return;
    }
    Resource contentResource = page.getContentResource();
    if (contentResource != null) {
      ModifiableValueMap contentProps = contentResource.adaptTo(ModifiableValueMap.class);
      if (contentProps == null) {
        throw new ConfigurationPersistenceAccessDeniedException("No write access: Unable to update page " + configResourcePath + ".");
      }

      Object user = resolver.getAttribute(ResourceResolverFactory.USER);
      Calendar now = Calendar.getInstance();

      contentProps.put(NameConstants.PN_LAST_MOD, now);
      contentProps.put(NameConstants.PN_LAST_MOD_BY, user);

      // check if resource has cq:lastModified because it is created in site admin
      if (contentProps.containsKey(NameConstants.PN_PAGE_LAST_MOD)) {
        contentProps.put(NameConstants.PN_PAGE_LAST_MOD, now);
        contentProps.put(NameConstants.PN_PAGE_LAST_MOD_BY, user);
      }
    }
  }

  public static void commit(ResourceResolver resourceResolver, String relatedResourcePath) {
    try {
      resourceResolver.commit();
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to persist configuration changes to " + relatedResourcePath, ex);
    }
  }

  /**
   * If the given resource points to an AEM page, delete the page using PageManager.
   * Otherwise delete the resource using ResourceResolver.
   * @param resource Resource to delete
   */
  @SuppressWarnings({ "null", "unused" })
  public static void deletePageOrResource(Resource resource) {
    Page configPage = resource.adaptTo(Page.class);
    if (configPage != null) {
      try {
        log.trace("! Delete page {}", configPage.getPath());
        PageManager pageManager = configPage.getPageManager();
        pageManager.delete(configPage, false);
      }
      catch (WCMException ex) {
        throw convertWCMException("Unable to delete configuration page at " + resource.getPath(), ex);
      }
    }
    else {
      try {
        log.trace("! Delete resource {}", resource.getPath());
        resource.getResourceResolver().delete(resource);
      }
      catch (PersistenceException ex) {
        throw convertPersistenceException("Unable to delete configuration resource at " + resource.getPath(), ex);
      }
    }
  }

  private static ConfigurationPersistenceException convertWCMException(String message, WCMException ex) {
    String causeClsName = ex.getCause().getClass().getName();
    if (StringUtils.equals(causeClsName, "com.day.cq.replication.AccessDeniedException")
        || StringUtils.equals(causeClsName, "javax.jcr.AccessDeniedException")) {
      return new ConfigurationPersistenceAccessDeniedException("No write access: " + message, ex);
    }
    return new ConfigurationPersistenceException(message, ex);
  }

  private static ConfigurationPersistenceException convertPersistenceException(String message, PersistenceException ex) {
    if (StringUtils.equals(ex.getCause().getClass().getName(), "javax.jcr.AccessDeniedException")) {
      // detect if commit failed due to read-only access to repository
      return new ConfigurationPersistenceAccessDeniedException("No write access: " + message, ex);
    }
    return new ConfigurationPersistenceException(message, ex);
  }

}
