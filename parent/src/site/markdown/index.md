## wcm.io Context-Aware Configuration

Context-Aware configuration for AEM applications, based on [Apache Sling Context-Aware Configuration][sling-caconfig].

Context-Aware means configurations that are related to a content resource or a resource tree, e.g. a web site or a tenant site. wcm.io Context-Aware Configuration provides a set of AEM-specific extensions to the Apache Sling implementation. Additionally a compatibility layer for [wcm.io Configuration 0.x][config-deprecated] is provided.


### Overview

* [Editor](editor/): Configuration Editor Template for AEM.
* [Application](application/): Context-Aware system configuration of shared bundles.
* [Extensions](extensions/): AEM-specific extensions for Sling Context-Aware Configuration.
* [Compatibility Layer](compat/): Compatibility Layer for wcm.io Configuration 0.x.


### Supported AEM versions

Context-Aware Configuration is supported in AEM 6.1, 6.2, 6.3 and upwards. AEM 6.0 is not supported.

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM. In AEM 6.3 you have to update the Apache Sling Context-Aware Configuration SPI and Impl bundles to the latest version to use all features.

Links to the latest Apache Sling Context-Aware Configuration bundles:

|---|---|---|
| Apache Sling Context-Aware Configuration API | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api) |
| Apache Sling Context-Aware Configuration SPI | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi) |
| Apache Sling Context-Aware Configuration Implementation | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl) |


### GitHub Repository

Sources: https://github.com/wcm-io/wcm-io-caconfig



[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
[config-deprecated]: http://wcm.io/config/
