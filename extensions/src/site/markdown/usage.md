## Configuration Extensions for AEM usage

### Installation

Deploy the bundle `io.wcm.caconfig.extensions` together with your application to the AEM instance.

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM. In AEM 6.3 you have to update the Apache Sling Context-Aware Configuration SPI and Impl version to the latest version if you want to use Editor version 1.1 and upwards. See [Deploy and configure Context-Aware Configuration in AEM][deploy-configure-caconfig-in-aem] for details.

You might consider also installing the [wcm.io Context-Aware Configuration Editor][wcmio-caconfig-editor].


### Configuration

See the following chapters:

* [Context Path Strategies][context-path-strategies]
* [Persistence Strategies][persistence-strategies]
* [Override Provider][override-providers]


[deploy-configure-caconfig-in-aem]: http://wcm.io/caconfig/deploy-configure-caconfig-in-aem.html
[wcmio-caconfig-editor]: http://wcm.io/caconfig/editor/
[context-path-strategies]: context-path-strategies.html
[persistence-strategies]: persistence-strategies.html
[override-providers]: override-providers.html
