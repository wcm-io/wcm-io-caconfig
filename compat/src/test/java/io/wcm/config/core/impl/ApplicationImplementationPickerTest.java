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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.wcm.config.core.management.Application;
import io.wcm.config.core.management.ApplicationFinder;
import io.wcm.testing.mock.aem.junit.AemContext;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("null")
public class ApplicationImplementationPickerTest {

  @Rule
  public AemContext context = new AemContext();

  private static final String APP_ID_1 = "app1";
  private static final String APP_ID_2 = "app2";
  private static final Application APP_1 = new Application(APP_ID_1, null);
  private static final Application APP_2 = new Application(APP_ID_2, null);
  private static final Class<?>[] IMPL_ARRAY = new Class<?>[] {
    Impl0.class,
    Impl1.class,
    Impl2.class
  };

  @Mock
  private ApplicationFinder applicationFinder;
  @Mock
  private Resource resourceApp1;
  @Mock
  private Resource resourceApp2;
  @Mock
  private Resource resourceOther;

  private ApplicationImplementationPicker underTest;

  @Before
  public void setUp() throws Exception {
    when(applicationFinder.find(resourceApp1)).thenReturn(APP_1);
    when(applicationFinder.find(resourceApp2)).thenReturn(APP_2);

    context.registerService(ApplicationFinder.class, applicationFinder);
    underTest = context.registerInjectActivateService(new ApplicationImplementationPicker());
  }

  @Test
  public void testResourceApp1() {
    assertSame(Impl1.class, underTest.pick(Comparable.class, IMPL_ARRAY, resourceApp1));
  }

  @Test
  public void testResourceApp2() {
    assertSame(Impl2.class, underTest.pick(Comparable.class, IMPL_ARRAY, resourceApp2));
  }

  @Test
  public void testResourceOther() {
    assertSame(Impl0.class, underTest.pick(Comparable.class, IMPL_ARRAY, resourceOther));
  }


  @Model(adaptables = Resource.class, adapters = Comparable.class)
  private static class Impl0 implements Comparable {
    @Override
    public int compareTo(Object o) {
      return 0;
    }
  }

  @Model(adaptables = Resource.class, adapters = Comparable.class)
  @io.wcm.config.spi.annotations.Application(APP_ID_1)
  private static class Impl1 implements Comparable {
    @Override
    public int compareTo(Object o) {
      return 0;
    }
  }

  @Model(adaptables = Resource.class, adapters = Comparable.class)
  @io.wcm.config.spi.annotations.Application(APP_ID_2)
  private static class Impl2 implements Comparable {
    @Override
    public int compareTo(Object o) {
      return 0;
    }
  }

}
