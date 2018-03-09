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
package io.wcm.caconfig.extensions.contextpath.impl;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.apache.sling.caconfig.resource.spi.ContextResource;

import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.junit.AemContext;

final class TestUtils {

  private TestUtils() {
    // static methods only
  }

  public static void assertNoResult(AemContext context, Iterator<ContextResource> result) {
    assertResult(context, result);
  }

  public static void assertResult(AemContext context, Iterator<ContextResource> result, String... paths) {
    if (paths.length % 2 != 0) {
      throw new IllegalArgumentException("Expected path pairs.");
    }
    List<ContextResource> resultList = ImmutableList.copyOf(result);
    int expectedSize = paths.length / 2;
    assertEquals("Number of paths", expectedSize, resultList.size());
    for (int i = 0; i < expectedSize / 2; i++) {
      String expectedContextPath = paths[i * 2];
      String expectedConfigRef = paths[i * 2 + 1];
      assertEquals(expectedContextPath, Path.getPathWithoutVersionHistory(resultList.get(i).getResource().getPath(), context.resourceResolver()));
      assertEquals(expectedConfigRef, resultList.get(i).getConfigRef());
    }
  }

}
