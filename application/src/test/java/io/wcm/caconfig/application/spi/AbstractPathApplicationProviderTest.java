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
package io.wcm.caconfig.application.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractPathApplicationProviderTest {

  private static final String APP_ID = "/apps/app1";
  private static final String APP_LABEL = "Application 1";

  @Mock
  private Resource resource;

  private ApplicationProvider underTest;

  @Before
  public void setUp() {
    underTest = new AbstractPathApplicationProvider(APP_ID, APP_LABEL, "/content/region1", "/content/region2") {
      // nothing to override
    };
  }

  @Test
  public void testApplicationId() {
    assertEquals(APP_ID, underTest.getApplicationId());
  }

  @Test
  public void testLabel() {
    assertEquals(APP_LABEL, underTest.getLabel());
  }

  @Test
  public void testMatches() {
    when(resource.getPath()).thenReturn("/");
    assertFalse(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content");
    assertFalse(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content/region1");
    assertTrue(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content/region1/site1");
    assertTrue(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content/region2");
    assertTrue(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content/region2/site2");
    assertTrue(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content/region3");
    assertFalse(underTest.matches(resource));

    when(resource.getPath()).thenReturn("/content/region3/site3");
    assertFalse(underTest.matches(resource));
  }

}
