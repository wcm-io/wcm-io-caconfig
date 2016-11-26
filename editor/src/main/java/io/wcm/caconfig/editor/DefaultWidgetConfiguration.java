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
package io.wcm.caconfig.editor;

import static io.wcm.caconfig.editor.EditorProperties.ROOT_PATH;
import static io.wcm.caconfig.editor.EditorProperties.ROWS;
import static io.wcm.caconfig.editor.EditorProperties.WIDGET_TYPE;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Provides constants for the widget configuration property names
 */
final class DefaultWidgetConfiguration {

  private DefaultWidgetConfiguration() {
    // only constants
  }

  static final Map<String, Object> TEXTFIELD = ImmutableMap.<String, Object>builder()
      .put(WIDGET_TYPE, "textfield")
      .build();

  static final Map<String, Object> MULTIFIELD = ImmutableMap.<String, Object>builder()
      .put(WIDGET_TYPE, "textMultivalue")
      .build();

  static final Map<String, Object> MAP = ImmutableMap.<String, Object>builder()
      .put(WIDGET_TYPE, "map")
      .build();

  static final Map<String, Object> TEXTAREA = ImmutableMap.<String, Object>builder()
      .put(WIDGET_TYPE, "textarea")
      .put(ROWS, "10")
      .build();

  static final Map<String, Object> CHECKBOX = ImmutableMap.<String, Object>builder()
      .put(WIDGET_TYPE, "checkbox")
      .build();

  static final Map<String, Object> PATHBROWSER = ImmutableMap.<String, Object>builder()
      .put(WIDGET_TYPE, "pathbrowser")
      .put(ROOT_PATH, "/content/")
      .build();

}
