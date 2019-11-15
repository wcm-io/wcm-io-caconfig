package io.wcm.caconfig.extensions.bindings.impl;

import com.adobe.cq.sightly.WCMBindings;
import com.day.cq.wcm.api.Page;
import javax.script.Bindings;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.resource.spi.ConfigurationBindingsResourceDetectionStrategy;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 * AEM-specific configuration binding resource strategy that has higher precedence than the default strategy from Sling.
 * <p>
 * It uses the {@code WCMBindings.CURRENT_PAGE} binding instead of the default {@code SlingBindings.REQUEST} binding.
 * </p>
 */
@Component(
    service = ConfigurationBindingsResourceDetectionStrategy.class,
    property = {
        Constants.SERVICE_RANKING + "=200"
    }
)
public class CurrentPageConfigurationBindingsResourceDetectionStrategy implements ConfigurationBindingsResourceDetectionStrategy {

    @Override
    public Resource detectResource(Bindings bindings) {
        if (bindings.containsKey(WCMBindings.CURRENT_PAGE)) {
            Page currentPage = (Page) bindings.get(WCMBindings.CURRENT_PAGE);
            return currentPage.getContentResource();
        }
        return null;
    }

}