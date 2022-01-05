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
package io.wcm.caconfig.sample.config;

import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_DROPDOWN_OPTIONS;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_DROPDOWN_OPTIONS_PROVIDER;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_PATHBROWSER_ROOT_PATH;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_PATHBROWSER_ROOT_PATH_CONTEXT;
import static io.wcm.caconfig.editor.EditorProperties.PROPERTY_WIDGET_TYPE;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_DROPDOWN;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_PATHBROWSER;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE_TEXTAREA;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

import io.wcm.caconfig.sample.impl.SampleDropdownOptionProvider;

/**
 * Config annotation class example
 */
@Configuration(label = "Sample Configuration", description = "This is a sample configuration.")
public @interface ConfigSample {

  /**
   * @return String parameter
   */
  @Property(label = "String Param", description = "This is a string parameter in the singleton configuration.", order = 1)
  String stringParam();

  /**
   * @return Integer parameter
   */
  @Property(label = "Integer Param", order = 2)
  int intParam();

  /**
   * @return Boolean parameter
   */
  @Property(label = "Boolean Param", order = 3)
  boolean boolParam();

  /**
   * @return Path parameter
   */
  @Property(label = "DAM Path", description = "Browse DAM assets.", order = 4, property = {
      PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_PATHBROWSER,
      PROPERTY_PATHBROWSER_ROOT_PATH + "=/content/dam"
  })
  String damPath();

  /**
   * @return Path parameter
   */
  @Property(label = "Context Path", description = "Browse pages in the current configuration context path.", order = 5,
      property = {
          PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_PATHBROWSER,
          PROPERTY_PATHBROWSER_ROOT_PATH_CONTEXT + "=true"
      })
  String contextPath();

  /**
   * @return String array parameter with default value
   */
  @Property(label = "String Array Param", order = 6)
  String[] stringArrayParam() default {
      "value1", "value2"
  };

  /**
   * @return String dropdown parameter
   */
  @Property(label = "String Dropdown Param", description = "This is a string parameter with dropdown list.", order = 7,
      property = {
          PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_DROPDOWN,
          PROPERTY_DROPDOWN_OPTIONS + "=["
              + "{'value':'option1','description':'First option'},"
              + "{'value':'option2','description':'Second option'},"
              + "{'value':'option3','description':'Third option'}"
              + "]"
      })
  String stringDropdownParam();

  /**
   * @return Integer dropdown parameter
   */
  @Property(label = "Integer Dropdown Param", order = 8,
      property = {
          PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_DROPDOWN,
          PROPERTY_DROPDOWN_OPTIONS + "=["
              + "{'value':1,'description':'Number One'},"
              + "{'value':2,'description':'Number Two'}"
              + "]"
      })
  int intDropdownParam();

  /**
   * @return String multivalue dropdown parameter
   */
  @Property(label = "String Multivalue Dropdown Param", description = "This is a multi-valued string parameter with dropdown list.", order = 9,
      property = {
          PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_DROPDOWN,
          PROPERTY_DROPDOWN_OPTIONS + "=["
              + "{'value':'option1','description':'Option #1'},"
              + "{'value':'option2','description':'Option #2'},"
              + "{'value':'option3','description':'Option #3'},"
              + "{'value':'option4','description':'Option #4'},"
              + "{'value':'option5','description':'Option #5'}"
              + "]"
      })
  String[] stringMultivalueDropdownParam();

  /**
   * @return String dynamic dropdown parameter
   */
  @Property(label = "String Dynamic Dropdown Param", description = "This is a string parameter with dynamic dropdown list.", order = 10,
      property = {
          PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_DROPDOWN,
          PROPERTY_DROPDOWN_OPTIONS_PROVIDER + "=" + SampleDropdownOptionProvider.SAMPLE_DROPDWON_OPTIONS_PROVIDER
      })
  String stringDropdownDynamicParam();

  /**
   * @return Path parameter
   */
  @Property(label = "String Text Area", description = "Enter multiple lines of text.", order = 11,
      property = {
          PROPERTY_WIDGET_TYPE + "=" + WIDGET_TYPE_TEXTAREA
      })
  String stringTextArea();

}
