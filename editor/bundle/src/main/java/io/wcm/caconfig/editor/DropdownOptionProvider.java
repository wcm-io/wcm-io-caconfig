/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Allows to provide a dynamic list of options.
 * Implement as OSGi service and set a service property {@link #PROPERTY_SELECTOR} which is used to reference
 * the implementation in the context-aware configuration property metadata options.
 */
@ConsumerType
public interface DropdownOptionProvider {

  /**
   * Name of OSGi property to distinguish different implementations of the provider.
   * The value that this property is set to has to be configured in the Context-Aware configuration property
   * metadata using {@link io.wcm.caconfig.editor.EditorProperties#PROPERTY_DROPDOWN_OPTIONS_PROVIDER}.
   */
  String PROPERTY_SELECTOR = "io.wcm.caconfig.editor.widget.dropdown.provider";

  /**
   * Get dynamic list of dropdown items to available in the context-aware configuration editor.
   * @param contextResource Context resource. This is usually the AEM page of the configuration editor
   * @return List of dropdown items
   */
  @NotNull
  List<DropdownOptionItem> getDropdownOptions(@NotNull Resource contextResource);

}
