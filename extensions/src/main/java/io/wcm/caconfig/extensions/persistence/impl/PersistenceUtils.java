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

import java.util.Calendar;
import java.util.HashMap;
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
import org.apache.sling.caconfig.spi.ConfigurationPersistenceAccessDeniedException;
import org.apache.sling.caconfig.spi.ConfigurationPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

final class PersistenceUtils {

  private static final String DEFAULT_FOLDER_NODE_TYPE = "sling:Folder";
  private static final Pattern PAGE_PATH_PATTERN = Pattern.compile("^(.*)/" + Pattern.quote(JCR_CONTENT) + "(/.*)?$");
  private static final Pattern JCR_CONTENT_PATTERN = Pattern.compile("^(.*/)?" + Pattern.quote(JCR_CONTENT) + "(/.*)?$");

  private static final Logger log = LoggerFactory.getLogger(PersistenceUtils.class);

  private PersistenceUtils() {
    // static methods only
  }

  public static boolean containsJcrContent(String path) {
    return JCR_CONTENT_PATTERN.matcher(path).matches();
  }

  public static void ensurePage(ResourceResolver resolver, String configResourcePath) {
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
    String pageName = ResourceUtil.getName(pagePath);
    Resource parentResource = getOrCreateResource(resolver, parentPath, DEFAULT_FOLDER_NODE_TYPE, null);
    try {
      if (log.isTraceEnabled()) {
        log.trace("! Create cq:Page node at {}", pagePath);
      }

      // create page directly via Sling API instead of PageManager because page name may contain dots (.)
      Map<String, Object> props = new HashMap<String, Object>();
      props.put(JcrConstants.JCR_PRIMARYTYPE, NameConstants.NT_PAGE);
      Resource pageResource = resolver.create(parentResource, pageName, props);
      props = new HashMap<String, Object>();
      props.put(JcrConstants.JCR_PRIMARYTYPE, "cq:PageContent");
      props.put(JcrConstants.JCR_TITLE, pageName);
      resolver.create(pageResource, JcrConstants.JCR_CONTENT, props);
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to create config page at " + pagePath, ex);
    }
  }

  public static Resource getOrCreateResource(ResourceResolver resolver, String path, String defaultNodeType, Map<String, Object> properties) {
    try {
      Resource resource = ResourceUtil.getOrCreateResource(resolver, path, defaultNodeType, defaultNodeType, false);
      if (properties != null) {
        replaceProperties(resource, properties);
      }
      return resource;
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to create resource at " + path, ex);
    }
  }

  public static void deleteChildren(Resource resource) {
    ResourceResolver resolver = resource.getResourceResolver();
    try {
      for (Resource child : resource.getChildren()) {
        resolver.delete(child);
      }
    }
    catch (PersistenceException ex) {
      throw convertPersistenceException("Unable to remove children from " + resource.getPath(), ex);
    }
  }

  public static void replaceProperties(Resource resource, Map<String, Object> properties) {
    if (log.isTraceEnabled()) {
      log.trace("! Store properties for resource {}: {}", resource.getPath(), properties);
    }
    ModifiableValueMap modValueMap = resource.adaptTo(ModifiableValueMap.class);
    if (modValueMap == null) {
      throw new ConfigurationPersistenceAccessDeniedException("No write access: Unable to store configuration data to " + resource.getPath() + ".");
    }
    // remove all existing properties that do not have jcr: namespace
    for (String propertyName : new HashSet<>(modValueMap.keySet())) {
      if (StringUtils.startsWith(propertyName, "jcr:")) {
        continue;
      }
      modValueMap.remove(propertyName);
    }
    modValueMap.putAll(properties);
  }

  public static void updatePageLastMod(ResourceResolver resolver, String configResourcePath) {
    Matcher matcher = PAGE_PATH_PATTERN.matcher(configResourcePath);
    if (!matcher.matches()) {
      return;
    }
    String pagePath = matcher.group(1);
    Resource contentResource = resolver.getResource(pagePath + "/" + JCR_CONTENT);
    if (contentResource != null) {
      ModifiableValueMap contentProps = contentResource.adaptTo(ModifiableValueMap.class);
      if (contentProps == null) {
        throw new ConfigurationPersistenceAccessDeniedException("No write access: Unable to update page " + configResourcePath + ".");
      }
      contentProps.put(NameConstants.PN_LAST_MOD, Calendar.getInstance());
      contentProps.put(NameConstants.PN_LAST_MOD_BY, resolver.getAttribute(ResourceResolverFactory.USER));
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

  public static ConfigurationPersistenceException convertPersistenceException(String message, PersistenceException ex) {
    if (StringUtils.equals(ex.getCause().getClass().getName(), "javax.jcr.AccessDeniedException")) {
      // detect if commit failed due to read-only access to repository
      return new ConfigurationPersistenceAccessDeniedException("No write access: " + message, ex);
    }
    return new ConfigurationPersistenceException(message, ex);
  }

}
