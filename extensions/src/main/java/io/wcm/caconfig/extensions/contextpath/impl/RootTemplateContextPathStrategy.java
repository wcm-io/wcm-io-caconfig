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
package io.wcm.caconfig.extensions.contextpath.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.spi.ContextPathStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableSet;

/**
 * {@link ContextPathStrategy} that detects context paths by matching parent pages against a list of allowed templates
 * for context root.
 * All page between min and max level up to a page with a page matching the templates are defined as context paths.
 */
@Component(service = ContextPathStrategy.class)
@Designate(ocd = RootTemplateContextPathStrategy.Config.class, factory = true)
public class RootTemplateContextPathStrategy implements ContextPathStrategy {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Context Path Strategy: Root Templates",
      description = "Detects context paths by matching parent pages against a list of allowed templates for context root. "
          + "All page between min and max level up to a page with a page matching the templates are defined as context paths.")
  static @interface Config {

    @AttributeDefinition(name = "Templates",
        description = "List of template paths allowed for context root pages.",
        required = true)
    String[] templatePaths();

    @AttributeDefinition(name = "Match All Levels",
        description = "If set to true, all pages between min and max level have to match with one of the given template paths. "
            + "Otherwise only the template of the first (deepest) parent page is evaluated.")
    boolean templateMatchAllLevels();

    @AttributeDefinition(name = "Min. Level",
        description = "Minimum allowed absolute parent level. Example: Absolute parent level 1 of '/foo/bar/test' is '/foo/bar'.",
        required = true)
    int minLevel() default 1;

    @AttributeDefinition(name = "Max. Level",
        description = "Maximum allowed absolute parent level. Example: Absolute parent level 1 of '/foo/bar/test' is '/foo/bar'.",
        required = true)
    int maxLevel() default 5;

    @AttributeDefinition(name = "Context path expression",
        description = "Expression to match context paths. Only context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.",
        required = true)
    String contextPathRegex() default "^/content(/.+)$";

    @AttributeDefinition(name = "Config path patterns",
        description = "Expression to derive the config path from the context path. Regex group references like $1 can be used.",
        required = true)
    String[] configPathPatterns() default "/conf$1";

    @AttributeDefinition(name = "Service Ranking",
        description = "Priority of context path strategy (higher = higher priority).")
    int service_ranking() default 2000;

    String webconsole_configurationFactory_nameHint() default "{applicationId} templates={templatePaths}";

  }

  private Set<String> templatePaths;
  private boolean templatMatchAllLevels;
  private int minLevel;
  private int maxLevel;
  private Pattern contextPathRegex;
  private String[] configPathPatterns;
  private int serviceRanking;

  private static final Logger log = LoggerFactory.getLogger(RootTemplateContextPathStrategy.class);

  @Activate
  void activate(Config config) {
    templatePaths = config.templatePaths() != null ? ImmutableSet.copyOf(config.templatePaths()) : Collections.<String>emptySet();
    templatMatchAllLevels = config.templateMatchAllLevels();
    minLevel = config.minLevel();
    maxLevel = config.maxLevel();
    try {
      contextPathRegex = Pattern.compile(config.contextPathRegex());
    }
    catch (PatternSyntaxException ex) {
      log.warn("Invalid context path regex: " + config.contextPathRegex(), ex);
    }
    configPathPatterns = config.configPathPatterns();
    serviceRanking = config.service_ranking();
  }

  @Override
  public Iterator<ContextResource> findContextResources(Resource resource) {
    if (!isValidConfig()) {
      return Collections.emptyIterator();
    }

    PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
    Page page = pageManager.getContainingPage(resource);
    List<String> contextPathCandidats;
    if (templatMatchAllLevels) {
      contextPathCandidats = getContextPathCandidatesMatchAll(page);
    }
    else {
      contextPathCandidats = getContextPathCandidatesMatchInnermost(page);
    }

    List<ContextResource> contextResources = new ArrayList<>();
    for (String contextPath : contextPathCandidats) {
      Resource contextResource = resource.getResourceResolver().getResource(contextPath);
      if (contextResource != null) {
        for (String configPathPattern : configPathPatterns) {
          String configRef = deriveConfigRef(contextPath, configPathPattern);
          if (configRef != null) {
            contextResources.add(new ContextResource(contextResource, configRef, serviceRanking));
          }
        }
      }
    }
    Collections.reverse(contextResources);
    return contextResources.iterator();
  }

  private boolean isValidConfig() {
    return !templatePaths.isEmpty()
        && contextPathRegex != null
        && configPathPatterns != null
        && configPathPatterns.length > 0;
  }

  private List<String> getContextPathCandidatesMatchInnermost(Page page) {
    List<String> candidates = new ArrayList<>();
    if (page != null) {
      for (int level = minLevel; level <= maxLevel; level++) {
        Page rootPage = page.getAbsoluteParent(level);
        if (rootPage != null) {
          String templatePath = rootPage.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
          if (templatePath != null && templatePaths.contains(templatePath)) {
            candidates.add(rootPage.getPath());
          }
        }
      }
    }
    return candidates;
  }

  private List<String> getContextPathCandidatesMatchAll(Page page) {
    List<String> candidates = new ArrayList<>();
    if (page != null) {
      for (int level = minLevel; level <= maxLevel; level++) {
        Page rootPage = page.getAbsoluteParent(level);
        if (rootPage != null) {
          String templatePath = rootPage.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
          if (templatePath != null && templatePaths.contains(templatePath)) {
            candidates.add(rootPage.getPath());
          }
        }
      }
    }
    return candidates;
  }

  private String deriveConfigRef(String contextPath, String configPathPattern) {
    Matcher matcher = contextPathRegex.matcher(contextPath);
    if (matcher.matches()) {
      return matcher.replaceAll(configPathPattern);
    }
    else {
      return null;
    }
  }

}
