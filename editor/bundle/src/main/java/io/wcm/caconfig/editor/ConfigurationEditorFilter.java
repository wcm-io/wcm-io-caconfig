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
package io.wcm.caconfig.editor;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * {@link ConfigurationEditorFilter} OSGi services provide application-specific filtering
 * of Context-Aware Configurations that are displayed in the "Add Configuration" dialog of the
 * Context-Aware configuration editor.
 * Applications can set service properties or bundle headers as defined in {@link ContextAwareService} to apply this
 * configuration only for resources that match the relevant resource paths.
 */
@ConsumerType
public interface ConfigurationEditorFilter extends ContextAwareService {

  /**
   * Allow to add configurations with this name in the configuration editor.
   * @param configName Configuration name
   * @return if true, the configuration is offered in the "add configuration" dialog
   */
  boolean allowAdd(@NotNull String configName);

}
