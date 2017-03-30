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

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class ApplicationInfoTest {

  private static final String APPLICATION_ID = "app1";
  private static final String LABEL = "Application #1";

  private ApplicationInfo underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new ApplicationInfo(APPLICATION_ID, LABEL);
  }

  @Test
  public void testGetApplicationId() throws Exception {
    assertEquals(APPLICATION_ID, underTest.getApplicationId());
    assertEquals(APPLICATION_ID, underTest.toString());
  }

  @Test
  public void testGetLabel() throws Exception {
    assertEquals(LABEL, underTest.getLabel());
  }

  @Test
  public void testSort() {
    Set<ApplicationInfo> apps = new TreeSet<>();
    apps.add(new ApplicationInfo("app1", "App #1"));
    apps.add(new ApplicationInfo("app3", "App #3"));
    apps.add(new ApplicationInfo("app2", "App #2"));

    ApplicationInfo[] appArray = apps.toArray(new ApplicationInfo[apps.size()]);
    assertEquals("app1", appArray[0].getApplicationId());
    assertEquals("app2", appArray[1].getApplicationId());
    assertEquals("app3", appArray[2].getApplicationId());
  }

}
