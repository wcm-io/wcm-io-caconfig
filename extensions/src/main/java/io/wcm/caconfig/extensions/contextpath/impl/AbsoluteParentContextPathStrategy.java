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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    @AttributeDefinition(name = "Retrieve context for ancestor page instead",
        description = "List of template paths and ancestor levels, separated by '='. Each page matching the template will retrieve instead the context for an ancestor page on the given level. This is useful for CAC editor pages which are often outside the main content tree.",
        required = true)
    String[] templatesWithAncestorLevel();
    
    @AttributeDefinition(name = "Context path whitelist",
        description = "Expression to match context paths. Context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.",
        required = true)
    String contextPathRegex() default "^/content(/.+)$";

    @AttributeDefinition(name = "Context path blacklist",
        description = "Expression to match context paths. Context paths matching this expression are not allowed.",
        required = true)
    String contextPathBlacklistRegex() default "^.*/tools(/config(/.+)?)?$";

    @AttributeDefinition(name = "Config path patterns",
        description = "Expression to derive the config path from the context path. Regex group references like $1 can be used.",
        required = true)
    String[] configPathPatterns() default "/conf$1";

    @AttributeDefinition(name = "Service Ranking",
        description = "Priority of context path strategy (higher = higher priority).")
    int service_ranking() default 2000;

    String webconsole_configurationFactory_nameHint() default "{applicationId} levels={levels}";

  }

  private Set<Integer> levels;
  private Pattern contextPathRegex;
  private Pattern contextPathBlacklistRegex;
  private String[] configPathPatterns;
  private int serviceRanking;
  private Map<String, Integer> templatesAndAncestorLevels;

  private static final Logger log = LoggerFactory.getLogger(AbsoluteParentContextPathStrategy.class);

  @Activate
  void activate(Config config) {
    levels = new TreeSet<>();
    if (config.levels() != null) {
      for (int level : config.levels()) {
        levels.add(level);
      }
    }
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
    templatesAndAncestorLevels = getTemplatesAndAncestorLevelsMapFromConfig(config.templatesWithAncestorLevel());
  }

  static Map<String, Integer> getTemplatesAndAncestorLevelsMapFromConfig(String[] config) {
    Map<String, Integer> templatesAndAncestorLevels = new HashMap<>();
    if (config == null) {
        return templatesAndAncestorLevels;
    }
    int line = 1;
    for (String configItem : config) {
        String[] parts = configItem.split("=", 2);
        if (parts == null || parts.length != 2) {
            throw new IllegalArgumentException("Each line in the config property 'templatesWithAncestorLevel' must follow the format '<template path>=<level>'! Line " + line + " is not following this pattern!");
        }
        try {
            Integer level = Integer.valueOf(parts[1]);
            templatesAndAncestorLevels.put(parts[0], level);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Each line in the config property 'templatesWithAncestorLevel' must follow the format '<template path>=<level>'! Line " + line + " does not contain a numeric level!", e);
        }
        
        line++;
    }
    return templatesAndAncestorLevels;
  }

  @Override
  public Iterator<ContextResource> findContextResources(Resource resource) {
    if (!isValidConfig()) {
      return Collections.emptyIterator();
    }
    try {
      resource = getBaseResource(resource);
    } catch (IllegalStateException e) {
        log.debug("Could not get base resource: " + e.getMessage(), e);
        return Collections.emptyIterator();
    }
    ResourceResolver resourceResolver = resource.getResourceResolver();
    List<ContextResource> contextResources = new ArrayList<>();
    for (int level : this.levels) {
      String contextPath = getAbsoluteParent(resource, level, resourceResolver);
      if (StringUtils.isNotEmpty(contextPath)) {
        Resource contextResource = resource.getResourceResolver().getResource(contextPath);
        if (contextResource != null) {
          for (String configPathPattern : configPathPatterns) {
            String configRef = deriveConfigRef(contextPath, configPathPattern, resourceResolver);
            if (configRef != null) {
              contextResources.add(new ContextResource(contextResource, configRef, serviceRanking));
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

  private Resource getBaseResource(Resource resource) {
      if (templatesAndAncestorLevels.isEmpty()) {
          return resource;
      }
      // get page for current resource
      ResourceResolver resourceResolver = resource.getResourceResolver();
      if (resourceResolver == null) {
          throw new IllegalStateException("Could not get resource resolver for resource!");
      }
      PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
      if (pageManager == null) {
          throw new IllegalStateException("Could not get PageManager from ResourceResolver!");
      }
      Page page = pageManager.getContainingPage(resource);
      if (page == null) {
          return resource;
      }
      String template = page.getProperties().get(NameConstants.PN_TEMPLATE, String.class);
      Integer ancestorLevel = templatesAndAncestorLevels.get(template);
      if (ancestorLevel == null) {
          return resource;
      }
      Page basePage = page.getParent(ancestorLevel);
      if (basePage == null) {
          throw new IllegalStateException("There is no parent page for level " + ancestorLevel +  " of page " + page.getPath());
      }
      return basePage.adaptTo(Resource.class);
  }

  private String getAbsoluteParent(Resource resource, int absoluteParent, ResourceResolver resourceResolver) {
    return Path.getAbsoluteParent(resource.getPath(), absoluteParent, resourceResolver);
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

}
