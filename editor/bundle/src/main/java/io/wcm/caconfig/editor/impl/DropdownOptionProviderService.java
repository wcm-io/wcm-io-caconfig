/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

import static io.wcm.caconfig.editor.impl.JsonMapper.OBJECT_MAPPER;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.wcm.caconfig.editor.DropdownOptionItem;
import io.wcm.caconfig.editor.DropdownOptionProvider;

/**
 * Get dynamic dropdown options.
 */
@Component(service = DropdownOptionProviderService.class)
public class DropdownOptionProviderService {

  private BundleContext bundleContext;

  @Activate
  private void activated(BundleContext bc) {
    this.bundleContext = bc;
  }

  /**
   * Gets dropdown items from service implementations.
   * @param contextResource Context resource
   * @return Dropdown items as as Maps
   */
  @SuppressWarnings("null")
  public @NotNull List<Map<String, Object>> getDropdownOptions(@NotNull String selector, @NotNull Resource contextResource) {
    final String filter = "(" + DropdownOptionProvider.PROPERTY_SELECTOR + "=" + selector + ")";
    try {
      ServiceReference<DropdownOptionProvider> ref = bundleContext.getServiceReferences(DropdownOptionProvider.class, filter)
          .stream().findFirst().orElse(null);
      if (ref != null) {
        DropdownOptionProvider provider = bundleContext.getService(ref);
        try {
          return toMapList(provider.getDropdownOptions(contextResource));
        }
        finally {
          bundleContext.ungetService(ref);
        }
      }
    }
    catch (InvalidSyntaxException ex) {
      throw new RuntimeException("Invalid filter syntax: " + filter, ex);
    }
    return Collections.emptyList();
  }

  @SuppressWarnings({ "null", "unchecked" })
  private @NotNull List<Map<String, Object>> toMapList(@NotNull List<DropdownOptionItem> items) {
    return items.stream()
        .map(item -> {
          Map<String, Object> map = OBJECT_MAPPER.convertValue(item, Map.class);
          return map;
        })
        .collect(Collectors.toList());
  }

}
