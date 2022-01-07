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
package io.wcm.caconfig.editor;

/**
 * Properties that can be used for configuration property definitions to customize the edit widget within the editor.
 */
public final class EditorProperties {

  private EditorProperties() {
    // constants only
  }

  /**
   * Property name for defining the widget type. Values should be one of the WIDGET_TYPE_* properties.
   */
  public static final String PROPERTY_WIDGET_TYPE = "widgetType";

  /**
   * Widget type to add a pathbrowser selection widget to a string parameter.
   */
  public static final String WIDGET_TYPE_PATHBROWSER = "pathbrowser";

  /**
   * With this additional property the root path for the path browser widget can be set:
   * The root path is passed as string value of the property.
   */
  public static final String PROPERTY_PATHBROWSER_ROOT_PATH = "pathbrowserRootPath";

  /**
   * With this additional property the root path for the path browser widget can be set:
   * If set to true, the current configuration context path is used as root path.
   */
  public static final String PROPERTY_PATHBROWSER_ROOT_PATH_CONTEXT = "pathbrowserRootPathContext";

  /**
   * Widget type to add a dropdown list selection widget to a string or number parameter.
   */
  public static final String WIDGET_TYPE_DROPDOWN = "dropdown";

  /**
   * Defines the list of dropdown options as JSON array with the list options.
   * Each list option item is a JSON object with two properties <code>value</code> and <code>description</code>.
   */
  public static final String PROPERTY_DROPDOWN_OPTIONS = "dropdownOptions";

  /**
   * Defines the OSGi service property of a {@link io.wcm.caconfig.editor.DropdownOptionProvider} implementation
   * that should be used to dynamically fetch a list of dropdown options, instead of providing
   * a fixed set of dropdown options via {@link #PROPERTY_DROPDOWN_OPTIONS}.
   */
  public static final String PROPERTY_DROPDOWN_OPTIONS_PROVIDER = "dropdownOptionsProvider";

  /**
   * Widget type that allows to enter multiple lines of text for a string parameter.
   */
  public static final String WIDGET_TYPE_TEXTAREA = "textarea";

}
