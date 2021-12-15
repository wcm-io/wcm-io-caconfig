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
package io.wcm.caconfig.editor.impl;

import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.ConfigurationCollectionPersistData;
import org.apache.sling.caconfig.spi.ConfigurationPersistData;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.ImmutableList;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConfigPersistServletTest {

  private static final String CONFIG_NAME = "testConfig";
  private static final String CONFIG_COL_NAME = "testConfigCol";

  private final AemContext context = new AemContext();

  @Mock
  private ConfigurationManager configManager;

  private ConfigPersistServlet underTest;

  @BeforeEach
  void setUp() {
    context.registerService(ConfigurationManager.class, configManager);
    context.registerInjectActivateService(new EditorConfig());
    underTest = context.registerInjectActivateService(new ConfigPersistServlet());

    ConfigurationMetadata configMetadata = new ConfigurationMetadata(CONFIG_NAME, ImmutableList.<PropertyMetadata<?>>of(
        new PropertyMetadata<>("stringProp", "value1"),
        new PropertyMetadata<>("intProp", Integer.class),
        new PropertyMetadata<>("longProp", Long.class),
        new PropertyMetadata<>("doubleProp", Double.class),
        new PropertyMetadata<>("boolProp", Boolean.class),
        new PropertyMetadata<>("nestedConfig", ConfigurationMetadata.class)
        .configurationMetadata(new ConfigurationMetadata("nestedConfig", ImmutableList.<PropertyMetadata<?>>of(), false))),
        false);
    when(configManager.getConfigurationMetadata(CONFIG_NAME)).thenReturn(configMetadata);

    ConfigurationMetadata configColMetadata = new ConfigurationMetadata(CONFIG_COL_NAME, ImmutableList.<PropertyMetadata<?>>of(
        new PropertyMetadata<>("stringProp", String[].class),
        new PropertyMetadata<>("intProp", Integer[].class),
        new PropertyMetadata<>("longProp", Long[].class),
        new PropertyMetadata<>("doubleProp", Double[].class),
        new PropertyMetadata<>("boolProp", Boolean[].class),
        new PropertyMetadata<>("nestedConfig", ConfigurationMetadata[].class)
        .configurationMetadata(new ConfigurationMetadata("nestedConfig", ImmutableList.<PropertyMetadata<?>>of(), false))),
        true);
    when(configManager.getConfigurationMetadata(CONFIG_COL_NAME)).thenReturn(configColMetadata);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testPersist() throws Exception {
    String jsonData = "{properties:{"
        + "stringProp:'value1',"
        + "intProp:5,"
        + "longProp:10,"
        + "doubleProp:1.23,"
        + "boolProp:true,"
        + "nestedConfig:'invalid',"
        + "otherString:'otherValue1',"
        + "otherInt:20"
        + "}}";
    assertEquals(HttpServletResponse.SC_OK, post(CONFIG_NAME, false, jsonData));

    ArgumentCaptor<ConfigurationPersistData> persistData = ArgumentCaptor.forClass(ConfigurationPersistData.class);
    verify(configManager, times(1)).persistConfiguration(same(context.request().getResource()), eq(CONFIG_NAME), persistData.capture());

    assertThat(persistData.getValue().getProperties(), allOf(
        hasEntry("stringProp", (Object)"value1"),
        hasEntry("intProp", (Object)5),
        hasEntry("longProp", (Object)10L),
        hasEntry("doubleProp", (Object)1.23d),
        hasEntry("boolProp", (Object)true),
        hasEntry("otherString", (Object)"otherValue1"),
        hasEntry("otherInt", (Object)20),
        not(hasKey("nestedConfig"))));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testPersist_AllValuesString() throws Exception {
    String jsonData = "{properties:{"
        + "stringProp:'value1',"
        + "intProp:'5',"
        + "longProp:'10',"
        + "doubleProp:'1.23',"
        + "boolProp:'true',"
        + "nestedConfig:'invalid',"
        + "otherString:'otherValue1',"
        + "otherInt:'20'"
        + "}}";
    assertEquals(HttpServletResponse.SC_OK, post(CONFIG_NAME, false, jsonData));

    ArgumentCaptor<ConfigurationPersistData> persistData = ArgumentCaptor.forClass(ConfigurationPersistData.class);
    verify(configManager, times(1)).persistConfiguration(same(context.request().getResource()), eq(CONFIG_NAME), persistData.capture());

    assertThat(persistData.getValue().getProperties(), allOf(
        hasEntry("stringProp", (Object)"value1"),
        hasEntry("intProp", (Object)5),
        hasEntry("longProp", (Object)10L),
        hasEntry("doubleProp", (Object)1.23d),
        hasEntry("boolProp", (Object)true),
        hasEntry("otherString", (Object)"otherValue1"),
        hasEntry("otherInt", "20"),
        not(hasKey("nestedConfig"))));
  }

  @Test
  void testPersistCollection_None() throws Exception {
    String jsonData = "{items:[]}";
    assertEquals(HttpServletResponse.SC_OK, post(CONFIG_COL_NAME, true, jsonData));

    ArgumentCaptor<ConfigurationCollectionPersistData> persistData = ArgumentCaptor.forClass(ConfigurationCollectionPersistData.class);
    verify(configManager, times(1)).persistConfigurationCollection(same(context.request().getResource()), eq(CONFIG_COL_NAME), persistData.capture());

    assertEquals(0, persistData.getValue().getItems().size());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testPersistCollection_One() throws Exception {
    String jsonData = "{properties:{colProp:'value1'},items:[{collectionItemName:'item1',properties:{"
        + "stringProp:['value1','value2'],"
        + "intProp:[5],"
        + "longProp:[10,15],"
        + "doubleProp:[1.23],"
        + "boolProp:[true,false],"
        + "nestedConfig:['invalid'],"
        + "otherString:['otherValue1'],"
        + "otherInt:[5]"
        + "}}]}";
    assertEquals(HttpServletResponse.SC_OK, post(CONFIG_COL_NAME, true, jsonData));

    ArgumentCaptor<ConfigurationCollectionPersistData> persistData = ArgumentCaptor.forClass(ConfigurationCollectionPersistData.class);
    verify(configManager, times(1)).persistConfigurationCollection(same(context.request().getResource()), eq(CONFIG_COL_NAME), persistData.capture());

    assertEquals(1, persistData.getValue().getItems().size());
    ConfigurationPersistData item1 = persistData.getValue().getItems().iterator().next();
    assertEquals("item1", item1.getCollectionItemName());
    assertThat(item1.getProperties(), allOf(
        hasEntry("stringProp", (Object)new String[] {
            "value1", "value2"
        }),
        hasEntry("intProp", (Object)new int[] {
            5
        }),
        hasEntry("longProp", (Object)new long[] {
            10L, 15L
        }),
        hasEntry("doubleProp", (Object)new double[] {
            1.23d
        }),
        hasEntry("boolProp", (Object)new boolean[] {
            true, false
        }),
        hasEntry("otherString", (Object)new String[] {
            "otherValue1"
        }),
        hasEntry("otherInt", (Object)new int[] {
            5
        }),
        not(hasKey("nestedConfig"))));
    assertThat(persistData.getValue().getProperties(), hasEntry("colProp", (Object)"value1"));
  }

  @Test
  void testPersistCollection_Two() throws Exception {
    String jsonData = "{items:[{collectionItemName:'item1',properties:{stringProp:['value1','value2']}},"
        + "{collectionItemName:'item2',properties:{stringProp:['value3']}}]}";
    assertEquals(HttpServletResponse.SC_OK, post(CONFIG_COL_NAME, true, jsonData));

    ArgumentCaptor<ConfigurationCollectionPersistData> persistData = ArgumentCaptor.forClass(ConfigurationCollectionPersistData.class);
    verify(configManager, times(1)).persistConfigurationCollection(same(context.request().getResource()), eq(CONFIG_COL_NAME), persistData.capture());

    assertEquals(2, persistData.getValue().getItems().size());
    Iterator<ConfigurationPersistData> items = persistData.getValue().getItems().iterator();
    ConfigurationPersistData item1 = items.next();
    ConfigurationPersistData item2 = items.next();

    assertEquals("item1", item1.getCollectionItemName());
    assertThat(item1.getProperties(),
        hasEntry("stringProp", (Object)new String[] {
            "value1", "value2"
        }));

    assertEquals("item2", item2.getCollectionItemName());
    assertThat(item2.getProperties(),
        hasEntry("stringProp", (Object)new String[] {
            "value3"
        }));
  }

  @Test
  void testDelete() throws Exception {
    assertEquals(HttpServletResponse.SC_OK, delete(CONFIG_NAME));

    verify(configManager, times(1)).deleteConfiguration(same(context.request().getResource()), eq(CONFIG_NAME));
  }

  private int post(String configName, boolean collection, String jsonData) throws Exception {
    context.request().setQueryString(RP_CONFIGNAME + "=" + configName
        + (collection ? "&collection=true" : ""));
    context.request().setContentType("application/json");
    context.request().setContent(jsonData.getBytes(StandardCharsets.UTF_8));
    context.request().setMethod("POST");

    underTest.service(context.request(), context.response());
    return context.response().getStatus();
  }

  private int delete(String configName) throws Exception {
    context.request().setQueryString(RP_CONFIGNAME + "=" + configName);
    context.request().setMethod("DELETE");

    underTest.service(context.request(), context.response());
    return context.response().getStatus();
  }

}
