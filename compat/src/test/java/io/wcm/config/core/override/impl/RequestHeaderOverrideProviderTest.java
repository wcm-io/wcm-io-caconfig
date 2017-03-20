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
package io.wcm.config.core.override.impl;

import static io.wcm.testing.mock.wcmio.sling.ContextPlugins.WCMIO_SLING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.testing.mock.aem.junit.AemContextBuilder;
import io.wcm.testing.mock.wcmio.sling.MockSlingExtensions;

public class RequestHeaderOverrideProviderTest {

  @Rule
  public AemContext context = new AemContextBuilder().plugin(WCMIO_SLING).build();;

  @Before
  public void setUp() {
    MockSlingExtensions.setRequestContext(context, context.request());

    context.request().setHeader(RequestHeaderOverrideProvider.REQUEST_HEADER_PREFIX + "[default]param1", "value1");
    context.request().setHeader(RequestHeaderOverrideProvider.REQUEST_HEADER_PREFIX + "[/config1]param2", "value2");
  }

  @Test
  public void testEnabled() {
    RequestHeaderOverrideProvider provider = context.registerInjectActivateService(new RequestHeaderOverrideProvider(),
        "enabled", true);

    Map<String, String> overrideMap = provider.getOverrideMap();
    assertEquals("value1", overrideMap.get("[default]param1"));
    assertEquals("value2", overrideMap.get("[/config1]param2"));
  }

  @Test
  public void testDisabled() {
    RequestHeaderOverrideProvider provider = context.registerInjectActivateService(new RequestHeaderOverrideProvider(),
        "enabled", false);

    Map<String, String> overrideMap = provider.getOverrideMap();
    assertTrue(overrideMap.isEmpty());
  }

}
