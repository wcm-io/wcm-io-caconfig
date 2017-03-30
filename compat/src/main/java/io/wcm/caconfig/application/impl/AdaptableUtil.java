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
package io.wcm.caconfig.application.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

/**
 * Helper methods for handling adaptables.
 */
final class AdaptableUtil {

  private AdaptableUtil() {
    // static methods only
  }

  /**
   * Tries to get the resource associated with the adaptable.
   * @param adaptable Adaptable
   * @return Resource or null
   */
  public static Resource getResource(Object adaptable) {
    if (adaptable instanceof Resource) {
      return (Resource)adaptable;
    }
    else if (adaptable instanceof SlingHttpServletRequest) {
      SlingHttpServletRequest request = (SlingHttpServletRequest)adaptable;
      return request.getResource();
    }
    return null;
  }

}
