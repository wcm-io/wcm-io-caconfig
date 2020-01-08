<img src="https://wcm.io/images/favicon-16@2x.png"/> wcm.io Context-Aware Configuration
======

## Front-End Development Environment

## Dependencies

- Node 6.11.2
- npm 3.10.10

### Getting Started

1. Start a local AEM instance on port 4502.
2. [Deploy sample project](../../sample-app).
3. Download the [Filesystem Resource Provider](https://sling.apache.org/documentation/bundles/accessing-filesystem-resources-extensions-fsresource.html)
4. Install and start the Filesystem Resource Provider via http://localhost:4502/system/console/bundles
5. Mount the editor bundle via http://localhost:4502/system/console/configMgr. ([See configurations below](#filesystem-resource-configurations)).
6. Load http://localhost:4502/content/contextaware-config-sample/en/config.html?debugClientLibs=true in browser (note query string).
7. Within [this folder](./), run:
  * `npm install`
  * `grunt build`
  * `grunt watch`

### Filesystem Resource Configurations

| Provider Root                            | Filesystem Root                                               |
| ---------------------------------------- | ------------------------------------------------------------- |
| `/apps/wcm-io/caconfig/editor`           | `/path/to/repo/editor/bundle/src/main/webapp/app-root`        |
