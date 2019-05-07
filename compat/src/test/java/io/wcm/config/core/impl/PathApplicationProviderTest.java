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
package io.wcm.config.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.config.spi.ApplicationProvider;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class PathApplicationProviderTest {

  private static final String APP_ID = "/apps/app1";
  private static final String APP_LABEL = "Application 1";

  private final AemContext context = new AemContext();

  private ApplicationProvider underTest;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(new PathApplicationProvider(),
        "applicationId", APP_ID,
        "label", APP_LABEL,
        "pathPatterns", new String[] { "^/content/region1(/.+)?$", "^/content/region2(/.+)?$" });
  }

  @Test
  void testApplicationId() {
    assertEquals(APP_ID, underTest.getApplicationId());
  }

  @Test
  void testLabel() {
    assertEquals(APP_LABEL, underTest.getLabel());
  }

  @Test
  void testMatches() {
    assertFalse(underTest.matches(context.resourceResolver().getResource("/")));
    assertFalse(underTest.matches(context.create().resource("/content")));
    assertTrue(underTest.matches(context.create().resource("/content/region1")));
    assertTrue(underTest.matches(context.create().resource("/content/region1/site1")));
    assertTrue(underTest.matches(context.create().resource("/content/region2")));
    assertTrue(underTest.matches(context.create().resource("/content/region2/site2")));
    assertFalse(underTest.matches(context.create().resource("/content/region3")));
    assertFalse(underTest.matches(context.create().resource("/content/region3/site3")));
  }

}
