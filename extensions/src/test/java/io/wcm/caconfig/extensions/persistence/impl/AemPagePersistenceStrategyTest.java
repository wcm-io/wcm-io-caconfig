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

import static org.apache.sling.testing.mock.caconfig.ContextPlugins.CACONFIG;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;

public class AemPagePersistenceStrategyTest {

  @Rule
  public AemContext context = new AemContextBuilder()
      .plugin(CACONFIG)
      .build();

  private Page contentPage;

  @Before
  public void setUp() throws Exception {
    context.registerInjectActivateService(new AemPagePersistenceStrategy());

    MockOsgi.setConfigForPid(context.bundleContext(), "org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy",
        "configRefResourceNames", new String[] { "jcr:content", "." });

    context.create().resource("/conf");
    contentPage = context.create().page("/content/test/site1",
        "sling:configRef", "/conf/test/site1");
  }

  @Test
  public void testGetResource() throws Exception {
    //throw new RuntimeException("not yet implemented");
  }

}
