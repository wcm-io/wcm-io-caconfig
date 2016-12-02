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

import static io.wcm.config.api.ParameterBuilder.create;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import io.wcm.config.api.Parameter;
import io.wcm.config.core.management.ParameterOverride;
import io.wcm.config.core.management.ParameterPersistence;
import io.wcm.config.core.management.ParameterPersistenceData;
import io.wcm.config.spi.ParameterProvider;
import io.wcm.sling.commons.resource.ImmutableValueMap;

@RunWith(MockitoJUnitRunner.class)
public class ParameterResolverImplTest {

  private static final String APP_ID_1 = "/apps/app1";
  private static final String APP_ID_2 = "/apps/app2";

  @Mock
  private ResourceResolver resolver;
  @Mock
  private ComponentContext componentContext;
  @Mock
  private BundleContext bundleContext;
  @Mock
  private ServiceReference serviceReference;
  @Mock
  private ParameterPersistence parameterPersistence;
  @Mock
  private ParameterOverride parameterOverride;

  @Mock
  private ParameterProvider parameterProvider1;
  private static final Map<String, Object> SERVICE_PROPS_1 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 1L,
          Constants.SERVICE_RANKING, 10);
  private static final Parameter<String> PARAM11 = create("param11", String.class, APP_ID_1).build();
  private static final Parameter<String> PARAM12 = create("param12", String.class, APP_ID_1)
      .defaultValue("defValue").build();
  private static final Parameter<String> PARAM13 = create("param13", String.class, APP_ID_1)
      .defaultValue("defValue").defaultOsgiConfigProperty("my.service:prop1").build();
  private static final Parameter<Map> PARAM_MAP = create("paramMap", Map.class, APP_ID_1).build();

  @Mock
  private ParameterProvider parameterProvider2;
  private static final Map<String, Object> SERVICE_PROPS_2 = ImmutableValueMap.of(
      Constants.SERVICE_ID, 2L,
      Constants.SERVICE_RANKING, 20);
  private static final Parameter<Integer> PARAM21 = create("param21", Integer.class, APP_ID_2)
      .defaultValue(55).build();

  @InjectMocks
  private ParameterResolverImpl underTest;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    when(bundleContext.getServiceReference("my.service")).thenReturn(serviceReference);
    when(serviceReference.getProperty("prop1")).thenReturn("valueFromOsgiConfig");

    when(parameterPersistence.getData(same(resolver), anyString())).thenReturn(toData(ImmutableValueMap.of()));
    when(parameterOverride.getOverrideSystemDefault(any(Parameter.class))).thenReturn(null);
    when(parameterOverride.getOverrideForce(anyString(), any(Parameter.class))).thenReturn(null);
    when(parameterOverride.getLockedParameterNames(anyString())).thenReturn(ImmutableSet.<String>of());

    Set<Parameter<?>> params1 = new HashSet<>();
    params1.add(PARAM11);
    params1.add(PARAM12);
    params1.add(PARAM13);
    params1.add(PARAM_MAP);
    when(parameterProvider1.getParameters()).thenReturn(params1);

    Set<Parameter<?>> params2 = new HashSet<>();
    params2.add(PARAM21);
    when(parameterProvider2.getParameters()).thenReturn(params2);

    underTest.bindParameterProvider(parameterProvider1, SERVICE_PROPS_1);
    underTest.bindParameterProvider(parameterProvider2, SERVICE_PROPS_2);
  }

  @Test
  public void testGetAllParameters() {
    Set<Parameter<?>> allParameters = ImmutableSet.of(PARAM11, PARAM12, PARAM13, PARAM_MAP, PARAM21);
    assertEquals(allParameters, underTest.getAllParameters());
  }

  @Test
  public void testDefaultValues() {
    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertNull(values.get("param11"));
    assertEquals("defValue", values.get("param12"));
    assertEquals("valueFromOsgiConfig", values.get("param13"));
    assertEquals(55, values.get("param21"));
  }

  @Test
  public void testOverrideSystemDefault() {
    when(parameterOverride.getOverrideSystemDefault(PARAM11)).thenReturn("override11");
    when(parameterOverride.getOverrideSystemDefault(PARAM12)).thenReturn("override12");
    when(parameterOverride.getOverrideSystemDefault(PARAM13)).thenReturn("override13");
    when(parameterOverride.getOverrideSystemDefault(PARAM21)).thenReturn(66);

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertEquals("override11", values.get("param11"));
    assertEquals("override12", values.get("param12"));
    assertEquals("override13", values.get("param13"));
    assertEquals(66, values.get("param21"));
  }

  @Test
  public void testConfiguredValues() {
    when(parameterPersistence.getData(resolver, "/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "config11")
        .put("param12", "config12")
        .put("param13", "config13")
        .put("param21", 77)
        .build()));

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertEquals("config11", values.get("param11"));
    assertEquals("config12", values.get("param12"));
    assertEquals("config13", values.get("param13"));
    assertEquals(77, values.get("param21"));
  }

  @Test
  public void testConfigurationHierarchy() {
    when(parameterPersistence.getData(resolver, "/region1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "r11")
        .put("param12", "r12")
        .put("param21", 88)
        .build()));
    when(parameterPersistence.getData(resolver, "/region1/site1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "s11")
        .put("param21", 99)
        .build()));
    when(parameterPersistence.getData(resolver, "/region1/site1/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "c11")
        .build()));

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of(
        "/region1/site1/config1", "/region1/site1", "/region1"));
    assertEquals("c11", values.get("param11"));
    assertEquals("r12", values.get("param12"));
    assertEquals("valueFromOsgiConfig", values.get("param13"));
    assertEquals(99, values.get("param21"));
  }

  @Test
  public void testOverrideForce() {
    when(parameterPersistence.getData(resolver, "/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "config11")
        .put("param12", "config12")
        .put("param13", "config13")
        .put("param21", 77)
        .build()));

    when(parameterOverride.getOverrideForce("/config1", PARAM11)).thenReturn("override11");
    when(parameterOverride.getOverrideForce("/config1", PARAM12)).thenReturn("override12");
    when(parameterOverride.getOverrideForce("/config1", PARAM13)).thenReturn("override13");
    when(parameterOverride.getOverrideForce("/config1", PARAM21)).thenReturn(66);

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertEquals("override11", values.get("param11"));
    assertEquals("override12", values.get("param12"));
    assertEquals("override13", values.get("param13"));
    assertEquals(66, values.get("param21"));
  }

  @Test
  public void testConfigurationHierarchyWithOverrides() {
    when(parameterPersistence.getData(resolver, "/region1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "r11")
        .put("param12", "r12")
        .put("param21", 88)
        .build()));
    when(parameterPersistence.getData(resolver, "/region1/site1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "s11")
        .put("param21", 99)
        .build()));
    when(parameterPersistence.getData(resolver, "/region1/site1/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "c11")
        .build()));

    when(parameterOverride.getOverrideSystemDefault(PARAM13)).thenReturn("override13");
    when(parameterOverride.getOverrideForce("/region1/site1", PARAM21)).thenReturn(66);

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of(
        "/region1/site1/config1", "/region1/site1", "/region1"));
    assertEquals("c11", values.get("param11"));
    assertEquals("r12", values.get("param12"));
    assertEquals("override13", values.get("param13"));
    assertEquals(66, values.get("param21"));
  }

  @Test
  public void testOsgiTypes() {
    underTest.unbindParameterProvider(parameterProvider1, SERVICE_PROPS_1);
    when(parameterProvider1.getParameters()).thenReturn(ImmutableSet.<Parameter<?>>builder()
        .add(create("stringParam", String.class, APP_ID_1).defaultOsgiConfigProperty("my.service:stringParam").build())
        .add(create("stringParamUnset", String.class, APP_ID_1).defaultOsgiConfigProperty("my.service:stringParamUnset").build())
        .add(create("stringArrayParam", String[].class, APP_ID_1).defaultOsgiConfigProperty("my.service:stringArrayParam").build())
        .add(create("integerParam", Integer.class, APP_ID_1).defaultOsgiConfigProperty("my.service:integerParam").build())
        .build());
    underTest.bindParameterProvider(parameterProvider1, SERVICE_PROPS_1);

    when(serviceReference.getProperty("stringParam")).thenReturn("stringValue");
    when(serviceReference.getProperty("stringArrayParam")).thenReturn(new String[] {
        "v1", "v2"
    });
    when(serviceReference.getProperty("integerParam")).thenReturn("25");

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertEquals("stringValue", values.get("stringParam"));
    assertNull(values.get("stringParamUnset"));
    assertArrayEquals(new String[] {
        "v1", "v2"
    }, (String[])values.get("stringArrayParam"));
    assertEquals(25, values.get("integerParam"));
  }

  @Test
  public void testConfiguredValuesInvalidTypes() {
    underTest.unbindParameterProvider(parameterProvider1, SERVICE_PROPS_1);
    when(parameterProvider1.getParameters()).thenReturn(ImmutableSet.<Parameter<?>>builder()
        .add(create("stringParam", String.class, APP_ID_1).build())
        .add(create("stringParam2", String.class, APP_ID_1).build())
        .add(create("stringParamDefaultValue", String.class, APP_ID_1).defaultValue("def").build())
        .add(create("stringArrayParam", String[].class, APP_ID_1).build())
        .add(create("integerParam", Integer.class, APP_ID_1).build())
        .add(create("integerParamDefaultValue", Integer.class, APP_ID_1).defaultValue(22).build())
        .build());
    underTest.bindParameterProvider(parameterProvider1, SERVICE_PROPS_1);

    when(parameterPersistence.getData(resolver, "/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("stringParam", 55)
        .put("stringParam2", "thisIsReallyAString")
        .put("stringParamDefaultValue", 66L)
        .put("stringArrayParam", new int[] {
            1, 2, 3
        })
        .put("integerParam", "value1")
        .put("integerParamDefaultValue", "value2")
        .build()));

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertNull(values.get("stringParam"));
    assertEquals("def", values.get("stringParamDefaultValue"));
    assertEquals("thisIsReallyAString", values.get("stringParam2"));
    assertNull(values.get("stringArrayParam"));
    assertNull(values.get("integerParam"));
    assertEquals(22, values.get("integerParamDefaultValue"));
  }


  @Test
  public void testConfigurationHierarchyWithLockedParameterNames() {
    when(parameterPersistence.getData(resolver, "/region1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "r11")
        .put("param12", "r12")
        .put("param21", 88)
        .build(), ImmutableSortedSet.of("param11")));
    when(parameterPersistence.getData(resolver, "/region1/site1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "s11")
        .put("param21", 99)
        .build(), ImmutableSortedSet.of("param21")));
    when(parameterPersistence.getData(resolver, "/region1/site1/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "c11")
        .put("param21", 111)
        .build()));

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of(
        "/region1/site1/config1", "/region1/site1", "/region1"));
    assertEquals("r11", values.get("param11"));
    assertEquals("r12", values.get("param12"));
    assertEquals("valueFromOsgiConfig", values.get("param13"));
    assertEquals(99, values.get("param21"));
  }

  @Test
  public void testConfigurationHierarchyWithOverridesAndLockedParameterNames() {
    when(parameterPersistence.getData(resolver, "/region1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "r11")
        .put("param12", "r12")
        .put("param21", 88)
        .build(), ImmutableSortedSet.of("param11")));
    when(parameterPersistence.getData(resolver, "/region1/site1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "s11")
        .put("param21", 99)
        .build(), ImmutableSortedSet.of("param21")));
    when(parameterPersistence.getData(resolver, "/region1/site1/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "c11")
        .put("param21", 111)
        .build()));

    when(parameterOverride.getOverrideSystemDefault(PARAM13)).thenReturn("override13");
    when(parameterOverride.getOverrideForce("/region1/site1", PARAM21)).thenReturn(66);

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of(
        "/region1/site1/config1", "/region1/site1", "/region1"));
    assertEquals("r11", values.get("param11"));
    assertEquals("r12", values.get("param12"));
    assertEquals("override13", values.get("param13"));
    assertEquals(66, values.get("param21"));
  }

  @Test
  public void testConfigurationHierarchyWithOverridesAndLockedParameterNamesFromOverride() {
    when(parameterPersistence.getData(resolver, "/region1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "r11")
        .put("param12", "r12")
        .put("param21", 88)
        .build(), ImmutableSortedSet.<String>of()));
    when(parameterPersistence.getData(resolver, "/region1/site1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "s11")
        .put("param21", 99)
        .build(), ImmutableSortedSet.<String>of()));
    when(parameterPersistence.getData(resolver, "/region1/site1/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "c11")
        .put("param21", 111)
        .build()));

    when(parameterOverride.getOverrideSystemDefault(PARAM13)).thenReturn("override13");
    when(parameterOverride.getOverrideForce("/region1/site1", PARAM21)).thenReturn(66);
    when(parameterOverride.getLockedParameterNames("/region1/site1")).thenReturn(ImmutableSet.of("param21"));

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of(
        "/region1/site1/config1", "/region1/site1", "/region1"));
    assertEquals("c11", values.get("param11"));
    assertEquals("r12", values.get("param12"));
    assertEquals("override13", values.get("param13"));
    assertEquals(66, values.get("param21"));
  }

  @Test
  public void testMapValue() {
    when(parameterPersistence.getData(resolver, "/config1")).thenReturn(toData(ImmutableValueMap.builder()
        .put("param11", "value1")
        .put("paramMap", PersistenceTypeConversionTest.SAMPLE_MAP_PERSISTENCE)
        .build()));

    Map<String, Object> values = underTest.getEffectiveValues(resolver, ImmutableList.of("/config1"));
    assertEquals("value1", values.get("param11"));
    assertEquals(PersistenceTypeConversionTest.SAMPLE_MAP, values.get("paramMap"));
  }


  private static ParameterPersistenceData toData(Map<String, Object> values) {
    return toData(values, ImmutableSortedSet.<String>of());
  }

  private static ParameterPersistenceData toData(Map<String, Object> values, SortedSet<String> lockedParameterNames) {
    return new ParameterPersistenceData(values, lockedParameterNames);
  }

}
