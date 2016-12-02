## Configuration Terminology

### Terms

Definition of terms used within the wcm.io context-specific configuration APIs and implementations:

* **Configuration**: With the term 'Configuration' we mean a specific configuration context which is described
  by a root path in the resource hierarchy including it's subtree. Configuration contexts can be nested.
  In real life a configuration can be a site, a region with multiple sites, a tenant with multiple region etc.
  It's up to the application to shape the configuration context as necessary.

* **Configuration Id**: The resource path pointing to the root of the configuration context. This is usually
  not the path where the configuration is stored (although possible), but the content to which the
  configuration should be applied.

* **Application**: Each application that uses the wcm.io context-specific configuration infrastructure
  and want's to provide own configuration parameter definitions or configuration finder strategy has to register
  itself as application. Applications can be full-fledged applications with templates, components, or just a
  library that provides configuration parameters consumed by applications.

* **Application Id**: A unique path identifying the application. If the application is stored in the resource
  hierarchy this should be the root path of the application.

* **Parameter**: A configuration parameter provided by a registered application that can be configured
  per configuration context. Parameter definitions can be enriched with meta data describing there type, default value
  and appearance in a configuration editor.

* **Configuration API**: This is used by applications that want to access the configured context-specific
  parameter-values (read-only).

* **Configuration SPI**: This is used by applications that want to provide own parameter definitions
  or want to customize the configuration management in any way supported.

* **Management API**: This is an API provided by the implementation of the configuration infrastructure
  and is used by applications that want to write configuration data (e.g. a configuration editor). But this is
  an exception, normally no application needs to access this API directly. It's part of the implementation bundle.

* **Configuration Editor**: A GUI for users that allows to change the configuration parameter values, inspect the
  parameter documentation and interact with the configuration inheritance.

* **Configuration Inheritance**: Because configuration contexts can be nested a configuration hierarchy results
  from each nesting level. Configured parameter values can be inherited via this hierarchy.


### Configuration levels

This image demonstrates an example of configuration levels, showing different configuration contexts nested forming
a configuration hierarchy.

![Configuration levels](images/configuration-levels.png)
