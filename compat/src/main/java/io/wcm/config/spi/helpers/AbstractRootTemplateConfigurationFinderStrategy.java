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
import java.util.Set;

import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableSet;

import io.wcm.config.spi.ConfigurationFinderStrategy;

/**
 * Abstract implementation of {@link ConfigurationFinderStrategy} based on that detects the configuration scope
 * based on the template of the root page.
 * All pages that are found inside the min/max bounds of the configuration levels are accepted as configuration
 * scope root. If a page with the given template is found, this is the inner-most configuration scope and
 * no further configuration scopes are accepted.
 */
@ConsumerType
public abstract class AbstractRootTemplateConfigurationFinderStrategy implements ConfigurationFinderStrategy {

  private final String applicationId;
  private final int minLevel;
  private final int maxLevel;
  private final Set<String> configRootTemplatePaths;

  /**
   * @param applicationId Application ID
   * @param minLevel Min. acceptable level for a configuration scope root
   *          Levels are used in the same way as {@link Text#getAbsoluteParent(String, int)}.
   *          Example:<br>
   *          <code>Text.getAbsoluteParent("/foo/bar/test", 1) == "/foo/bar"</code>
   * @param maxLevel Max. acceptable level for a configuration scope root
   * @param templatePaths Template paths that are allowed as configuration scope root, e.g. homepage template of a site
   */
  protected AbstractRootTemplateConfigurationFinderStrategy(String applicationId, int minLevel,
      int maxLevel, String... templatePaths) {
    this.applicationId = applicationId;
    this.minLevel = minLevel;
    this.maxLevel = maxLevel;
    this.configRootTemplatePaths = ImmutableSet.copyOf(templatePaths);
  }

  @Override
  public final String getApplicationId() {
    return this.applicationId;
  }

  @Override
  public final Iterator<String> findConfigurationIds(Resource resource) {
    List<String> configurationIds = new ArrayList<>();

    PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
    if (pageManager != null) {
      Page page = pageManager.getContainingPage(resource);
      if (page != null) {
        for (int level = minLevel; level <= maxLevel; level++) {
          Page rootPage = page.getAbsoluteParent(level);
          if (rootPage != null) {
            String templatePath = rootPage.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
            if (templatePath != null) {
              configurationIds.add(rootPage.getPath());
              if (configRootTemplatePaths.contains(templatePath)) {
                break;
              }
            }
          }
        }
      }
    }

    return configurationIds.iterator();
  }

}
