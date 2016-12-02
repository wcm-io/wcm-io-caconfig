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

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import io.wcm.config.spi.ParameterPersistenceProvider;

/**
 * Persistence provider that stores configuration values in pages in a path tools/config relative to the config id.
 */
@Component(immediate = true, metatype = true,
label = "wcm.io Configuration Persistence Provider: /tools/config Pages",
description = "Allows to read and store configurations in /tools/config pages.")
@Service(ParameterPersistenceProvider.class)
public final class ToolsConfigPagePersistenceProvider extends AbstractConfigPagePersistenceProvider {

  static final String RELATIVE_CONFIG_PATH = "/tools/config";

  @Property(label = "Enabled", boolValue = ToolsConfigPagePersistenceProvider.DEFAULT_ENABLED,
      description = "Enable parameter persistence provider")
  static final String PROPERTY_ENABLED = "enabled";
  static final boolean DEFAULT_ENABLED = false;

  @Property(label = "Service Ranking", intValue = ToolsConfigPagePersistenceProvider.DEFAULT_RANKING,
      description = "Priority of parameter persistence providers (lower = higher priority)",
      propertyPrivate = false)
  static final String PROPERTY_RANKING = Constants.SERVICE_RANKING;
  static final int DEFAULT_RANKING = 2000;

  @Property(label = "Config Template",
      description = "Template that is used for a configuration page.")
  static final String PROPERTY_CONFIG_PAGE_TEMPLATE = "configPageTemplate";

  @Property(label = "Structure Template",
      description = "Template that is used for the tools page.")
  static final String PROPERTY_STRUCTURE_PAGE_TEMPLATE = "structurePageTemplate";

  private boolean enabled;
  private String configPageTemplate;
  private String structurePageTemplate;

  @Override
  protected boolean isEnabled() {
    return enabled;
  }

  @Override
  protected String getConfigPagePath(String configurationId) {
    return configurationId + RELATIVE_CONFIG_PATH;
  }

  @Override
  protected String getConfigPageTemplate() {
    return configPageTemplate;
  }

  @Override
  protected String getStructurePageTemplate() {
    return structurePageTemplate;
  }

  @Activate
  void activate(final ComponentContext ctx) {
    Dictionary config = ctx.getProperties();
    this.enabled = PropertiesUtil.toBoolean(config.get(PROPERTY_ENABLED), DEFAULT_ENABLED);
    this.configPageTemplate = PropertiesUtil.toString(config.get(PROPERTY_CONFIG_PAGE_TEMPLATE), null);
    this.structurePageTemplate = PropertiesUtil.toString(config.get(PROPERTY_STRUCTURE_PAGE_TEMPLATE), null);
  }

}
