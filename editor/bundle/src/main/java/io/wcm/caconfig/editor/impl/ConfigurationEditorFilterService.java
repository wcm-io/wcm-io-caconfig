/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.caconfig.editor.impl;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.caconfig.editor.ConfigurationEditorFilter;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Aggregates configuration filters via Context-Aware services.
 */
@Component(service = ConfigurationEditorFilterService.class)
public class ConfigurationEditorFilterService {

  @Reference
  private ContextAwareServiceResolver serviceResolver;

  /**
   * Allow to add configurations with this name in the configuration editor.
   * @param contextResource Content resource
   * @param configName Configuration name
   * @return if true, the configuration is offered in the "add configuration" dialog
   */
  public boolean allowAdd(@NotNull Resource contextResource, @NotNull String configName) {
    return serviceResolver.resolveAll(ConfigurationEditorFilter.class, contextResource).getServices()
        .filter(filter -> !filter.allowAdd(configName))
        .count() == 0;
  }

}
