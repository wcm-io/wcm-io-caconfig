## About Context-Aware Configuration Extensions for AEM

AEM-specific extensions for Apache Sling Context-Aware Configuration.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.extensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.extensions)


### Documentation

* [Usage][usage]
* [Changelog][changelog]


### Overview

The following extensions are provided:

* Configure [Context Path Strategies][context-path-strategies]: without the need for `sling:configRef` attributes based on hierarchy levels or root templates
* AEM-specific [Persistence Strategies][persistence-strategies] to store configuration in `cq:Page` nodes either in `/conf` or in `tools/config` pages together with the content
* Configuration [Override Provider][override-providers] based on request headers (e.g. for QA instances - disabled by default)
* A [Reference Provider][reference-provider] implementation for context-aware configurations


### Supported AEM versions

Context-Aware Configuration is supported in AEM 6.1, 6.2, 6.3 and upwards. AEM 6.0 is not supported.

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM. In AEM 6.3 you have to update the Apache Sling Context-Aware Configuration SPI and Impl bundles to the latest version to use all features.

See [Deploy and configure Context-Aware Configuration in AEM][deploy-configure-caconfig-in-aem] for details.


### AEM Version Support Matrix

|Context-Aware Configuration Extensions for AEM version |AEM version supported
|-------------------------------------------------------|----------------------
|1.9.x or higher                                        |AEM 6.5+, AEMaaCS
|1.8.x                                                  |AEM 6.4+, AEMaaCS
|1.7.x                                                  |AEM 6.3+
|1.6.x                                                  |AEM 6.2+
|1.0.x - 1.5.x                                          |AEM 6.1+


[usage]: usage.html
[changelog]: changes-report.html
[deploy-configure-caconfig-in-aem]: https://wcm.io/caconfig/deploy-configure-caconfig-in-aem.html
[context-path-strategies]: context-path-strategies.html
[persistence-strategies]: persistence-strategies.html
[override-providers]: override-providers.html
[reference-provider]: reference-provider.html
