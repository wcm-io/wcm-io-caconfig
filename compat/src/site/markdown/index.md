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

When this compat bundle is deployed you can either use the old wcm.io Configuration API or the Apache Sling Context-Aware Configuration API to access the configuration. All wcm.io Configurations are grouped in one single configuration with the internal name `config`.

There is also a wiki page with lists different migration scenarios: [Migrate from wcm.io Configuration 0.x to Context-Aware Configuration][caconfig-migration]

[apidocs]: apidocs/
[changelog]: changes-report.html
[config-deprecated]: http://wcm.io/config/
[caconfig-migration]: https://wcm-io.atlassian.net/wiki/x/BgCvAg
[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
