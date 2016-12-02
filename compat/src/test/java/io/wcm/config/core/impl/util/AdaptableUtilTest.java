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
package io.wcm.config.core.impl.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AdaptableUtilTest {

  @Mock
  private Resource resource;
  @Mock
  private ResourceResolver resolver;
  @Mock
  private SlingHttpServletRequest request;

  @Before
  public void setUp() {
    when(request.getResource()).thenReturn(resource);
  }

  @Test
  public void testResource() {
    assertSame(resource, AdaptableUtil.getResource(resource));
  }

  @Test
  public void testRequest() {
    assertSame(resource, AdaptableUtil.getResource(request));
  }

  @Test
  public void testOther() {
    assertNull(AdaptableUtil.getResource(resolver));
  }

}
