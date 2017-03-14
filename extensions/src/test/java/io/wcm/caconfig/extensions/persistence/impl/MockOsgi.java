/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.caconfig.extensions.persistence.impl;

import static org.apache.sling.testing.mock.osgi.MapUtil.toDictionary;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/*
 * Can be removed once updated to osgi-mock 1.9.4/2.2.4 or higher.
 */
final class MockOsgi {

  private MockOsgi() {
    // static methods only
  }

  /**
   * Set configuration via ConfigurationAdmin service in bundle context for component with given pid.
   * @param bundleContext Bundle context
   * @param pid PID
   * @param properties Configuration properties
   */
  public static void setConfigForPid(BundleContext bundleContext, String pid, Map<String, Object> properties) {
    setConfigForPid(bundleContext, pid, toDictionary(properties));
  }

  /**
   * Set configuration via ConfigurationAdmin service in bundle context for component with given pid.
   * @param bundleContext Bundle context
   * @param pid PID
   * @param properties Configuration properties
   */
  public static void setConfigForPid(BundleContext bundleContext, String pid, Object... properties) {
    setConfigForPid(bundleContext, pid, toDictionary(properties));
  }

  private static void setConfigForPid(BundleContext bundleContext, String pid, Dictionary<String, Object> properties) {
    ConfigurationAdmin configAdmin = getConfigAdmin(bundleContext);
    if (configAdmin == null) {
      throw new RuntimeException("ConfigurationAdmin service is not registered in bundle context.");
    }
    try {
      Configuration config = configAdmin.getConfiguration(pid);
      config.update(properties);
    }
    catch (IOException ex) {
      throw new RuntimeException("Unable to update configuration for pid '" + pid + "'.", ex);
    }
  }

  /**
   * Get configuration admin.
   * @param bundleContext Bundle context
   * @return Configuration admin or null if not registered.
   */
  private static ConfigurationAdmin getConfigAdmin(BundleContext bundleContext) {
    ServiceReference ref = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
    if (ref != null) {
      return (ConfigurationAdmin)bundleContext.getService(ref);
    }
    return null;
  }

}
