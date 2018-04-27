/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.caconfig.sample.model;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.models.annotations.Model;

import io.wcm.caconfig.sample.config.ConfigSampleNested;
import io.wcm.caconfig.sample.config.ConfigSampleSub;
import io.wcm.caconfig.sample.config.ConfigSampleSub2;

/**
 * Reads configuration from {@link ConfigSampleNested}.
 */
@Model(adaptables = Resource.class)
public class ConfigSampleNestedModel {

  private final ConfigSampleNested config;

  /**
   * @param resource Resource
   */
  public ConfigSampleNestedModel(Resource resource) {
    ConfigurationBuilder configurationBuilder = resource.adaptTo(ConfigurationBuilder.class);
    config = configurationBuilder.as(ConfigSampleNested.class);
  }

  public ConfigSampleNested getConfig() {
    return config;
  }

  public ConfigSampleSub[] getSub() {
    return config.sub();
  }

  public ConfigSampleSub2 getSub2() {
    return config.sub2();
  }

  public ConfigSampleSub2[] getSub2List() {
    return config.sub2List();
  }

}
