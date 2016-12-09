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
package io.wcm.config.spi.helpers;

import java.util.regex.Pattern;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.config.spi.ApplicationProvider;

/**
 * Abstract implementation of {@link ApplicationProvider} that supports detecting an application based
 * on one or multiple fixed paths subtrees.
 * @deprecated Please use {@link io.wcm.caconfig.application.impl.PathApplicationProvider}
 */
@Deprecated
@ConsumerType
public abstract class AbstractPathApplicationProvider implements ApplicationProvider {

  private final String applicationId;
  private final String label;
  private final Pattern[] pathPatterns;

  /**
   * @param applicationId Application id
   * @param label Application display name
   * @param pathPatterns List of regular expression to match all paths/subetrees this application belongs to.
   */
  public AbstractPathApplicationProvider(String applicationId, String label, Pattern... pathPatterns) {
    this.applicationId = applicationId;
    this.label = label;
    this.pathPatterns = pathPatterns;
  }

  /**
   * @param applicationId Application id
   * @param label Application display name
   * @param paths List of paths/subtrees this application belongs to
   */
  public AbstractPathApplicationProvider(String applicationId, String label, String... paths) {
    this(applicationId, label, convertToPathPatterns(paths));
  }

  private static Pattern[] convertToPathPatterns(String[] paths) {
    Pattern[] pathPatterns = new Pattern[paths.length];
    for (int i = 0; i < paths.length; i++) {
      pathPatterns[i] = Pattern.compile("^" + Pattern.quote(paths[i]) + "(/.*)?$");
    }
    return pathPatterns;
  }

  @Override
  public final String getApplicationId() {
    return this.applicationId;
  }

  @Override
  public final String getLabel() {
    return this.label;
  }

  @Override
  public final boolean matches(Resource resource) {
    for (Pattern pattern : pathPatterns) {
      if (pattern.matcher(resource.getPath()).matches()) {
        return true;
      }
    }
    return false;
  }

}
