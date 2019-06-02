## Override Providers

### Overview

See [Apache Sling Context-Aware Configuration - Override][sling-caconfig-override] for an introduction in the override concept and syntax, and the providers that are included by default.

wcm.io Context-Aware Configuration Extension for AEM adds additional providers.


### Override Provider: Request Header

This provider is deactivated by default. **It should never be activated on production instances.**

With this provider it is possible to inject configuration overrides from HTTP headers of incoming HTTP requests. This is useful on QA instances with automated tests which expect a certain context-aware configuration.

Via the "Header Name" configuration property the name of the header is defined. The header can be included multiple times in the request, each containing an configuration override string.


[sling-caconfig-override]: https://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration-override.html

