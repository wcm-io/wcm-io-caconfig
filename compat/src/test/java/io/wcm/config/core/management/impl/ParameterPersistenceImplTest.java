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
package io.wcm.config.core.management.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.SortedSet;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import com.google.common.collect.ImmutableSortedSet;

import io.wcm.config.core.management.ParameterPersistence;
import io.wcm.config.core.management.ParameterPersistenceData;
import io.wcm.config.spi.ParameterPersistenceProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;

/**
 * Test {@link ParameterPersistenceImpl} with basic property values, but multiple providers and merging.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterPersistenceImplTest {

  private static final String CONFIG_ID = "/config1";

  private static final Map<String, Object> SAMPLE_VALUES = ImmutableValueMap.builder()
      .put("prop1", "value1")
      .put("prop2", 55)
      .build();
  private static final SortedSet<String> SAMPLE_LOCKED_PARAMETER_NAMES = ImmutableSortedSet.of("prop1", "prop5");
  private static final Map<String, Object> SAMPLE_VALUES_INTERNAL = ImmutableValueMap.builder()
      .put("prop1", "value1")
      .put("prop2", 55)
      .put(ParameterPersistence.PN_LOCKED_PARAMETER_NAMES, SAMPLE_LOCKED_PARAMETER_NAMES.toArray(new String[SAMPLE_LOCKED_PARAMETER_NAMES.size()]))
      .build();

  private static final Map<String, Object> SAMPLE_VALUES_2 = ImmutableValueMap.builder()
      .put("prop3", "value3")
      .put("prop2", 66)
      .build();

  @Mock
  private ResourceResolver resolver;

  private DummyPersistenceProvider persistenceProvider1;
  private static final Map<String, Object> SERVICE_PROPS_1 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 1L,
          Constants.SERVICE_RANKING, 10);

  private DummyPersistenceProvider persistenceProvider2;
  private static final Map<String, Object> SERVICE_PROPS_2 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 2L,
          Constants.SERVICE_RANKING, 20);

  @InjectMocks
  private ParameterPersistenceImpl underTest;

  @Before
  public void setUp() {
    persistenceProvider1 = new DummyPersistenceProvider();
    persistenceProvider2 = new DummyPersistenceProvider();
    underTest.bindParameterPersistenceProvider(persistenceProvider1, SERVICE_PROPS_1);
    underTest.bindParameterPersistenceProvider(persistenceProvider2, SERVICE_PROPS_2);
  }

  @Test
  public void testGetValues_FirstProvider() {
    persistenceProvider1.setMap(SAMPLE_VALUES_INTERNAL);
    persistenceProvider2.setMap(SAMPLE_VALUES_2);

    ParameterPersistenceData data = underTest.getData(resolver, CONFIG_ID);
    assertEqualsInclArrayValues(SAMPLE_VALUES, data.getValues());
    assertEquals(SAMPLE_LOCKED_PARAMETER_NAMES, data.getLockedParameterNames());
  }

  @Test
  public void testGetValues_SecondProvider() {
    persistenceProvider1.setMap(null);
    persistenceProvider1.setMap(SAMPLE_VALUES_2);

    ParameterPersistenceData data = underTest.getData(resolver, CONFIG_ID);
    assertEqualsInclArrayValues(SAMPLE_VALUES_2, data.getValues());
    assertTrue(data.getLockedParameterNames().isEmpty());
  }

  @Test
  public void testGetValues_NoProvider() {
    persistenceProvider1.setMap(null);
    persistenceProvider1.setMap(null);

    ParameterPersistenceData data = underTest.getData(resolver, CONFIG_ID);
    assertTrue(data.getValues().isEmpty());
    assertTrue(data.getLockedParameterNames().isEmpty());
  }

  @Test
  public void testStoreValues_FirstProvider() throws PersistenceException {
    persistenceProvider1.setStoreSuccess(true);
    persistenceProvider2.setStoreSuccess(true);

    underTest.storeData(resolver, CONFIG_ID, new ParameterPersistenceData(SAMPLE_VALUES, SAMPLE_LOCKED_PARAMETER_NAMES));

    assertEqualsInclArrayValues(SAMPLE_VALUES_INTERNAL, persistenceProvider1.getMap());
    assertNull(persistenceProvider2.getMap());
  }

  @Test
  public void testStoreValues_SecondProvider() throws PersistenceException {
    persistenceProvider1.setStoreSuccess(false);
    persistenceProvider2.setStoreSuccess(true);

    underTest.storeData(resolver, CONFIG_ID, new ParameterPersistenceData(SAMPLE_VALUES_2, ImmutableSortedSet.<String>of()));

    assertNull(persistenceProvider1.getMap());
    assertEqualsInclArrayValues(SAMPLE_VALUES_2, persistenceProvider2.getMap());
  }

  @Test(expected = PersistenceException.class)
  public void testStoreValues_NoProvider() throws PersistenceException {
    persistenceProvider1.setStoreSuccess(false);
    persistenceProvider2.setStoreSuccess(false);

    underTest.storeData(resolver, CONFIG_ID, new ParameterPersistenceData(SAMPLE_VALUES, SAMPLE_LOCKED_PARAMETER_NAMES));

    assertNull(persistenceProvider1.getMap());
    assertNull(persistenceProvider2.getMap());
  }

  @Test
  public void testStoreValues_MergeWithExisting() throws PersistenceException {
    persistenceProvider1.setMap(SAMPLE_VALUES_INTERNAL);
    persistenceProvider1.setStoreSuccess(true);

    underTest.storeData(resolver, CONFIG_ID, new ParameterPersistenceData(SAMPLE_VALUES_2, ImmutableSortedSet.<String>of()), true);

    Map<String,Object> expecMap = ImmutableValueMap.builder()
        .put("prop1", "value1")
        .put("prop3", "value3")
        .put("prop2", 66)
        .put(ParameterPersistence.PN_LOCKED_PARAMETER_NAMES, new String[] {
            "prop1", "prop5"
        })
        .build();
    assertEqualsInclArrayValues(expecMap, persistenceProvider1.getMap());
  }

  @Test
  public void testStoreValues_MergeWithExisting_WithLockedParameterNames() throws PersistenceException {
    persistenceProvider1.setMap(SAMPLE_VALUES_INTERNAL);
    persistenceProvider1.setStoreSuccess(true);

    underTest.storeData(resolver, CONFIG_ID, new ParameterPersistenceData(SAMPLE_VALUES_2, ImmutableSortedSet.of("prop3")), true);

    Map<String, Object> expecMap = ImmutableValueMap.builder()
        .put("prop1", "value1")
        .put("prop3", "value3")
        .put("prop2", 66)
        .put(ParameterPersistence.PN_LOCKED_PARAMETER_NAMES, new String[] {
            "prop1", "prop3", "prop5"
        })
        .build();
    assertEqualsInclArrayValues(expecMap, persistenceProvider1.getMap());
  }

  @Test
  public void testStoreWithMapConversion() throws PersistenceException {
    persistenceProvider1.setStoreSuccess(true);

    Map<String, Object> valuesToStore = ImmutableValueMap.builder()
        .put("mapValue", PersistenceTypeConversionTest.SAMPLE_MAP)
        .build();
    underTest.storeData(resolver, CONFIG_ID, new ParameterPersistenceData(valuesToStore, ImmutableSortedSet.<String>of()));

    Map<String, Object> storedValues = ImmutableValueMap.builder()
        .put("mapValue", PersistenceTypeConversionTest.SAMPLE_MAP_PERSISTENCE)
        .build();
    assertEqualsInclArrayValues(storedValues, persistenceProvider1.getMap());
  }

  /**
   * Asserts two maps and if value is an string array compares the arrays for equality as well.
   */
  static void assertEqualsInclArrayValues(Map<String, Object> map1, Map<String, Object> map2) {
    assertEquals("map size", map1.size(), map2.size());
    for (Map.Entry<String, Object> entry : map1.entrySet()) {
      if (entry.getValue() instanceof String[]) {
        assertArrayEquals("property " + entry.getKey(), (String[])entry.getValue(), (String[])map2.get(entry.getKey()));
      }
      else {
        assertEquals("property " + entry.getKey(), entry.getValue(), map2.get(entry.getKey()));
      }
    }
  }

  static class DummyPersistenceProvider implements ParameterPersistenceProvider {

    private Map<String, Object> map;
    private boolean storeSuccess;

    @Override
    public Map<String, Object> get(ResourceResolver resolver, String configurationId) {
      return this.map;
    }

    @Override
    public boolean store(ResourceResolver resolver, String configurationId, Map<String, Object> values) throws PersistenceException {
      if (this.storeSuccess) {
        this.map = values;
      }
      return this.storeSuccess;
    }

    public Map<String, Object> getMap() {
      return this.map;
    }

    public void setMap(Map<String, Object> map) {
      this.map = map;
    }

    public void setStoreSuccess(boolean storeSuccess) {
      this.storeSuccess = storeSuccess;
    }

  }

}
