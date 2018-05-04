## wcm.io Context-Aware Configuration

Context-Aware configuration for AEM applications, based on [Apache Sling Context-Aware Configuration][sling-caconfig].

Context-Aware means configurations that are related to a content resource or a resource tree, e.g. a web site or a tenant site. wcm.io Context-Aware Configuration provides a set of AEM-specific extensions to the Apache Sling implementation. Additionally a compatibility layer for [wcm.io Configuration 0.x][config-deprecated] is provided.


### Overview

* [Editor](editor/): Configuration Editor Template for AEM.
* [Extensions](extensions/): AEM-specific extensions for Sling Context-Aware Configuration.
* [Compatibility Layer](compat/): Compatibility Layer for wcm.io Configuration 0.x.


### Supported AEM versions

Context-Aware Configuration is supported in AEM 6.1 and upwards. AEM 6.0 is not supported.

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM. In AEM 6.3 you have to update the Apache Sling Context-Aware Configuration SPI and Impl bundles to the latest version to use all features.

See [Deploy and configure Context-Aware Configuration in AEM][deploy-configure-caconfig-in-aem] for details.


### GitHub Repository

Sources: https://github.com/wcm-io/wcm-io-caconfig


### Further Resources

* [wcm.io AEM Context-Aware Configuration training material](http://training.wcm.io/caconfig/)
* [adaptTo() 2016 Talk: Sling Context-Aware Configuration](https://adapt.to/2016/en/schedule/sling-context-aware-configuration.html)
* [adaptTo() 2017 Talk: Context-Aware Configuration in AEM](https://adapt.to/2017/en/schedule/context-aware-configuration-in-aem.html)


[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
[config-deprecated]: http://wcm.io/config/
[deploy-configure-caconfig-in-aem]: http://wcm.io/caconfig/deploy-configure-caconfig-in-aem.html
