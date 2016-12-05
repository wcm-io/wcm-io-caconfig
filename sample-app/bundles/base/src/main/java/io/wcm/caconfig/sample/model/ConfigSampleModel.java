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
package io.wcm.caconfig.sample.model;

import java.util.Collection;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.apache.sling.models.annotations.Model;

import io.wcm.caconfig.sample.config.ConfigSample;
import io.wcm.caconfig.sample.config.ConfigSample2;
import io.wcm.caconfig.sample.config.ConfigSampleList;

/**
 * Sling model example
 */
@Model(adaptables = Resource.class)
public class ConfigSampleModel {

  private ConfigSample config;
  private ConfigSample2 config2;
  private Collection<ConfigSampleList> configList;

  /**
   * @param resource Current resource
   */
  public ConfigSampleModel(Resource resource) {
    ConfigurationBuilder configBuilder = resource.adaptTo(ConfigurationBuilder.class);
    this.config = configBuilder.as(ConfigSample.class);
    this.config2 = configBuilder.as(ConfigSample2.class);
    this.configList = configBuilder.asCollection(ConfigSampleList.class);
  }

  /**
   * @return Configuration object
   */
  public ConfigSample getConfig() {
    return this.config;
  }

  /**
   * @return Configuration object
   */
  public ConfigSample2 getConfig2() {
    return this.config2;
  }

  /**
   * @return Configuration list
   */
  public Collection<ConfigSampleList> getConfigList() {
    return this.configList;
  }

}
