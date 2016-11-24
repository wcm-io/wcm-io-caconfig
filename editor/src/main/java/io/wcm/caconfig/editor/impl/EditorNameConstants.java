/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.caconfig.editor.impl;

/**
 * Internal constants
 */
final class EditorNameConstants {

  private EditorNameConstants() {
    // only constants
  }

  /**
   * Name of the property to set the name of the parameter
   */
  public static final String PARAMETER_NAME = "name";

  /**
   * Name of the property to set the the application id of the parameter
   */
  public static final String PARAMETER_VALUE = "value";

  /**
   * Name of the property to set the inherited value of the parameter
   */
  public static final String INHERITED_VALUE = "inheritedValue";

  /**
   * Name of the property to set the application id of the parameter
   */
  public static final String APPLICATION_ID = "application";

  /**
   * Name of the property to set the flag whether the parameter value is inherited
   */
  public static final String INHERITED = "inherited";

  /**
   * Name of the property to set the flag whether the parameter value is locked
   */
  public static final String LOCKED = "locked";

  /**
   * Name of the property to set the flag whether the parameter value was locked and cannot be unlocked
   */
  public static final String LOCKED_INHERITED = "lockedInherited";

}
