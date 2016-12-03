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
package io.wcm.config.editor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Properties to define the edit capabilities of configuration parameters.
 */
@ProviderType
public final class EditorProperties {

  private EditorProperties() {
    // only constants
  }

  /**
   * Set the display label of the property.
   */
  public static final String LABEL = "label";

  /**
   * Set the widget type.
   */
  public static final String WIDGET_TYPE = "widgetType";

  /**
   * Set the the group of the parameter.
   */
  public static final String GROUP = "group";

  /**
   * Set the the description of the parameter.
   */
  public static final String DESCRIPTION = "description";

  /**
   * Set the minimum length value for the text field.
   */
  public static final String MINLENGTH = "minlength";

  /**
   * Set the maximum length value for the text field.
   */
  public static final String MAXLENGTH = "maxlength";

  /**
   * Set the number of rows on the text area.
   */
  public static final String ROWS = "rows";

  /**
   * Set the "required" flag.
   */
  public static final String REQUIRED = "required";

  /**
   * Set the validation pattern.
   */
  public static final String PATTERN = "pattern";

  /**
   * Set root path for the browser.
   */
  public static final String ROOT_PATH = "rootPath";

}
