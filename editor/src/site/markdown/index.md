## About Configuration Editor

Context-Aware Configuration Editor Template for AEM.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.editor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.editor)


### Documentation

* [Usage][usage]
* [Changelog][changelog]


### Overview

The configuration editor can be used by AEM author users to read and write configuration data mapped to the current context pages.

Features:

* Manage Context-Aware Configuration by creating an editor page in the content context
* Manage singleton configuration, configuration collections and nested configurations
* Display all configuration metadata and default values
* Support all data types and arrays of values
* Special support for inherited and overridden values

See [Usage][usage] for further details and some screenshots.

There is also a [sample application][sample-app] you can deploy directly in a local AEM 6.1 or 6.2 instance to test the configuration editor.


### Supported AEM versions

Context-Aware Configuration is supported in AEM 6.1, 6.2 and upwards. AEM 6.0 is not supported.

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM.



[usage]: usage.html
[changelog]: changes-report.html
[sample-app]: https://github.com/wcm-io/wcm-io-caconfig/tree/develop/sample-app
