<img src="http://wcm.io/images/favicon-16@2x.png"/> wcm.io Context-Aware Configuration
======

## Front-End Development Environment

### Getting Started

1. Start a local AEM 6.1 instance on port 4502.
2. [Deploy sample project](../../sample-app).
3. Use the [Filesystem Resource Provider](http://sling.apache.org/documentation/bundles/accessing-filesystem-resources-extensions-fsresource.html) to mount the editor bundle. ([See configurations below](#filesystem-resource-configurations)).
4. Load http://localhost:4502/content/contextaware-config-sample/en/config.html?debugClientLibs=true in browser (note query string).
5. Within [this folder](./), run:
  * `npm install`
  * `grunt build`
  * `grunt watch`

### Filesystem Resource Configurations

| Provider Root                            | Filesystem Root                                               |
| ---------------------------------------- | ------------------------------------------------------------- |
| `/apps/wcm-io/caconfig/editor`           | `/path/to/repo/editor/bundle/src/main/webapp/app-root`        |
| `/etc/clientlibs/wcm-io/caconfig/editor` | `/path/to/repo/editor/bundle/src/main/webapp/clientlibs-root` |
