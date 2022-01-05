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

import static io.wcm.caconfig.editor.impl.NameConstants.RP_COLLECTION;
import static io.wcm.caconfig.editor.impl.NameConstants.RP_CONFIGNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.management.ValueInfo;
import org.apache.sling.caconfig.management.multiplexer.ConfigurationPersistenceStrategyMultiplexer;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.wcm.caconfig.editor.DropdownOptionItem;
import io.wcm.caconfig.editor.DropdownOptionProvider;
import io.wcm.caconfig.editor.EditorProperties;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class ConfigDataServletTest {

  private final AemContext context = new AemContext();

  @Mock
  private ConfigurationManager configManager;
  @Mock
  private ConfigurationPersistenceStrategyMultiplexer configurationPersistenceStrategy;

  private ConfigDataServlet underTest;

  @BeforeEach
  void setUp() {
    when(configurationPersistenceStrategy.getCollectionParentConfigName(anyString(), nullable(String.class))).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {
        return (String)invocation.getArgument(0);
      }
    });
    when(configurationPersistenceStrategy.getConfigName(anyString(), nullable(String.class))).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) {
        return (String)invocation.getArgument(0);
      }
    });

    context.registerService(ConfigurationManager.class, configManager);
    context.registerService(ConfigurationPersistenceStrategyMultiplexer.class, configurationPersistenceStrategy);
    context.registerInjectActivateService(DropdownOptionProviderService.class);
    context.registerInjectActivateService(EditorConfig.class);
    underTest = context.registerInjectActivateService(ConfigDataServlet.class);
  }

  @Test
  void testNoConfigName() throws Exception {
    underTest.doGet(context.request(), context.response());
    assertEquals(HttpServletResponse.SC_NOT_FOUND, context.response().getStatus());
  }

  @Test
  void testSingle() throws Exception {
    ConfigurationData configData = buildConfigData("name1", 0);
    when(configManager.getConfiguration(context.currentResource(), "name1")).thenReturn(configData);

    context.request().setQueryString(RP_CONFIGNAME + "=" + configData.getConfigName());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = buildConfigDataJson("name1", false);
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testCollection() throws Exception {
    ConfigurationData configData1 = buildConfigData("name1", 1);
    ConfigurationData configData2 = buildConfigData("name1", 2);
    ConfigurationData configDataNew = buildConfigData("new", 0);
    ConfigurationCollectionData configCollectionData = mock(ConfigurationCollectionData.class);
    when(configCollectionData.getConfigName()).thenReturn("name1");
    when(configCollectionData.getItems()).thenReturn(ImmutableList.of(configData1, configData2));
    when(configCollectionData.getProperties()).thenReturn(ImmutableMap.<String, Object>of("colProp1", true));
    when(configManager.getConfigurationCollection(context.currentResource(), "name1")).thenReturn(configCollectionData);
    when(configManager.newCollectionItem(context.currentResource(), "name1")).thenReturn(configDataNew);

    context.request().setQueryString(RP_CONFIGNAME + "=name1&" + RP_COLLECTION + "=true");
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{configName:'name1',properties:{colProp1:true},items:["
        + buildConfigDataJson("name1", 1, false) + ","
        + buildConfigDataJson("name1", 2, false)
        + "],newItem:" + buildConfigDataJson("new", null) + "}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testNested() throws Exception {
    ConfigurationData configData = mock(ConfigurationData.class);
    when(configData.getConfigName()).thenReturn("nestedConfig");
    when(configData.getPropertyNames()).thenReturn(ImmutableSet.of("param1", "subConfig", "subConfigList"));

    ValueInfo param1 = mock(ValueInfo.class);
    when(param1.getName()).thenReturn("param1");
    when(configData.getValueInfo("param1")).thenReturn(param1);

    ValueInfo subConfig = mock(ValueInfo.class);
    when(subConfig.getName()).thenReturn("subConfig");
    ConfigurationData subConfigData = buildConfigData("nestedConfig/subConfig", 0);
    when(subConfig.getValue()).thenReturn(subConfigData);
    when(subConfig.getPropertyMetadata()).thenReturn(new PropertyMetadata<>("subConfig", ConfigurationMetadata.class)
        .label("subConfig-label")
        .description("subConfig-desc")
        .configurationMetadata(new ConfigurationMetadata("subConfig", ImmutableList.<PropertyMetadata<?>>of(), false)));
    when(configData.getValueInfo("subConfig")).thenReturn(subConfig);

    ValueInfo subConfigList = mock(ValueInfo.class);
    when(subConfigList.getName()).thenReturn("subConfigList");
    ConfigurationData[] subConfigListData = new ConfigurationData[] {
        buildConfigData("nestedConfig/subConfigList", 1),
        buildConfigData("nestedConfig/subConfigList", 2)
    };
    when(subConfigList.getValue()).thenReturn(subConfigListData);
    when(subConfigList.getPropertyMetadata()).thenReturn(new PropertyMetadata<>("subConfigList", ConfigurationMetadata[].class)
        .label("subConfigList-label")
        .description("subConfigList-desc")
        .configurationMetadata(new ConfigurationMetadata("subConfigList", ImmutableList.<PropertyMetadata<?>>of(), true)));
    when(configData.getValueInfo("subConfigList")).thenReturn(subConfigList);


    when(configManager.getConfiguration(context.currentResource(), "nestedConfig")).thenReturn(configData);

    context.request().setQueryString(RP_CONFIGNAME + "=" + configData.getConfigName());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{configName:'nestedConfig',overridden:false,inherited:false,properties:["
        + "{name:'param1',default:false,inherited:false,overridden:false},"
        + "{name:'subConfig',metadata:{label:'subConfig-label',description:'subConfig-desc'},nestedConfig:"
        + buildConfigDataJson("nestedConfig/subConfig", null)
        + "},"
        + "{name:'subConfigList',metadata:{label:'subConfigList-label',description:'subConfigList-desc'},nestedConfigCollection:{configName:'nestedConfig/subConfigList',items:["
        + buildConfigDataJson("nestedConfig/subConfigList", 1, false) + ","
        + buildConfigDataJson("nestedConfig/subConfigList", 2, false)
        + "]}}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testSingleWithDropdown() throws Exception {
    ConfigurationData configData = buildConfigDataWithDropdown("name1");
    when(configManager.getConfiguration(context.currentResource(), "name1")).thenReturn(configData);

    context.request().setQueryString(RP_CONFIGNAME + "=" + configData.getConfigName());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{configName:'name1',overridden:false,inherited:false,"
        + "properties:["
        + "{name:'param1',value:'option1',effectiveValue:'option1',default:false,inherited:true,overridden:false,"
        + "metadata:{type:'String',properties:{widgetType:'dropdown',dropdownOptions:["
        + "{'value':'option1','description':'First option'},"
        + "{'value':'option2','description':'Second option'},"
        + "{'value':'option3','description':'Third option'}"
        + "]}}},"
        + "{name:'param2',value:5,effectiveValue:5,default:false,inherited:true,overridden:false,"
        + "metadata:{type:'Integer',defaultValue:0,properties:{widgetType:'dropdown',dropdownOptions:["
        + "{'value':1,'description':'Number One'},"
        + "{'value':2,'description':'Number Two'}"
        + "]}}}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @Test
  void testSingleWithDropdownDynamic() throws Exception {
    ConfigurationData configData = buildConfigDataWithDropdownDynamic("name1");
    when(configManager.getConfiguration(context.currentResource(), "name1")).thenReturn(configData);

    context.request().setQueryString(RP_CONFIGNAME + "=" + configData.getConfigName());
    underTest.doGet(context.request(), context.response());

    assertEquals(HttpServletResponse.SC_OK, context.response().getStatus());

    String expectedJson = "{configName:'name1',overridden:false,inherited:false,"
        + "properties:["
        + "{name:'param1',value:'option1',effectiveValue:'option1',default:false,inherited:true,overridden:false,"
        + "metadata:{type:'String',properties:{widgetType:'dropdown',dropdownOptions:["
        + "{'value':'option1','description':'First option'},"
        + "{'value':'option2','description':'Second option'},"
        + "{'value':'option3','description':'Third option'}"
        + "]}}}"
        + "]}";
    JSONAssert.assertEquals(expectedJson, context.response().getOutputAsString(), true);
  }

  @SuppressWarnings("unchecked")
  private ConfigurationData buildConfigData(String configName, int index) {
    ConfigurationData configData = mock(ConfigurationData.class);
    when(configData.getConfigName()).thenReturn(configName);
    if (index > 0) {
      when(configData.getCollectionItemName()).thenReturn("item" + index);
    }
    when(configData.getPropertyNames()).thenReturn(ImmutableSet.of("param1", "param2", "param3"));

    ValueInfo param1 = buildValueInfo("param1", new String[] {
        "v1"
    }, new String[] {
        "v1", "v2"
    }, new String[0]);
    when(configData.getValueInfo("param1")).thenReturn(param1);

    ValueInfo param2 = buildValueInfo("param2", 5, 55, 555);
    when(configData.getValueInfo("param2")).thenReturn(param2);

    ValueInfo param3 = buildValueInfo("param3", true, false, null);
    when(configData.getValueInfo("param3")).thenReturn(param3);

    return configData;
  }

  @SuppressWarnings("unchecked")
  private ConfigurationData buildConfigDataWithDropdown(String configName) {
    ConfigurationData configData = mock(ConfigurationData.class);
    when(configData.getConfigName()).thenReturn(configName);
    when(configData.getPropertyNames()).thenReturn(ImmutableSet.of("param1", "param2"));

    ValueInfo param1 = buildValueInfo("param1", "option1", "option1", null);
    when(param1.getPropertyMetadata()).thenReturn(
        new PropertyMetadata<>("param1", String.class)
            .properties(ImmutableMap.of(
                EditorProperties.PROPERTY_WIDGET_TYPE, EditorProperties.WIDGET_TYPE_DROPDOWN,
                EditorProperties.PROPERTY_DROPDOWN_OPTIONS, "["
                    + "{'value':'option1','description':'First option'},"
                    + "{'value':'option2','description':'Second option'},"
                    + "{'value':'option3','description':'Third option'}"
                    + "]")));
    when(configData.getValueInfo("param1")).thenReturn(param1);

    ValueInfo param2 = buildValueInfo("param2", 5, 5, 0);
    when(param2.getPropertyMetadata()).thenReturn(
        new PropertyMetadata<>("param2", 0)
            .properties(ImmutableMap.of(
                EditorProperties.PROPERTY_WIDGET_TYPE, EditorProperties.WIDGET_TYPE_DROPDOWN,
                EditorProperties.PROPERTY_DROPDOWN_OPTIONS, "["
                    + "{'value':1,'description':'Number One'},"
                    + "{'value':2,'description':'Number Two'}"
                    + "]")));
    when(configData.getValueInfo("param2")).thenReturn(param2);

    return configData;
  }

  @SuppressWarnings("unchecked")
  private ConfigurationData buildConfigDataWithDropdownDynamic(String configName) {
    ConfigurationData configData = mock(ConfigurationData.class);
    when(configData.getConfigName()).thenReturn(configName);
    when(configData.getPropertyNames()).thenReturn(ImmutableSet.of("param1"));

    ValueInfo param1 = buildValueInfo("param1", "option1", "option1", null);
    when(param1.getPropertyMetadata()).thenReturn(
        new PropertyMetadata<>("param1", String.class)
            .properties(ImmutableMap.of(
                EditorProperties.PROPERTY_WIDGET_TYPE, EditorProperties.WIDGET_TYPE_DROPDOWN,
                EditorProperties.PROPERTY_DROPDOWN_OPTIONS_PROVIDER, "provider1")));
    when(configData.getValueInfo("param1")).thenReturn(param1);

    DropdownOptionProvider provider = mock(DropdownOptionProvider.class);
    context.registerService(DropdownOptionProvider.class, provider,
        DropdownOptionProvider.PROPERTY_SELECTOR, "provider1");

    when(provider.getDropdownOptions(context.currentResource())).thenReturn(ImmutableList.of(
        new DropdownOptionItem("option1", "First option"),
        new DropdownOptionItem("option2", "Second option"),
        new DropdownOptionItem("option3", "Third option")));

    return configData;
  }

  @SuppressWarnings("unchecked")
  private <T> ValueInfo<T> buildValueInfo(String name, T value, T effectiveValue, T defaultValue) {
    ValueInfo valueInfo = mock(ValueInfo.class);
    when(valueInfo.getName()).thenReturn(name);
    when(valueInfo.getValue()).thenReturn(value);
    when(valueInfo.getEffectiveValue()).thenReturn(effectiveValue);
    when(valueInfo.isDefault()).thenReturn(false);
    when(valueInfo.isInherited()).thenReturn(true);
    when(valueInfo.isOverridden()).thenReturn(false);
    if (defaultValue != null) {
      when(valueInfo.getPropertyMetadata()).thenReturn(new PropertyMetadata<>(name, defaultValue)
          .label(name + "-label")
          .description(name + "-desc")
          .properties(ImmutableMap.of("custom", name + "-custom")));
    }
    return valueInfo;
  }

  private String buildConfigDataJson(String configName, Boolean inherited) {
    return buildConfigDataJson(configName, 0, inherited);
  }

  private String buildConfigDataJson(String configName, int index, Boolean inherited) {
    return "{configName:'" + configName + "',overridden:false,"
        + (index > 0 ? "collectionItemName:'item" + index + "'," : "")
        + (inherited != null ? "inherited:" + inherited.booleanValue() + "," : "")
        + "properties:["
        + "{name:'param1',value:['v1'],effectiveValue:['v1','v2'],default:false,inherited:true,overridden:false,"
        + "metadata:{type:'String',multivalue:true,defaultValue:[],label='param1-label',description:'param1-desc',properties:{custom:'param1-custom'}}},"
        + "{name:'param2',value:5,effectiveValue:55,default:false,inherited:true,overridden:false,"
        + "metadata:{type:'Integer',defaultValue:555,label='param2-label',description:'param2-desc',properties:{custom:'param2-custom'}}},"
        + "{name:'param3',value:true,effectiveValue:false,default:false,inherited:true,overridden:false}"
        + "]}";
  }

}
