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
package io.wcm.caconfig.editor.impl.data.confignames;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.wcm.caconfig.editor.impl.ConfigNamesServlet;

/**
 * JSON model for responses of {@link ConfigNamesServlet}.
 */
public class ConfigNameItem {

  private String configName;
  private String label;
  private String description;
  private Boolean collection;
  private Boolean exists;
  private Boolean inherited;
  private Boolean overridden;
  private Boolean allowAdd;

  public String getConfigName() {
    return this.configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }

  public String getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCollection() {
    return this.collection;
  }

  public void setCollection(boolean collection) {
    this.collection = collection;
  }

  public boolean isExists() {
    return this.exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }

  public boolean isInherited() {
    return this.inherited;
  }

  public void setInherited(boolean inherited) {
    this.inherited = inherited;
  }

  public boolean isOverridden() {
    return this.overridden;
  }

  public void setOverridden(boolean overridden) {
    this.overridden = overridden;
  }

  public boolean isAllowAdd() {
    return this.allowAdd;
  }

  public void setAllowAdd(boolean allowAdd) {
    this.allowAdd = allowAdd;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

}
