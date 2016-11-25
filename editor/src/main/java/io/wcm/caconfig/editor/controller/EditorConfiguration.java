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
package io.wcm.caconfig.editor.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Provides editor configuration options
 */
@Model(adaptables = {
    HttpServletRequest.class,
    Resource.class
})
@ProviderType
public class EditorConfiguration {

  private final String providerUrl;

  /**
   * @param currentResource
   */
  @Inject
  public EditorConfiguration(@SlingObject Resource currentResource) {
    providerUrl = currentResource.getPath() + ".configProvider.json";
  }

  public String getProviderUrl() {
    return providerUrl;
  }

}
