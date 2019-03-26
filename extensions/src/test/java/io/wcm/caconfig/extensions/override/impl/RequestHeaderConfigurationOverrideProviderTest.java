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
package io.wcm.caconfig.extensions.override.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.wcm.sling.commons.request.RequestContext;
import io.wcm.sling.models.injectors.impl.AemObjectInjector;
import io.wcm.sling.models.injectors.impl.ModelsImplConfiguration;
import io.wcm.sling.models.injectors.impl.SlingObjectOverlayInjector;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextBuilder;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.wcmio.sling.MockRequestContext;

@ExtendWith(AemContextExtension.class)
class RequestHeaderConfigurationOverrideProviderTest {

  private static final String HEADER_NAME = "testHeaderName";

  private final AemContext context = new AemContextBuilder()
      .beforeSetUp(aemContext -> {
        // register request context
        aemContext.registerService(RequestContext.class, new MockRequestContext());

        // register sling models extensions
        aemContext.registerInjectActivateService(new ModelsImplConfiguration(),
            ImmutableMap.<String, Object>of("requestThreadLocal", true));

        aemContext.registerInjectActivateService(new AemObjectInjector());
        aemContext.registerInjectActivateService(new SlingObjectOverlayInjector());
      })
      .build();

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    MockRequestContext requestContext = (MockRequestContext)context.getService(RequestContext.class);
    requestContext.setRequest(context.request());

    context.request().addHeader(HEADER_NAME, "param1=value1");
    context.request().addHeader(HEADER_NAME, "[/content/path1]param2=value2");
  }

  @Test
  void testEnabled() {
    RequestHeaderConfigurationOverrideProvider provider = context.registerInjectActivateService(new RequestHeaderConfigurationOverrideProvider(),
        "enabled", true,
        "headerName", HEADER_NAME);

    assertEquals(ImmutableList.of(
        "param1=value1",
        "[/content/path1]param2=value2"),
        provider.getOverrideStrings());
  }

  @Test
  void testDisabled() {
    RequestHeaderConfigurationOverrideProvider provider = context.registerInjectActivateService(new RequestHeaderConfigurationOverrideProvider(),
        "enabled", false);

    assertTrue(provider.getOverrideStrings().isEmpty());
  }

}
