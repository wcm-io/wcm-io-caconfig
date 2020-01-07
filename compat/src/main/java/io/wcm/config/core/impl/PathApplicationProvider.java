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
package io.wcm.config.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.config.spi.ApplicationProvider;

/**
 * {@link ApplicationProvider} that supports detecting an application based paths subtrees.
 */
@Component(service = ApplicationProvider.class)
@Designate(ocd = PathApplicationProvider.Config.class, factory = true)
public class PathApplicationProvider implements ApplicationProvider {

  @ObjectClassDefinition(name = "wcm.io Application Provider: Path",
      description = "Detects applications based on path subtrees.")
  static @interface Config {

    @AttributeDefinition(name = "Application ID",
        description = "Application path. Example: /apps/application1", required = true)
    String applicationId();

    @AttributeDefinition(name = "Application Label",
        description = "Optional: Label for application")
    String label() default "";

    @AttributeDefinition(name = "Path patterns",
        description = "List of regular expressions matchin all content paths of context resources related to the application.",
        required = true)
    String[] pathPatterns() default { "^/content(/.+)?$" };

    String webconsole_configurationFactory_nameHint() default "{applicationId}";

  }

  private String applicationId;
  private String label;
  private final List<Pattern> pathPatterns = new ArrayList<>();

  private static final Logger log = LoggerFactory.getLogger(PathApplicationProvider.class);

  @Activate
  void activate(Config config) {
    applicationId = config.applicationId();
    label = config.label();
    if (config.pathPatterns() != null) {
      for (String pathPattern : config.pathPatterns()) {
        try {
          pathPatterns.add(Pattern.compile(pathPattern));
        }
        catch (PatternSyntaxException ex) {
          log.warn("Invalid path regex: {}", pathPattern, ex);
        }
      }
    }

  }

  @Override
  public String getApplicationId() {
    return applicationId;
  }

  @Override
  public String getLabel() {
    return StringUtils.defaultString(label, applicationId);
  }

  @Override
  public boolean matches(Resource resource) {
    for (Pattern pattern : pathPatterns) {
      if (pattern.matcher(resource.getPath()).matches()) {
        return true;
      }
    }
    return false;
  }

}
