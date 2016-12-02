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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.config.spi.ConfigurationFinderStrategy;

/**
 * Abstract implementation of {@link ConfigurationFinderStrategy} that supports one or multiple fixed levels
 * in content hierarchy where configurations are supported.
 */
@ConsumerType
public abstract class AbstractAbsoluteParentConfigurationFinderStrategy implements ConfigurationFinderStrategy {

  private final String applicationId;
  private final int[] levels;

  /**
   * @param applicationId Application ID
   * @param levels List of absolute levels where configuration is supported.
   *          Levels are used in the same way as {@link Text#getAbsoluteParent(String, int)}.
   *          Example:<br>
   *          <code>Text.getAbsoluteParent("/foo/bar/test", 1) == "/foo/bar"</code>
   */
  protected AbstractAbsoluteParentConfigurationFinderStrategy(String applicationId, int... levels) {
    this.applicationId = applicationId;
    this.levels = levels;
  }

  @Override
  public final String getApplicationId() {
    return this.applicationId;
  }

  @Override
  public final Iterator<String> findConfigurationIds(Resource resource) {
    List<String> configurationIds = new ArrayList<>();
    for (int level : this.levels) {
      addAbsoluteParent(configurationIds, resource, level);
    }
    return configurationIds.iterator();
  }

  private void addAbsoluteParent(List<String> configurationIds, Resource resource, int absoluteParent) {
    String configurationId = Text.getAbsoluteParent(resource.getPath(), absoluteParent);
    if (StringUtils.isNotEmpty(configurationId)) {
      configurationIds.add(configurationId);
    }
  }

}
