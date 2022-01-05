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

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Item for {@link DropdownOptionProvider}.
 */
@ProviderType
public final class DropdownOptionItem {

  private final String value;
  private final String description;

  /**
   * @param value Item value
   * @param description Item description
   */
  public DropdownOptionItem(@NotNull String value, @NotNull String description) {
    this.value = value;
    this.description = description;
  }

  /**
   * @return Item value
   */
  public @NotNull String getValue() {
    return this.value;
  }

  /**
   * @return Item description
   */
  public @NotNull String getDescription() {
    return this.description;
  }

}
