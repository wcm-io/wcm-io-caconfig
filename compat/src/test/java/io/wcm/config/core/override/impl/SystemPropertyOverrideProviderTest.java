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
package io.wcm.config.core.override.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class SystemPropertyOverrideProviderTest {

  private final AemContext context = new AemContext();

  @BeforeEach
  void setUp() {
    System.setProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[default]param1", "value1");
    System.setProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[/config1]param2", "value2");
  }

  @AfterEach
  void tearDown() {
    System.clearProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[default]param1");
    System.clearProperty(SystemPropertyOverrideProvider.SYSTEM_PROPERTY_PREFIX + "[/config1]param2");
  }

  @Test
  void testEnabled() {
    SystemPropertyOverrideProvider provider = context.registerInjectActivateService(new SystemPropertyOverrideProvider(),
        "enabled", true);

    Map<String,String> overrideMap = provider.getOverrideMap();
    assertEquals("value1", overrideMap.get("[default]param1"));
    assertEquals("value2", overrideMap.get("[/config1]param2"));
  }

  @Test
  void testDisabled() {
    SystemPropertyOverrideProvider provider = context.registerInjectActivateService(new SystemPropertyOverrideProvider(),
        "enabled", false);

    Map<String, String> overrideMap = provider.getOverrideMap();
    assertTrue(overrideMap.isEmpty());
  }

}
