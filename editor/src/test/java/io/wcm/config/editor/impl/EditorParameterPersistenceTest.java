/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.config.editor.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.wcm.config.api.Configuration;
import io.wcm.config.api.Parameter;
import io.wcm.config.api.ParameterBuilder;
import io.wcm.config.core.management.ConfigurationFinder;
import io.wcm.config.core.management.ParameterPersistence;
import io.wcm.config.core.management.ParameterPersistenceData;
import io.wcm.config.core.management.ParameterResolver;
import io.wcm.config.editor.WidgetTypes;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import io.wcm.wcm.commons.util.RunMode;

/**
 * Tests for the {@link EditorParameterPersistence}
 */
@RunWith(MockitoJUnitRunner.class)
public class EditorParameterPersistenceTest {

  @Mock
  private ParameterResolver parameterResolver;
  private static final String APP_ID = "/app/test";
  private static final Parameter<String> NON_EDITABLE_PARAMETER = ParameterBuilder.create("some-param", String.class, APP_ID)
      .defaultValue("defaultValue").build();
  private static final Parameter<Map> PARAMETER_MAP = ParameterBuilder.create("map-param", Map.class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Parameter<String[]> PARAMETER_MULTIVALUE = ParameterBuilder.create("multivalue-param", String[].class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Parameter<String> PARAMETER_STRING = ParameterBuilder.create("string-param", String.class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Parameter<Boolean> PARAMETER_BOOLEAN = ParameterBuilder.create("boolean-param", Boolean.class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Parameter<Integer> PARAMETER_INTEGER = ParameterBuilder.create("integer-param", Integer.class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Parameter<Long> PARAMETER_LONG = ParameterBuilder.create("long-param", Long.class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Parameter<Double> PARAMETER_DOUBLE = ParameterBuilder.create("double-param", Double.class, APP_ID)
      .properties(WidgetTypes.TEXTAREA.getDefaultWidgetConfiguration()).build();
  private static final Set<Parameter<?>> PARAMETERS = ImmutableSet.<Parameter<?>>of(PARAMETER_BOOLEAN, PARAMETER_DOUBLE, PARAMETER_INTEGER, PARAMETER_LONG,
      PARAMETER_MAP, PARAMETER_MULTIVALUE, PARAMETER_STRING, NON_EDITABLE_PARAMETER);

  private static final Map<String, Object> REQUEST_PARAMETERS = ImmutableValueMap.builder()
      .put("map-param" + EditorParameterPersistence.MAP_KEY_SUFFIX, new String[] {
          "key1", "key2"
      })
      .put("map-param", new String[] {
          "value1", "value2"
      })
      .put("multivalue-param", new String[] {
          "value1", "value2"
      })
      .put("string-param", "value")
      .put("integer-param", "1")
      .put("long-param", "5")
      .put("double-param", "3.454")
      .put("boolean-param", "true")
      .put(ParameterPersistence.PN_LOCKED_PARAMETER_NAMES, new String[]{"string-param", "boolean-param"})
      .build();

  @Mock
  private ConfigurationFinder configurationFinder;
  @Mock
  private Configuration configuration;

  @Mock
  private ResourceResolver resourceResolver;
  @Mock
  private ParameterPersistence persistence;
  @Mock
  private SlingHttpServletRequest request;
  @Mock
  private Resource resource;

  @Rule
  public AemContext context = new AemContext();

  @Before
  public void setUp() {
    context.runMode(RunMode.AUTHOR);
    context.request().setParameterMap(REQUEST_PARAMETERS);
    Page page = context.create().page("/content/test/tools/config");
    context.currentPage(page);
    context.request().setMethod(HttpConstants.METHOD_POST);
    context.registerService(ConfigurationFinder.class, configurationFinder);
    context.registerService(ParameterResolver.class, parameterResolver);
    context.registerService(ParameterPersistence.class, persistence);
    when(parameterResolver.getAllParameters()).thenReturn(PARAMETERS);
    when(configurationFinder.find(Matchers.any(Resource.class))).thenReturn(configuration);
    when(configuration.getConfigurationId()).thenReturn("/content/test");
  }

  @Test
  public void testResponseNoConfigurationFound() throws Exception {
    when(configurationFinder.find(Matchers.any(Resource.class))).thenReturn(null);

    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, context.response().getStatus());
  }

  @Test
  public void testResponseConfigurationFound() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    underTest.service(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMapValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Map<String, Object> mapParam = (Map<String, Object>)argument.getValue().getValues().get("map-param");
    assertEquals("value1", mapParam.get("key1"));
    assertEquals("value2", mapParam.get("key2"));
  }

  @Test
  public void testMultiValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    String[] multiParam = (String[])argument.getValue().getValues().get("multivalue-param");
    assertEquals("value1", multiParam[0]);
    assertEquals("value2", multiParam[1]);
  }

  @Test
  public void testBooleanValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Boolean value = (Boolean)argument.getValue().getValues().get("boolean-param");
    assertEquals(true, value);
  }

  @Test
  public void testIntegerValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Integer value = (Integer)argument.getValue().getValues().get("integer-param");
    assertEquals(Integer.valueOf(1), value);
  }

  @Test
  public void testLongValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Long value = (Long)argument.getValue().getValues().get("long-param");
    assertEquals(Long.valueOf(5L), value);
  }

  @Test
  public void testDoubleValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Double value = (Double)argument.getValue().getValues().get("double-param");
    assertEquals(Double.valueOf(3.454), value);
  }

  @Test
  public void testLockedParameterValue() throws Exception {
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Set<String> value = argument.getValue().getLockedParameterNames();
    assertEquals(2, value.size());
    Iterator<String> iterator = value.iterator();
    assertEquals("boolean-param", iterator.next());
    assertEquals("string-param", iterator.next());
  }

  @Test
  public void testLockedParameterValueEmpty() throws Exception {
    context.request().setParameterMap(ImmutableMap.<String, Object>of());
    EditorParameterPersistence underTest = new EditorParameterPersistence();
    context.registerInjectActivateService(underTest);
    ArgumentCaptor<ParameterPersistenceData> argument = ArgumentCaptor.forClass(ParameterPersistenceData.class);
    underTest.service(context.request(), context.response());
    verify(persistence).storeData(any(ResourceResolver.class), eq("/content/test"), argument.capture(), eq(false));

    Set<String> value = argument.getValue().getLockedParameterNames();
    assertEquals(0, value.size());
  }

}
