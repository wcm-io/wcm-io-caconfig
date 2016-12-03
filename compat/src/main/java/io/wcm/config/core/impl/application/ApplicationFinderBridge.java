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
package io.wcm.config.core.impl.application;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.config.core.management.Application;

/**
 * Bridges old ApplicationFinder to new ApplicationFinder API.
 */
@Component(immediate = true, service = io.wcm.config.core.management.ApplicationFinder.class)
@SuppressWarnings("deprecation")
public final class ApplicationFinderBridge implements io.wcm.config.core.management.ApplicationFinder {

  @Reference
  private ApplicationFinder delegate;

  @Override
  public Application find(Resource resource) {
    return toApplication(delegate.find(resource));
  }

  @Override
  public Set<Application> getAll() {
    SortedSet<Application> allApps = new TreeSet<>();
    for (ApplicationInfo appInfo : delegate.getAll()) {
      allApps.add(toApplication(appInfo));
    }
    return allApps;
  }

  private Application toApplication(ApplicationInfo appInfo) {
    if (appInfo == null) {
      return null;
    }
    return new Application(appInfo.getApplicationId(), appInfo.getLabel());
  }

}
