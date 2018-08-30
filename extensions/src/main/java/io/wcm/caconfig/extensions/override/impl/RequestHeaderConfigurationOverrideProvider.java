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
package io.wcm.caconfig.extensions.override.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.caconfig.spi.ConfigurationOverrideProvider;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import io.wcm.sling.commons.request.RequestContext;

/**
 * Provides configuration override strings from current request header.
 */
@Component(immediate = true, service = ConfigurationOverrideProvider.class)
@Designate(ocd = RequestHeaderConfigurationOverrideProvider.Config.class)
public final class RequestHeaderConfigurationOverrideProvider implements ConfigurationOverrideProvider {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Override Provider: Request Header",
      description = "Allows to define configuration property default values or overrides from inconming request headers.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled",
        description = "Enable this override provider.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Header Name",
        description = "Name of the request header to get override strings from. Can be present multiple times.")
    String headerName() default "wcmio.caconfig.override";

    @AttributeDefinition(name = "Service Ranking",
        description = "Priority of configuration override providers (higher = higher priority).")
    int service_ranking() default 300;

  }

  private static final Logger log = LoggerFactory.getLogger(RequestHeaderConfigurationOverrideProvider.class);

  private Config config;

  @Reference
  private RequestContext requestContext;

  @Override
  public @NotNull Collection<String> getOverrideStrings() {
    if (config.enabled()) {
      if (requestContext != null) {
        SlingHttpServletRequest request = requestContext.getThreadRequest();
        if (request != null) {
          return buildMapFromHeaders(request);
        }
      }
      else {
        log.warn("RequestContext service not running - unable to inspect current request.");
      }
    }
    return ImmutableList.<String>of();
  }

  private Collection<String> buildMapFromHeaders(SlingHttpServletRequest request) {
    List<String> result = new ArrayList<>();
    Enumeration<String> headerValues = request.getHeaders(config.headerName());
    while (headerValues.hasMoreElements()) {
      result.add(headerValues.nextElement());
    }
    return result;
  }

  @Activate
  void activate(Config value) {
    this.config = value;
  }

}
