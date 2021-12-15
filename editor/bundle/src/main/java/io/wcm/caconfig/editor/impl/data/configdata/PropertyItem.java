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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;

/**
 * JSON model for responses of {@link ConfigDataServlet}.
 */
public class PropertyItem {

  private String name;
  private Object value;
  private Object effectiveValue;
  private String configSourcePath;
  private Boolean isDefault;
  private Boolean inherited;
  private Boolean overridden;
  private PropertyItemMetadata metadata;
  private ConfigItem nestedConfig;
  private ConfigCollectionItem nestedConfigCollection;

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Object getValue() {
    return this.value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Object getEffectiveValue() {
    return this.effectiveValue;
  }

  public void setEffectiveValue(Object effectiveValue) {
    this.effectiveValue = effectiveValue;
  }

  public String getConfigSourcePath() {
    return this.configSourcePath;
  }

  public void setConfigSourcePath(String configSourcePath) {
    this.configSourcePath = configSourcePath;
  }

  @JsonProperty("default")
  public Boolean getIsDefault() {
    return this.isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
  }

  public Boolean getInherited() {
    return this.inherited;
  }

  public void setInherited(Boolean inherited) {
    this.inherited = inherited;
  }

  public Boolean getOverridden() {
    return this.overridden;
  }

  public void setOverridden(Boolean overridden) {
    this.overridden = overridden;
  }

  public PropertyItemMetadata getMetadata() {
    return this.metadata;
  }

  public void setMetadata(PropertyItemMetadata metadata) {
    this.metadata = metadata;
  }

  public ConfigItem getNestedConfig() {
    return this.nestedConfig;
  }

  public void setNestedConfig(ConfigItem nestedConfig) {
    this.nestedConfig = nestedConfig;
  }

  public ConfigCollectionItem getNestedConfigCollection() {
    return this.nestedConfigCollection;
  }

  public void setNestedConfigCollection(ConfigCollectionItem nestedConfigCollection) {
    this.nestedConfigCollection = nestedConfigCollection;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

}
