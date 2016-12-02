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
package io.wcm.caconfig.application;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Application meta data.
 */
@ProviderType
public final class ApplicationInfo implements Comparable<ApplicationInfo> {

  private final String applicationId;
  private final String label;

  /**
   * @param applicationId Application id
   * @param label Label
   */
  public ApplicationInfo(String applicationId, String label) {
    this.applicationId = applicationId;
    this.label = label;
  }

  /**
   * @return Application Id. The application is is usually the application path at /apps/ or /libs/.
   */
  public String getApplicationId() {
    return this.applicationId;
  }

  /**
   * @return Display name for application.
   */
  public String getLabel() {
    return this.label;
  }

  @Override
  public int hashCode() {
    return this.applicationId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ApplicationInfo)) {
      return false;
    }
    return this.applicationId.equals(((ApplicationInfo)obj).getApplicationId());
  }

  @Override
  public int compareTo(ApplicationInfo o) {
    return this.applicationId.compareTo(o.getApplicationId());
  }

  @Override
  public String toString() {
    return this.applicationId;
  }

}
