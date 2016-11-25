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
package io.wcm.caconfig.editor.impl;

import java.io.IOException;
import java.util.SortedSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.CharEncoding;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Get configuration names with labels and descriptions.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.resourceTypes=/apps/wcm-io/caconfig/editor/components/page/editor",
    "sling.servlet.extensions=json",
    "sling.servlet.selectors=" + ConfigNamesServlet.SELECTOR
})
public class ConfigNamesServlet extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "configNames";

  @Reference
  private ConfigurationManager configManager;

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    try {
      response.setContentType("application/json;charset=" + CharEncoding.UTF_8);
      response.getWriter().write(getConfigNames().toString());
    }
    catch (JSONException ex) {
      throw new ServletException("Unable to generate JSON.", ex);
    }
  }

  private JSONArray getConfigNames() throws JSONException {
    JSONArray output = new JSONArray();

    SortedSet<String> configNames = configManager.getConfigurationNames();
    for (String configName : configNames) {
      ConfigurationMetadata metadata = configManager.getConfigurationMetadata(configName);
      if (metadata != null) {
        JSONObject item = new JSONObject();
        item.putOpt("configName", configName);
        item.putOpt("label", metadata.getLabel());
        item.putOpt("description", metadata.getDescription());
        if (metadata.isCollection()) {
          item.put("collection", true);
        }
        output.put(item);
      }
    }

    return output;
  }

}
