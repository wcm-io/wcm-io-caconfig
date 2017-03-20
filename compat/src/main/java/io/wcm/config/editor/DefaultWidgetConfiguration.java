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

import static io.wcm.config.editor.EditorProperties.ROOT_PATH;
import static io.wcm.config.editor.EditorProperties.ROWS;
import static io.wcm.config.editor.EditorProperties.WIDGET_TYPE;

import java.util.Map;

import io.wcm.sling.commons.resource.ImmutableValueMap;

/**
 * Provides constants for the widget configuration property names
 */
final class DefaultWidgetConfiguration {

  private DefaultWidgetConfiguration() {
    // only constants
  }

  static final Map<String, Object> TEXTFIELD = ImmutableValueMap.builder()
      .put(WIDGET_TYPE, "textfield")
      .build();

  static final Map<String, Object> MULTIFIELD = ImmutableValueMap.builder()
      .put(WIDGET_TYPE, "textMultivalue")
      .build();

  static final Map<String, Object> MAP = ImmutableValueMap.builder()
      .put(WIDGET_TYPE, "map")
      .build();

  static final Map<String, Object> TEXTAREA = ImmutableValueMap.builder()
      .put(WIDGET_TYPE, "textarea")
      .put(ROWS, "10")
      .build();

  static final Map<String, Object> CHECKBOX = ImmutableValueMap.builder()
      .put(WIDGET_TYPE, "checkbox")
      .build();

  static final Map<String, Object> PATHBROWSER = ImmutableValueMap.builder()
      .put(WIDGET_TYPE, "pathbrowser")
      .put(ROOT_PATH, "/content/")
      .build();

}
