## About Configuration Implementation

Context-specific configuration management implementation.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.config.core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.config.core)


### Documentation

* [API documentation][apidocs]
* [Parameter Persistence Providers][persistence-providers]
* [Parameter Override Providers][override-providers]
* [Changelog][changelog]


### Overview

The `io.wcm.config.core` bundle is the implementation of the API and SPI interfaces defined in the
[Configuration API][configuration-api].

Additionally it provides a [Configuration management API][management-api] which can be used by configuration
editors or other applications which have to write or manage configuration data.

The wcm.io project provides an implementation of such a [Configuration Editor][configuration-editor].


[apidocs]: apidocs/
[changelog]: changes-report.html
[configuration-api]: ../api/
[configuration-editor]: ../editor/
[management-api]: apidocs/io/wcm/config/core/management/package-summary.html
[persistence-providers]: persistence-providers.html
[override-providers]: override-providers.html
