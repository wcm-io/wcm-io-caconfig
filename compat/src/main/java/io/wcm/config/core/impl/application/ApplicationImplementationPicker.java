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
package io.wcm.config.core.impl.application;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.ImplementationPicker;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.caconfig.application.ApplicationFinder;
import io.wcm.caconfig.application.ApplicationInfo;
import io.wcm.config.core.impl.util.AdaptableUtil;
import io.wcm.config.spi.annotations.Application;

/**
 * Same as {@link io.wcm.caconfig.application.impl.ApplicationImplementationPicker},
 * but with support for the deprecated Application annotation.
 */
@Component(immediate = true, service = ImplementationPicker.class, property = {
    Constants.SERVICE_RANKING + "=1001"
})
@SuppressWarnings("deprecation")
public class ApplicationImplementationPicker implements ImplementationPicker {

  @Reference
  private ApplicationFinder applicationFinder;

  @Override
  public Class<?> pick(Class<?> adapterType, Class<?>[] implementationsTypes, Object adaptable) {
    Class<?> classMatchingApplication = pickMatchingApplication(implementationsTypes, adaptable);
    if (classMatchingApplication != null) {
      return classMatchingApplication;
    }
    return pickFirstWithoutApplication(implementationsTypes);
  }

  private Class<?> pickMatchingApplication(Class<?>[] implementationsTypes, Object adaptable) {
    String applicationId = getApplicationId(adaptable);
    if (applicationId != null) {
      for (Class<?> clazz : implementationsTypes) {
        Application applicationAnnotation = clazz.getAnnotation(Application.class);
        if (applicationAnnotation != null
            && StringUtils.equals(applicationId, applicationAnnotation.value())) {
          return clazz;
        }
      }
    }
    return null;
  }

  private String getApplicationId(Object adaptable) {
    Resource resource = AdaptableUtil.getResource(adaptable);
    if (resource != null) {
      ApplicationInfo application = applicationFinder.find(resource);
      if (application != null) {
        return application.getApplicationId();
      }
    }
    return null;
  }

  private Class<?> pickFirstWithoutApplication(Class<?>[] implementationsTypes) {
    for (Class<?> clazz : implementationsTypes) {
      if (!clazz.isAnnotationPresent(io.wcm.caconfig.application.spi.annotations.Application.class)) {
        return clazz;
      }
    }
    return null;
  }

}
