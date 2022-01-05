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
package io.wcm.caconfig.sample.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import io.wcm.caconfig.editor.DropdownOptionItem;
import io.wcm.caconfig.editor.DropdownOptionProvider;

/**
 * Sample {@link DropdownOptionProvider}.
 */
@Component(service = DropdownOptionProvider.class, property = {
    DropdownOptionProvider.PROPERTY_SELECTOR + "=" + SampleDropdownOptionProvider.SAMPLE_DROPDWON_OPTIONS_PROVIDER
})
public class SampleDropdownOptionProvider implements DropdownOptionProvider {

  /**
   * Selector value for this provider.
   */
  public static final String SAMPLE_DROPDWON_OPTIONS_PROVIDER = "sample";

  @Override
  @NotNull
  public List<DropdownOptionItem> getDropdownOptions(@NotNull Resource contextResource) {
    List<DropdownOptionItem> result = new ArrayList<>();
    result.add(new DropdownOptionItem("dynoption1", "Dynamic Option #1"));
    result.add(new DropdownOptionItem("dynoption2", "Dynamic Option #2"));
    result.add(new DropdownOptionItem("dynoption3", "Dynamic Option #3"));
    return result;
  }

}
