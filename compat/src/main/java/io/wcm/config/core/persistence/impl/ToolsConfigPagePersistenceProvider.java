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

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.wcm.config.spi.ParameterPersistenceProvider;

/**
 * Persistence provider that stores configuration values in pages in a path tools/config relative to the config id.
 */
@Component(immediate = true, service = ParameterPersistenceProvider.class)
@Designate(ocd = ToolsConfigPagePersistenceProvider.Config.class)
public final class ToolsConfigPagePersistenceProvider extends AbstractConfigPagePersistenceProvider {

  static final String RELATIVE_CONFIG_PATH = "/tools/config";

  @ObjectClassDefinition(name = "wcm.io Configuration Persistence Provider: /tools/config Pages",
      description = "Allows to read and store configurations in /tools/config pages.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Enable parameter persistence provider.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Service Ranking", description = "Priority of parameter persistence providers (lower = higher priority).")
    int service_ranking() default 2000;

    @AttributeDefinition(name = "Config Template", description = "Template that is used for a configuration page.")
    String configPageTemplate();

    @AttributeDefinition(name = "Structure Template", description = "Template that is used for the tools page.")
    String structurePageTemplate();

  }

  private Config config;

  @Override
  protected boolean isEnabled() {
    return config.enabled();
  }

  @Override
  protected String getConfigPagePath(String configurationId) {
    return configurationId + RELATIVE_CONFIG_PATH;
  }

  @Override
  protected String getConfigPageTemplate() {
    return config.configPageTemplate();
  }

  @Override
  protected String getStructurePageTemplate() {
    return config.structurePageTemplate();
  }

  @Activate
  void activate(Config cfg) {
    this.config = cfg;
  }

}
