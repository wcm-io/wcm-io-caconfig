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
package io.wcm.caconfig.editor.model;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caconfig.editor.impl.ConfigDataServlet;
import io.wcm.caconfig.editor.impl.ConfigNamesServlet;
import io.wcm.caconfig.editor.impl.ConfigPersistServlet;
import io.wcm.caconfig.editor.impl.EditorConfig;

/**
 * Provides editor configuration options
 */
@Model(adaptables = HttpServletRequest.class)
@ProviderType
public class EditorConfiguration {

  @SlingObject
  private Resource currentResource;
  @SlingObject
  private SlingHttpServletRequest request;
  @OSGiService
  private ConfigurationResourceResolver configResourceResolver;
  @OSGiService
  private EditorConfig editorConfig;

  private String configNamesUrl;
  private String configDataUrl;
  private String configPersistUrl;
  private String contextPath;
  private String language;
  private boolean enabled;

  @PostConstruct
  private void activate() {
    this.configNamesUrl = currentResource.getPath() + "." + ConfigNamesServlet.SELECTOR + ".json";
    this.configDataUrl = currentResource.getPath() + "." + ConfigDataServlet.SELECTOR + ".json";
    this.configPersistUrl = currentResource.getPath() + "." + ConfigPersistServlet.SELECTOR + ".json";
    this.contextPath = configResourceResolver.getContextPath(currentResource);
    this.language = request.getLocale().getLanguage();
    this.enabled = editorConfig.isEnabled();
  }

  public String getConfigNamesUrl() {
    return configNamesUrl;
  }

  public String getConfigDataUrl() {
    return configDataUrl;
  }

  public String getConfigPersistUrl() {
    return configPersistUrl;
  }

  public String getContextPath() {
    return this.contextPath;
  }

  public String getLanguage() {
    return this.language;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

}
