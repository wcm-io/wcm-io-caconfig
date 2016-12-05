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
 * Another config annotation class example
 */
@Configuration(label = "Sample Configuration 2", description = "This is another sample configuration.")
public @interface ConfigSample2 {

  /**
   * @return String parameter
   */
  @Property(label = "String Param", description = "This is a string parameter in the singleton configuration.")
  String stringParam() default "Default Value";

  /**
   * @return Integer parameter
   */
  @Property(label = "Double Param")
  double doubleParam();

  /**
   * @return Integer array parameter
   */
  @Property(label = "Integer Array Param")
  int[] intArrayParam();

}
