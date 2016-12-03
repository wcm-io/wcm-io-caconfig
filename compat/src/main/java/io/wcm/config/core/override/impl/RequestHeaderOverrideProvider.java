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
package io.wcm.config.core.override.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.wcm.config.spi.ParameterOverrideProvider;
import io.wcm.sling.commons.request.RequestContext;

/**
 * Provides parameter override map from current request header.
 */
@Component(immediate = true, service = ParameterOverrideProvider.class)
@Designate(ocd = RequestHeaderOverrideProvider.Config.class)
public final class RequestHeaderOverrideProvider implements ParameterOverrideProvider {

  /**
   * Prefix for override request header
   */
  public static final String REQUEST_HEADER_PREFIX = "config.override.";

  @ObjectClassDefinition(name = "wcm.io Configuration Property Override Provider: Request Header",
      description = "Allows to define configuration property default values or overrides from inconming request headers.")
  static @interface Config {

    @AttributeDefinition(name = "Enabled", description = "Enable parameter override provider.")
    boolean enabled() default false;

    @AttributeDefinition(name = "Service Ranking", description = "Priority of parameter override providers (lower = higher priority).")
    int service_ranking() default 1000;

  }

  private static final Logger log = LoggerFactory.getLogger(RequestHeaderOverrideProvider.class);

  private boolean enabled;

  @Reference
  private RequestContext requestContext;

  @Override
  public Map<String, String> getOverrideMap() {
    if (this.enabled) {
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
    return ImmutableMap.<String, String>of();
  }

  private Map<String, String> buildMapFromHeaders(SlingHttpServletRequest request) {
    Map<String, String> map = new HashMap<>();
    Enumeration keys = request.getHeaderNames();
    while (keys.hasMoreElements()) {
      Object keyObject = keys.nextElement();
      if (keyObject instanceof String) {
        String key = (String)keyObject;
        if (StringUtils.startsWith(key, REQUEST_HEADER_PREFIX)) {
          map.put(StringUtils.substringAfter(key, REQUEST_HEADER_PREFIX), request.getHeader(key));
        }
      }
    }
    return map;
  }

  @Activate
  void activate(Config config) {
    this.enabled = config.enabled();
  }

}
