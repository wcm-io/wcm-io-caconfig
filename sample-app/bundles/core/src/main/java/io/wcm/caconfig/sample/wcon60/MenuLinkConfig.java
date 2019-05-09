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

import org.apache.sling.caconfig.annotation.Property;

/**
 * Code example from https://wcm-io.atlassian.net/browse/WCON-60 with collections of collections.
 */
public @interface MenuLinkConfig {

  /**
   * @return Link Text
   */
  @Property(
      label = "Link Text",
      description = "Configure a link text, when this is available, this will receive preference over nav and page title [Optional]",
      order = 200)
  String linkText();

  /**
   * @return Link
   */
  @Property(
      label = "Link",
      description = "Configure the link to an internal page via the path browser or enter an absolute URL [Optional]",
      property = {
          "widgetType=pathbrowser",
          "pathbrowserRootPath=/content"
      },
      order = 300)
  String link();

}
