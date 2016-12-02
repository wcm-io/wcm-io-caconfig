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
package io.wcm.config.spi;

import java.util.regex.Pattern;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Provides information about a registered application, and how to detect if a certain resource belongs to this
 * application or not.
 * This interface has to be implemented by applications that provide their own templates and components, and need
 * their own controller or handler implementations chosen by the ApplicationImplementationPicker for Sling Models.
 */
@ConsumerType
public interface ApplicationProvider {

  /**
   * Pattern that defines the allowed syntax for application ids.
   */
  Pattern APPLICATION_ID_PATTERN = Pattern.compile("^(/[a-zA-Z0-9\\-\\_]+)+$");

  /**
   * @return Application Id. The application is is usually the application path at /apps/ or /libs/.
   */
  String getApplicationId();

  /**
   * @return Display name for application.
   */
  String getLabel();

  /**
   * Evaluates if the given resources belongs to this application or not.
   * It is important that this check is implemented as efficient as possible (e.g. by only checking the resource
   * path against a precompiled regular expression), because this method is called quite often.
   * @param resource Resource
   * @return true if the resource matches to the application.
   */
  boolean matches(Resource resource);

}
