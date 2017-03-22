## About Configuration Extensions for AEM

AEM-specific extensions for Apache Sling Context-Aware Configuration.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.extensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.extensions)


### Documentation

* [Usage][usage]
* [Changelog][changelog]


### Overview

The following extensions are provided:

* Configure Context Path Strategies without the need for `sling:configRef` attributes based on hierarchy levels or root templates
* AEM-specific Persistence Strategies to store configuration in `cq:Page` nodes either in `/conf` or in `tools/config` pages together with the content
* Configuration Override Provider based on request headers (e.g. for QA instances - disabled by default)

See [Usage][usage] for further details.


[usage]: usage.html
[changelog]: changes-report.html
