/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


/**
 * Tests for the available {@link WidgetTypes}
 */
public class WidgetTypesTest {

  @Test
  public void testDefaultParametersImmutability() {
    Map<String, Object> defaultParams = WidgetTypes.PATHBROWSER.getDefaultWidgetConfiguration();
    assertEquals(defaultParams.get(EditorProperties.ROOT_PATH), "/content/");
    defaultParams.put(EditorProperties.ROOT_PATH, "/content/test");

    assertEquals(defaultParams.get(EditorProperties.ROOT_PATH), "/content/test");
    assertEquals(WidgetTypes.PATHBROWSER.getDefaultWidgetConfiguration().get(EditorProperties.ROOT_PATH), "/content/");
  }

  @Test
  public void testDefaultOverrides() {
    Map<String, Object> overrides = new HashMap<>();
    overrides.put(EditorProperties.ROOT_PATH, "/content/test");

    Map<String, Object> params = WidgetTypes.PATHBROWSER.getWidgetConfiguration(overrides);
    assertEquals(params.get(EditorProperties.ROOT_PATH), "/content/test");
    assertEquals(WidgetTypes.PATHBROWSER.getDefaultWidgetConfiguration().get(EditorProperties.ROOT_PATH), "/content/");
  }

}
