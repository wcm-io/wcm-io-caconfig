/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.caconfig.editor.impl.data.configdata;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;

/**
 * JSON model for responses of {@link ConfigDataServlet}.
 */
public class ConfigCollectionItem {

  private String configName;
  private Map<String, Object> properties;
  private Collection<ConfigItem> items;
  private ConfigItem newItem;

  public String getConfigName() {
    return this.configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }

  public Map<String, Object> getProperties() {
    return this.properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public Collection<ConfigItem> getItems() {
    return this.items;
  }

  public void setItems(Collection<ConfigItem> items) {
    this.items = items;
  }

  public ConfigItem getNewItem() {
    return this.newItem;
  }

  public void setNewItem(ConfigItem newItem) {
    this.newItem = newItem;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

}
