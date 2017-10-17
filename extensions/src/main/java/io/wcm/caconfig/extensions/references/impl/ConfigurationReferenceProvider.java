package io.wcm.caconfig.extensions.references.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageManagerFactory;
import com.day.cq.wcm.api.reference.Reference;
import com.day.cq.wcm.api.reference.ReferenceProvider;

/**
 * <p>
 * This implementation of {@link ReferenceProvider} allows to resolve references of a given {@link Resource} to context-aware
 * configurations.
 * </p>
 * <p>
 * This is for example used by ActivationReferenceSearchServlet to resolve referenced content of pages during activation of a page using
 * AEM sites. Returning the configurations allows the editor to activate them along with the page referring to them.
 * </p>
 * <p>
 * This component can be disabled by configuration, but its enabled by default.
 * </p>
 */
@Component(service = ReferenceProvider.class)
@Designate(ocd = ConfigurationReferenceProvider.Config.class)
public class ConfigurationReferenceProvider implements ReferenceProvider {

    @ObjectClassDefinition(name = "wcm.io Context-Aware Configuration Reference Provider",
            description = "Allows to resolve references from resources to their Context-Aware configurations, for example during page activation.")
    static @interface Config {

        @AttributeDefinition(name = "Enabled",
                description = "Enable this reference provider.")
        boolean enabled() default true;
    }

    @org.osgi.service.component.annotations.Reference
    private ConfigurationManager configurationManager;

    @org.osgi.service.component.annotations.Reference
    private PageManagerFactory pageManagerFactory;

    private boolean enabled = false;

    @Activate
    protected void activate(Config config) {
        enabled = config.enabled();
    }

    @Deactivate
    protected void deactivate() {
        enabled = false;
    }

    @Override
    public List<Reference> findReferences(Resource resource) {
        if (!enabled) {
            return Collections.emptyList();
        }

        Set<String> configurationNames = configurationManager.getConfigurationNames();
        List<Reference> references = new ArrayList<>(configurationNames.size());
        ResourceResolver resourceResolver = resource.getResourceResolver();

        for(String configurationName : configurationNames) {
            ConfigurationData configurationData = configurationManager.getConfiguration(resource, configurationName);
            ConfigurationMetadata configurationMetadata = configurationManager.getConfigurationMetadata(configurationName);
            Resource configurationResource = resourceResolver.getResource(configurationData.getResourcePath());

            if (configurationResource != null) {
                references.add(new Reference(getType(), StringUtils.defaultIfEmpty(configurationMetadata.getLabel(),
                        configurationMetadata.getName()), configurationResource, getLastModifiedOf(configurationResource)));
            }
        }

        return references;
    }

    private long getLastModifiedOf(Resource configurationResource) {
        Page configurationPage = configurationResource.adaptTo(Page.class);

        if (configurationPage == null && StringUtils.equals(configurationResource.getName(), JcrConstants.JCR_CONTENT)) {
            configurationPage = configurationResource.getParent().adaptTo(Page.class);
        }

        if (configurationPage != null && configurationPage.getLastModified() != null) {
            return configurationPage.getLastModified().getTimeInMillis();
        } else {
            ValueMap properties = configurationResource.getValueMap();
            return properties.get(JcrConstants.JCR_LASTMODIFIED, Calendar.class).getTimeInMillis();
        }
    }

    private static String getType() {
        return "caconfig";
    }
}
