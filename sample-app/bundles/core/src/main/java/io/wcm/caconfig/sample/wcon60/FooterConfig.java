/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.caconfig.sample.wcon60;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * Code example from https://wcm-io.atlassian.net/browse/WCON-60 with collections of collections.
 */
@Configuration(
    label = "WCON-60 Footer Configuration",
    description = "Add footer navigation, footer branding and social configuration")
public @interface FooterConfig {

  /**
   * @return Navigational menu's
   */
  @Property(
      label = "Navigational menu's",
      description = "Define multiple menu's which usually will be rendered in order of definition. Please note that configuration and rendering are not necessarily a 1-on-1 match",
      order = 200)
  MenuConfig[] menu();

}