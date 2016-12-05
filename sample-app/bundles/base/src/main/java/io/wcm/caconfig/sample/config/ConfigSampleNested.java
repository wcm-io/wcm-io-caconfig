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
package io.wcm.caconfig.sample.config;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * Nested config annotation class example
 */
@Configuration(label = "Sample Configuration Nested", description = "This is another sample configuration that is nested.")
public @interface ConfigSampleNested {

  /**
   * @return String parameter
   */
  @Property(label = "String Param", description = "This is a string parameter in the singleton configuration.")
  String stringParam();

  /**
   * @return Sub Config
   */
  @Property(label = "Sub Config", description = "Nested configuration")
  ConfigSampleSub[] sub();

  /**
   * @return Sub Config 2
   */
  @Property(label = "Sub Config 2", description = "Another nested configuration")
  ConfigSampleSub2 sub2();

}
