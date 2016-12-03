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
package io.wcm.caconfig.application;

import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Find associated application for a resource.
 */
@ProviderType
public interface ApplicationFinder {

  /**
   * Find application that is associated with this resource.
   * All implementors of {@link io.wcm.caconfig.application.spi.ApplicationProvider} are enquired in order of service ranking,
   * the first that accepts the resource as matching wins.
   * @param resource Resource
   * @return Application instance if a match was found, otherwise null
   */
  ApplicationInfo find(Resource resource);

  /**
   * Get all applications that are registered in the system.
   * @return Set of application instances
   */
  Set<ApplicationInfo> getAll();

}
