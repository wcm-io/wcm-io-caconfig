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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;

import io.wcm.config.spi.ParameterPersistenceProvider;

/**
 * Common functionality for storing configuration in a configuration page.
 */
abstract class AbstractConfigPagePersistenceProvider implements ParameterPersistenceProvider {

  static final String CONFIG_RESOURCE_NAME = "config";

  private final Logger log = LoggerFactory.getLogger(getClass());

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

  protected abstract boolean isEnabled();

  protected abstract String getConfigPagePath(String configurationId);

  protected abstract String getConfigPageTemplate();

  protected abstract String getStructurePageTemplate();

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

}
