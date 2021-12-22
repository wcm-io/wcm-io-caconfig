/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.caconfig.extensions.bindings.impl;

import javax.script.Bindings;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.caconfig.spi.ConfigurationBindingsResourceDetectionStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceRanking;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

/**
 * AEM-specific implementation of {@link ConfigurationBindingsResourceDetectionStrategy}.
 * It detects if the current request is attached to an AEM page, and uses the resource of that page
 * for resolving the context-aware configurations.
 * With this, it works also for structure components in editable templates, which are technically located below /conf.
 */
@Component(service = ConfigurationBindingsResourceDetectionStrategy.class)
@ServiceRanking(100)
public class AemConfigurationBindingsResourceDetectionStrategy implements ConfigurationBindingsResourceDetectionStrategy {

  @Override
  public @Nullable Resource detectResource(@NotNull Bindings bindings) {
    SlingHttpServletRequest request = getRequest(bindings);
    if (request != null) {
      Page currentPage = getCurrentPage(request);
      if (currentPage != null) {
        return currentPage.adaptTo(Resource.class);
      }
    }
    return null;
  }

  private @Nullable SlingHttpServletRequest getRequest(@NotNull Bindings bindings) {
    return (SlingHttpServletRequest)bindings.get(SlingBindings.REQUEST);
  }

  private @Nullable Page getCurrentPage(@NotNull SlingHttpServletRequest request) {
    ComponentContext componentContext = WCMUtils.getComponentContext(request);
    if (componentContext != null) {
      return componentContext.getPage();
    }
    return null;
  }

}
