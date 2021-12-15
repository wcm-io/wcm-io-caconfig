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

import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.wcm.caconfig.editor.impl.ConfigNamesServlet;

/**
 * JSON model for responses of {@link ConfigNamesServlet}.
 */
public class ConfigNamesResponse {

  private String contextPath;
  private Collection<ConfigNameItem> configNames;

  public String getContextPath() {
    return this.contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public Collection<ConfigNameItem> getConfigNames() {
    return this.configNames;
  }

  public void setConfigNames(Collection<ConfigNameItem> configNames) {
    this.configNames = configNames;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
  }

}
