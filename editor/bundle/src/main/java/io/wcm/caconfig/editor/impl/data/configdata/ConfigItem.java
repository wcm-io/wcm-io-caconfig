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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;

/**
 * JSON model for responses of {@link ConfigDataServlet}.
 */
public class ConfigItem {

  private String configName;
  private String collectionItemName;
  private Boolean overridden;
  private Boolean inherited;
  private Collection<PropertyItem> properties;

  public String getConfigName() {
    return this.configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }

  public String getCollectionItemName() {
    return this.collectionItemName;
  }

  public void setCollectionItemName(String collectionItemName) {
    this.collectionItemName = collectionItemName;
  }

  public Boolean getOverridden() {
    return this.overridden;
  }

  public void setOverridden(Boolean overridden) {
    this.overridden = overridden;
  }

  public Boolean getInherited() {
    return this.inherited;
  }

  public void setInherited(Boolean inherited) {
    this.inherited = inherited;
  }

  public Collection<PropertyItem> getProperties() {
    return this.properties;
  }

  public void setProperties(Collection<PropertyItem> properties) {
    this.properties = properties;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

}
