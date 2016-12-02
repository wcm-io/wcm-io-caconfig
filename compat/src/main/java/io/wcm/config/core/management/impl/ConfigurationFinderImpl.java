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
package io.wcm.config.core.management.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.config.api.Configuration;
import io.wcm.config.core.impl.ConfigurationImpl;
import io.wcm.config.core.management.ConfigurationFinder;
import io.wcm.config.core.management.ParameterResolver;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.sling.commons.osgi.RankedServices;

/**
 * Default implementation of {@link ConfigurationFinder}.
 */
@Component(immediate = true, metatype = true,
label = "wcm.io Configuration Finder",
description = "Configuration management service to detect context-specific configuration for resources.")
@Service(ConfigurationFinder.class)
public final class ConfigurationFinderImpl implements ConfigurationFinder {

  @Property(label = "Exclude paths",
      description = "List of regular expression patterns for paths which should never be accepted as valie configuration Ids.",
      cardinality = Integer.MAX_VALUE,
      value = {
          "^.*/jcr:content(/.*)?$",
          "^.*/tools$",
          "^.*/tools/config$"
  })
  static final String PROPERTY_EXCLUDE_PATH_PATTERNS = "excludePathPatterns";
  private static final String[] DEFAULT_EXCLUDE_PATH_PATTERNS = new String[] {
      "^.*/jcr:content(/.*)?$",
      "^.*/tools$",
      "^.*/tools/config$"
  };

  /**
   * Configuration finder strategies provided by installed applications.
   */
  @Reference(name = "configurationFinderStrategy", referenceInterface = ConfigurationFinderStrategy.class,
      cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC)
  private final RankedServices<ConfigurationFinderStrategy> finderStrategies = new RankedServices<>();

  @Reference
  private ApplicationFinder applicationFinder;
  @Reference
  private ParameterResolver parameterResolver;

  private List<Pattern> excludePathPatterns = ImmutableList.of();

  /**
   * Ordering of configuration id by "closed match" - is simply a descending alphanumeric sort.
   */
  private static final Comparator<String> CONFIGURATION_ID_CLOSED_MATCH_COMPARATOR = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      return o2.compareTo(o1);
    }
  };

  private static final Logger log = LoggerFactory.getLogger(ConfigurationFinderImpl.class);

  @Activate
  void activate(final ComponentContext ctx) {
    String[] excludePathPatternStrings = PropertiesUtil.toStringArray(
        ctx.getProperties().get(PROPERTY_EXCLUDE_PATH_PATTERNS), DEFAULT_EXCLUDE_PATH_PATTERNS);
    excludePathPatterns = new ArrayList<>();
    for (String excludePathPatternString : excludePathPatternStrings) {
      try {
        excludePathPatterns.add(Pattern.compile(excludePathPatternString));
      }
      catch (PatternSyntaxException ex) {
        log.warn("Ignoring invalid regular expression: " + excludePathPatternString, ex);
      }
    }
  }

  @Override
  public Configuration find(Resource resource) {
    return find(resource, findApplicationId(resource));
  }

  @Override
  public Configuration find(Resource resource, String applicationId) {
    Set<String> allIds = getAllMatchingConfigurationIds(resource, applicationId);
    Configuration config = readConfiguration(resource.getResourceResolver(), allIds);
    if (log.isDebugEnabled()) {
      log.debug("find({}, {}): {}", resource.getPath(), applicationId, config);
    }
    return config;
  }

  @Override
  public Iterator<Configuration> findAll(Resource resource) {
    return findAll(resource, findApplicationId(resource));
  }

  private String findApplicationId(Resource resource) {
    ApplicationInfo application = applicationFinder.find(resource);
    if (application != null) {
      return application.getApplicationId();
    }
    return null;
  }

  @Override
  public Iterator<Configuration> findAll(Resource resource, String applicationId) {
    List<Configuration> configurations = new ArrayList<>();
    List<String> allIds = new LinkedList<String>(getAllMatchingConfigurationIds(resource, applicationId));
    while (!allIds.isEmpty()) {
      configurations.add(readConfiguration(resource.getResourceResolver(), allIds));
      allIds.remove(0);
    }
    if (log.isDebugEnabled()) {
      log.debug("findAll({}, {}): {}", resource.getPath(), applicationId, Joiner.on(",").join(configurations));
    }
    return configurations.iterator();
  }

  private Set<String> getAllMatchingConfigurationIds(Resource resource, String applicationId) {
    Set<String> allIds = new TreeSet<>(CONFIGURATION_ID_CLOSED_MATCH_COMPARATOR);
    for (ConfigurationFinderStrategy finderStrategy : finderStrategies) {
      if (matchesApplicationId(applicationId, finderStrategy.getApplicationId())) {
        Iterator<String> configurationIds = finderStrategy.findConfigurationIds(resource);
        while (configurationIds.hasNext()) {
          String configurationId = configurationIds.next();
          if (isAccepted(configurationId)) {
            allIds.add(configurationId);
          }
        }
      }
    }
    return allIds;
  }

  private boolean isAccepted(String configurationId) {
    for (Pattern pattern : excludePathPatterns) {
      if (pattern.matcher(configurationId).matches()) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesApplicationId(String expected, String actual) {
    if (expected == null) {
      return true;
    }
    else {
      return StringUtils.equals(expected, actual);
    }
  }

  private Configuration readConfiguration(ResourceResolver resolver, Collection<String> configurationIds) {
    if (configurationIds.isEmpty()) {
      return null;
    }
    String topmostConfigurationId = configurationIds.iterator().next();
    Map<String, Object> values = this.parameterResolver.getEffectiveValues(resolver, configurationIds);
    return new ConfigurationImpl(topmostConfigurationId, values);
  }

  void bindConfigurationFinderStrategy(ConfigurationFinderStrategy service, Map<String, Object> props) {
    finderStrategies.bind(service, props);
  }

  void unbindConfigurationFinderStrategy(ConfigurationFinderStrategy service, Map<String, Object> props) {
    finderStrategies.unbind(service, props);
  }

}
