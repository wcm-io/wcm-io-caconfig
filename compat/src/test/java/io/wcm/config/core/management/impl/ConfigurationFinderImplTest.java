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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.config.api.Configuration;
import io.wcm.config.core.management.ParameterResolver;
import io.wcm.config.spi.ConfigurationFinderStrategy;
import io.wcm.sling.commons.resource.ImmutableValueMap;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationFinderImplTest {

  @Mock
  private ComponentContext componentContext;
  @Mock
  private Resource resource;
  @Mock
  private ParameterResolver parameterResolver;
  @Mock
  private ApplicationFinder applicationFinder;

  @Mock
  private ConfigurationFinderStrategy finderStrategy1;
  private static final Map<String, Object> SERVICE_PROPS_1 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 1L,
          Constants.SERVICE_RANKING, 10);
  private static final String APPLICATION_ID_1 = "app1";

  @Mock
  private ConfigurationFinderStrategy finderStrategy2;
  private static final Map<String, Object> SERVICE_PROPS_2 =
      ImmutableValueMap.of(Constants.SERVICE_ID, 2L,
          Constants.SERVICE_RANKING, 5);
  private static final String APPLICATION_ID_2 = "app2";

  @InjectMocks
  private ConfigurationFinderImpl underTest;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    Dictionary<String, Object> config = new Hashtable<>();
    config.put(ConfigurationFinderImpl.PROPERTY_EXCLUDE_PATH_PATTERNS, new String[] {
        "^.*/notaccepted$",
        "^.*/notaccepted/.*$"
    });
    when(componentContext.getProperties()).thenReturn(config);
    underTest.activate(componentContext);
    underTest.bindConfigurationFinderStrategy(finderStrategy1, SERVICE_PROPS_1);
    underTest.bindConfigurationFinderStrategy(finderStrategy2, SERVICE_PROPS_2);

    when(finderStrategy1.findConfigurationIds(resource)).thenReturn(ImmutableList.<String>builder()
        .add("/content/region1/region11/site")
        .build().iterator());
    when(finderStrategy1.getApplicationId()).thenReturn(APPLICATION_ID_1);

    when(finderStrategy2.findConfigurationIds(resource)).thenReturn(ImmutableList.<String>builder()
        .add("/content/region1/region11/site/language")
        .add("/content/region1")
        .build().iterator());
    when(finderStrategy2.getApplicationId()).thenReturn(APPLICATION_ID_2);

    when(parameterResolver.getEffectiveValues((ResourceResolver)any(), (Collection)anyCollection()))
    .then(new Answer<Map<String,Object>>() {
      @Override
      public Map<String, Object> answer(InvocationOnMock invocation) {
        Collection<String> configurationIds = (Collection<String>)invocation.getArguments()[1];
        Map<String, Object> props = new HashMap<>();
        props.put("path", configurationIds.iterator().next());
        return props;
      }
    });
  }

  @After
  public void tearDown() {
    underTest.unbindConfigurationFinderStrategy(finderStrategy1, SERVICE_PROPS_1);
    underTest.unbindConfigurationFinderStrategy(finderStrategy2, SERVICE_PROPS_2);
  }

  @Test
  public void testFindResource() {
    Configuration conf = underTest.find(resource);
    assertNotNull(conf);
    assertEquals("/content/region1/region11/site/language", conf.getConfigurationId());
    assertEquals("/content/region1/region11/site/language", conf.get("path", String.class));
  }

  @Test
  public void testFindResourceForApplication() {
    Configuration conf1 = underTest.find(resource, APPLICATION_ID_1);
    assertNotNull(conf1);
    assertEquals("/content/region1/region11/site", conf1.getConfigurationId());
    assertEquals("/content/region1/region11/site", conf1.get("path", String.class));

    Configuration conf2 = underTest.find(resource, APPLICATION_ID_2);
    assertNotNull(conf2);
    assertEquals("/content/region1/region11/site/language", conf2.getConfigurationId());
    assertEquals("/content/region1/region11/site/language", conf2.get("path", String.class));

    Configuration conf3 = underTest.find(resource, "invalidAppId");
    assertNull(conf3);
  }

  @Test
  public void testFindResourceDetectionByApplicationFinder() {
    when(applicationFinder.find(resource)).thenReturn(new ApplicationInfo(APPLICATION_ID_1, null));
    Configuration conf = underTest.find(resource);
    assertNotNull(conf);
    assertEquals("/content/region1/region11/site", conf.getConfigurationId());
    assertEquals("/content/region1/region11/site", conf.get("path", String.class));
  }

  @Test
  public void testFindAllResource() {
    Iterator<Configuration> confs = underTest.findAll(resource);
    List<Configuration> confList = Lists.newArrayList(confs);
    assertEquals(3, confList.size());
    assertEquals("/content/region1/region11/site/language", confList.get(0).getConfigurationId());
    assertEquals("/content/region1/region11/site", confList.get(1).getConfigurationId());
    assertEquals("/content/region1", confList.get(2).getConfigurationId());
  }

  @Test
  public void testFindAllResourceForApplication() {
    Iterator<Configuration> confs = underTest.findAll(resource, APPLICATION_ID_2);
    List<Configuration> confList = Lists.newArrayList(confs);
    assertEquals(2, confList.size());
    assertEquals("/content/region1/region11/site/language", confList.get(0).getConfigurationId());
    assertEquals("/content/region1", confList.get(1).getConfigurationId());
  }

  @Test
  public void testExcludedPathPattenrs() {
    when(finderStrategy1.findConfigurationIds(resource)).thenReturn(ImmutableList.<String>builder()
        .add("/content/region1/region11/site")
        .add("/content/region1/region11/notaccepted")
        .add("/content/notaccepted/region11/site")
        .build().iterator());
    when(finderStrategy2.findConfigurationIds(resource)).thenReturn(ImmutableList.<String>of().iterator());

    Iterator<Configuration> confs = underTest.findAll(resource);
    List<Configuration> confList = Lists.newArrayList(confs);
    assertEquals(1, confList.size());
    assertEquals("/content/region1/region11/site", confList.get(0).getConfigurationId());
  }

}
