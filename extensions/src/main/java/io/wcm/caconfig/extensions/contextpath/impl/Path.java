/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.caconfig.extensions.contextpath.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

/**
 * Get absolute parent paths respecting side-by-side comparison in AEM 6.3 which may copy
 * a content tree to /content/versionhistory[/tenant]/userid.
 */
final class Path {

  /**
   * Path for storing version history for side-by-side comparison.
   */
  public static final String VERSION_HISTORY = "/content/versionhistory";

  private static final Pattern VERSION_HISTORY_PATTERN = Pattern.compile(VERSION_HISTORY + "/[^/]+(/.*)");
  private static final Pattern VERSION_HISTORY_TENANT_PATTERN = Pattern.compile(VERSION_HISTORY + "/[^/]+/[^/]+(/.*)");

  private Path() {
    // static methods only
  }

  /**
   * Get absolute parent of given path. If path is below /content/versionhistory the path level is adjusted accordingly.
   * @param path Path
   * @param parentLevel Parent level
   * @param resourceResolver Resource resolver
   * @return Absolute parent path
   */
  public static String getAbsoluteParent(String path, int parentLevel, ResourceResolver resourceResolver) {
    return Text.getAbsoluteParent(path, parentLevel + getParentLevelOffset(path, resourceResolver));
  }

  /**
   * Get absolute parent of given path. If path is below /content/versionhistory the path level is adjusted accordingly.
   * @param page Page
   * @param parentLevel Parent level
   * @param resourceResolver Resource resolver
   * @return Absolute parent page
   */
  public static Page getAbsoluteParent(Page page, int parentLevel, ResourceResolver resourceResolver) {
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    String absoluteParentPath = getAbsoluteParent(page.getPath(), parentLevel, resourceResolver);
    return pageManager.getPage(absoluteParentPath);
  }

  /**
   * Gets original path if the given path points to /content/versionhistory/*.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return Path without /content/versionhistory rewriting
   */
  public static String getPathWithoutVersionHistory(String path, ResourceResolver resourceResolver) {
    boolean isTenant = isTenant(resourceResolver);
    Matcher matcher = isTenant ? VERSION_HISTORY_TENANT_PATTERN.matcher(path) : VERSION_HISTORY_PATTERN.matcher(path);
    if (matcher.matches()) {
      return "/content" + matcher.group(1);
    }
    else {
      return path;
    }
  }

  /**
   * Calculates offset for parent level if path points ot /content/versionhistory.
   * @param path Path
   * @param resourceResolver Resource resolver
   * @return 0 or offset if in /content/versionhistory
   */
  private static int getParentLevelOffset(String path, ResourceResolver resourceResolver) {
    boolean isTenant = isTenant(resourceResolver);
    Matcher matcher = isTenant ? VERSION_HISTORY_TENANT_PATTERN.matcher(path) : VERSION_HISTORY_PATTERN.matcher(path);
    if (matcher.matches()) {
      return isTenant ? 3 : 2;
    }
    else {
      return 0;
    }
  }

  /**
   * Checks if a tenant is active for the current user.
   * @param resourceResolver Resource resolver
   * @return true if tenant is active
   */
  private static boolean isTenant(ResourceResolver resourceResolver) {
    Tenant tenant = resourceResolver.adaptTo(Tenant.class);
    return tenant != null;
  }

}
