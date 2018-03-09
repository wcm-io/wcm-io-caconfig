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
package io.wcm.caconfig.extensions.contextpath.impl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.tenant.Tenant;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RootTemplateContextPathStrategyVersionHistoryTenantTest extends RootTemplateContextPathStrategyTest {

  @Mock
  private Tenant tenant;

  @Override
  @Before
  public void setUp() {
    context.registerAdapter(ResourceResolver.class, Tenant.class, tenant);

    level1 = context.create().page("/content/versionhistory/tenant1/user1/region1").adaptTo(Resource.class);
    level2 = context.create().page("/content/versionhistory/tenant1/user1/region1/site1", TEMPLATE_1).adaptTo(Resource.class);
    level3 = context.create().page("/content/versionhistory/tenant1/user1/region1/site1/en", TEMPLATE_2).adaptTo(Resource.class);
    level4 = context.create().page("/content/versionhistory/tenant1/user1/region1/site1/en/page1").adaptTo(Resource.class);
  }

}
