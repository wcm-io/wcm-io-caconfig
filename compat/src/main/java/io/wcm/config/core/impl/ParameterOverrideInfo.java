/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains information about a parameter override from parameter override providers.
 */
class ParameterOverrideInfo {

  /**
   * Scope for system default value
   */
  public static final String DEFAULT_SCOPE = "default";

  /**
   * Suffix to mark a parameter override as locked, the parameter value cannot be overridden in nested configuration
   * scopes.
   */
  public static final String LOCKED_SUFFIX = ":locked";

  private static final Pattern OVERRIDE_STRING_PATTERN = Pattern.compile("^(\\[([^\\[\\]:]+)(" + LOCKED_SUFFIX + ")?\\])?([^\\[\\]]+)$");

  private final String configurationId;
  private final boolean overrideSystemDefault;
  private final boolean isLocked;
  private final String parameterName;

  ParameterOverrideInfo(String overrideString) {
    Matcher matcher = OVERRIDE_STRING_PATTERN.matcher(overrideString);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid override string identifying scope and parameter: " + overrideString);
    }

    boolean systemScope = false;
    String scope = matcher.group(2);

    // evaluate overrideSystemDefault
    if (StringUtils.equals(DEFAULT_SCOPE, scope)) {
      overrideSystemDefault = true;
      systemScope = true;
    }
    else {
      overrideSystemDefault = false;
    }

    // evaluate isLocked
    if (StringUtils.equals(LOCKED_SUFFIX.substring(1), scope)) {
      isLocked = true;
      systemScope = true;
    }
    else {
      isLocked = StringUtils.equals(matcher.group(3), LOCKED_SUFFIX)
          || StringUtils.isEmpty(matcher.group(2));
    }

    if (matcher.group(3) != null && systemScope) {
      throw new IllegalArgumentException("Invalid override string identifying scope and parameter "
          + "- ':locked' not allowed here: " + overrideString);
    }

    // evaluate configurationId
    if (systemScope) {
      configurationId = null;
    }
    else {
      configurationId = matcher.group(2);
    }

    // evaluate parameterName
    parameterName = matcher.group(4);
  }

  /**
   * @return Configuration ID
   */
  public String getConfigurationId() {
    return this.configurationId;
  }

  /**
   * @return True if the scope "default" was set
   */
  public boolean isOverrideSystemDefault() {
    return this.overrideSystemDefault;
  }

  /**
   * @return True if the parameter override was marked as "locked" for the given configuration ID
   */
  public boolean isLocked() {
    return this.isLocked;
  }

  /**
   * @return Parameter name
   */
  public String getParameterName() {
    return this.parameterName;
  }

}
