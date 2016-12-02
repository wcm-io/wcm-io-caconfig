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
package io.wcm.config.core.management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.SortedSet;

import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.sling.commons.resource.ImmutableValueMap;

public class ParameterPersistenceDataTest {

  private static final Map<String, Object> SAMPLE_MAP = ImmutableValueMap.builder()
      .put("param1", "value1")
      .put("param2", 55)
      .build();
  private static final SortedSet<String> SAMPLE_SET = ImmutableSortedSet.of("param1");

  @Test
  public void testWithData() {
    ParameterPersistenceData data = new ParameterPersistenceData(SAMPLE_MAP, SAMPLE_SET);
    assertEquals(SAMPLE_MAP, data.getValues());
    assertEquals(SAMPLE_SET, data.getLockedParameterNames());
  }

  @Test
  public void testEmpty() {
    ParameterPersistenceData data = ParameterPersistenceData.EMPTY;
    assertTrue(data.getValues().isEmpty());
    assertTrue(data.getLockedParameterNames().isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithNullMap() {
    new ParameterPersistenceData(null, SAMPLE_SET);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithNullSet() {
    new ParameterPersistenceData(SAMPLE_MAP, null);
  }

}
