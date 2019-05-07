## About Configuration Compatibility Layer

Context-aware configuration compatibility Layer for [wcm.io Configuration 0.x][config-deprecated].

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.compat/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.compat)


### Documentation

* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

This bundle is a drop-in replacement for [wcm.io Configuration 0.x][config-deprecated] and makes it easy to migrate existing applications or 3rdparty libraries based on it to [Apache Sling Context-Aware Configuration][sling-caconfig] without having to recompile the applications or change the code to use the new APIs. Please note that it is still recommended to do this on the long run, but for the first migration step you can use this bundle.

Nearly all features of [wcm.io Configuration 0.x][config-deprecated] are supported with the following exclusions:

* Custom configuration data persistence is not supported
* Of the build-in persistence providers only the `tools/config` provider is supported. If you want to store your data in `/conf` you should directly switch to the Context-Aware Configuration.
* Locking parameters in the configuration hierarchy is not supported (but you can use overrides to mimic this)
* Map data type is no longer supported - but it is mapped to a string array with key value pairs separated by "="

Configuration collections and nested configurations are new features from Sling Context-Aware Configuration are not supported by the compatibility layer (as they where not supported in wcm.io Configuration 0.x).

When this compat bundle is deployed you can either use the old wcm.io Configuration API or the Apache Sling Context-Aware Configuration API to access the configuration. All wcm.io Configurations are grouped in one single configuration with the internal name `config`.

There is also a wiki page with lists different migration scenarios: [Migrate from wcm.io Configuration 0.x to Context-Aware Configuration][caconfig-migration]


### Supported AEM versions

Context-Aware Configuration is supported in AEM 6.1, 6.2, 6.3 and upwards. AEM 6.0 is not supported.

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM. In AEM 6.3 you have to update the Apache Sling Context-Aware Configuration SPI and Impl bundles to the latest version to use all features.

See [Deploy and configure Context-Aware Configuration in AEM][deploy-configure-caconfig-in-aem] for details.


### AEM Version Support Matrix

|Context-Aware Configuration Compatibility Layer version |AEM version supported
|--------------------------------------------------------|----------------------
|1.1.x or higher                                         |AEM 6.2 and up
|1.0.x                                                   |AEM 6.1 and up


[apidocs]: apidocs/
[changelog]: changes-report.html
[config-deprecated]: http://wcm.io/config/
[caconfig-migration]: https://wcm-io.atlassian.net/wiki/x/BgCvAg
[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
[deploy-configure-caconfig-in-aem]: http://wcm.io/caconfig/deploy-configure-caconfig-in-aem.html
