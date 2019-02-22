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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.caconfig.resource.spi.ContextPathStrategy;
import org.apache.sling.caconfig.resource.spi.ContextResource;
import org.jetbrains.annotations.NotNull;
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

import io.wcm.wcm.commons.util.Path;

/**
 * {@link ContextPathStrategy} that detects context paths by absolute parent levels of a context resource.
 */
@Component(service = ContextPathStrategy.class)
@Designate(ocd = AbsoluteParentContextPathStrategy.Config.class, factory = true)
public class AbsoluteParentContextPathStrategy implements ContextPathStrategy {

  @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Context Path Strategy: Absolute Parents",
      description = "Detects context paths by absolute parent levels of a context resource.")
  static @interface Config {

    @AttributeDefinition(name = "Absolute Levels",
        description = "List of absolute parent levels. Example: Absolute parent level 1 of '/foo/bar/test' is '/foo/bar'.",
        required = true)
    int[] levels();

    @AttributeDefinition(name = "Unlimited levels",
        description = "If set to true, the 'Absolute Levels' define only the minimum levels. "
            + "Above the highest level number every additional level is accepted as well.")
    boolean unlimited() default false;

    @AttributeDefinition(name = "Context path whitelist",
        description = "Expression to match context paths. Context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.",
        required = true)
    String contextPathRegex() default "^/content(/.+)$";

    @AttributeDefinition(name = "Context path blacklist",
        description = "Expression to match context paths. Context paths matching this expression are not allowed.",
        required = true)
    String contextPathBlacklistRegex() default "^.*/tools(/config(/.+)?)?$";

    @AttributeDefinition(name = "Template path blacklist",
            description = "Context paths belonging to a page matching one of the given template paths are not allowed.",
            required = true)
    String[] templatePathsBlacklist();

    @AttributeDefinition(name = "Config path patterns",
        description = "Expression to derive the config path from the context path. Regex group references like $1 can be used.",
        required = true)
    String[] configPathPatterns() default "/conf$1";

    @AttributeDefinition(name = "Service Ranking",
        description = "Priority of context path strategy (higher = higher priority).")
    int service_ranking() default 2000;

    String webconsole_configurationFactory_nameHint() default "levels={levels}, path={contextPathRegex}";

  }

  private Set<Integer> levels;
  private int unlimitedLevelStart;
  private boolean unlimited;
  private Pattern contextPathRegex;
  private Pattern contextPathBlacklistRegex;
  private String[] configPathPatterns;
  private int serviceRanking;
  private Set<String> templatePathsBlacklist;

  private static final Logger log = LoggerFactory.getLogger(AbsoluteParentContextPathStrategy.class);

  @Activate
  void activate(Config config) {
    levels = new TreeSet<>();
    if (config.levels() != null) {
      for (int level : config.levels()) {
        levels.add(level);
        if (level >= unlimitedLevelStart) {
          unlimitedLevelStart = level + 1;
        }
      }
    }
    unlimited = config.unlimited();
    try {
      contextPathRegex = Pattern.compile(config.contextPathRegex());
    }
    catch (PatternSyntaxException ex) {
      log.warn("Invalid context path regex: " + config.contextPathRegex(), ex);
    }
    if (StringUtils.isNotEmpty(config.contextPathBlacklistRegex())) {
      try {
        contextPathBlacklistRegex = Pattern.compile(config.contextPathBlacklistRegex());
      }
      catch (PatternSyntaxException ex) {
        log.warn("Invalid context path blacklist regex: " + config.contextPathBlacklistRegex(), ex);
      }
    }
    configPathPatterns = config.configPathPatterns();
    serviceRanking = config.service_ranking();
    // make sure this is never null (only DS 1.4 initializes them always to empty arrays)
    templatePathsBlacklist = config.templatePathsBlacklist() != null ? new HashSet<>(Arrays.asList(config.templatePathsBlacklist())) : Collections.emptySet();
  }

  @SuppressWarnings("null")
  @Override
  public @NotNull Iterator<ContextResource> findContextResources(@NotNull Resource resource) {
    if (!isValidConfig()) {
      return Collections.emptyIterator();
    }

    ResourceResolver resourceResolver = resource.getResourceResolver();
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    List<ContextResource> contextResources = new ArrayList<>();

    int maxLevel = Path.getAbsoluteLevel(resource.getPath(), resourceResolver);
    for (int level = 0; level <= maxLevel; level++) {
      if (levels.contains(level) || (unlimited && level >= unlimitedLevelStart)) {
        String contextPath = Path.getAbsoluteParent(resource.getPath(), level, resourceResolver);
        if (StringUtils.isNotEmpty(contextPath)) {
          Resource contextResource = resource.getResourceResolver().getResource(contextPath);
          if (contextResource != null) {
            // first check if resource is blacklisted
            if (isResourceBelongingToBlacklistedTemplates(contextResource, pageManager)) {
              log.trace("Resource '{}' is belonging to a page derived from a blacklisted template, skipping level {}", contextPath, level);
              break;
            }
            for (String configPathPattern : configPathPatterns) {
              String configRef = deriveConfigRef(contextPath, configPathPattern, resourceResolver);
              if (configRef != null) {
                contextResources.add(new ContextResource(contextResource, configRef, serviceRanking));
              }
            }
          }
        }
      }
    }

    Collections.reverse(contextResources);
    return contextResources.iterator();
  }

  private boolean isValidConfig() {
    return !levels.isEmpty()
        && contextPathRegex != null
        && configPathPatterns != null
        && configPathPatterns.length > 0;
  }

  private String deriveConfigRef(String contextPath, String configPathPattern, ResourceResolver resourceResolver) {
    Matcher matcher = contextPathRegex.matcher(Path.getOriginalPath(contextPath, resourceResolver));
    Matcher blacklistMatcher = null;
    if (contextPathBlacklistRegex != null) {
      blacklistMatcher = contextPathBlacklistRegex.matcher(contextPath);
    }
    if (matcher.matches() && (blacklistMatcher == null || !blacklistMatcher.matches())) {
      return matcher.replaceAll(configPathPattern);
    }
    else {
      return null;
    }
  }

  @SuppressWarnings({ "null", "unused" })
  private boolean isResourceBelongingToBlacklistedTemplates(Resource resource, PageManager pageManager) {
    if (templatePathsBlacklist.isEmpty()) {
      return false;
    }
    Page page = pageManager.getContainingPage(resource);
    // if no containing page could be determined, we don't blacklist
    if (page == null) {
      log.trace("Resource '{}' is not part of page, blacklisted templates are not considered.", resource.getPath());
      return false;
    }
    String templatePath = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
    if (templatePath != null) {
      if (templatePathsBlacklist.contains(templatePath)) {
        return true;
      }
    }
    else {
      log.trace("Resource '{}' is part of page '{}' which doesn't contain any template property, blacklisted templates are not considered.",
          resource.getPath(), page.getPath());
      return false;
    }
    log.trace("Resource '{}' is part of page '{}' but is not based on any of the blacklisted templates.", resource.getPath(), page.getPath());
    return false;
  }

}
