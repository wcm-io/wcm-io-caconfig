## Reference Provider

### Overview

wcm.io Context-Aware Configuration Extensions provide an implementation of the AEM [ReferenceProvider][aem-referenceprovider] service interface. For each content page it provides references to the related configuration pages (e.g. stored below /conf).

If you activate an AEM page you will be asked if you want to publish references as well. If there are unpublished configuration changes in configuration pages related to the current configuration context (or higher hierarchy levels) they are listed as well.

This only works if the configurations are stored as AEM pages, e.g. using the "AEM Page" persistence strategy, see [Persistence Strategies][persistence-strategies].


### Configuration

The reference provider can be disabled by configuration, but its enabled by default.

To disable it configure:

```
  io.wcm.caconfig.extensions.references.impl.ConfigurationReferenceProvider
    enabled=B"false"
```



[aem-referenceprovider]: https://docs.adobe.com/docs/en/aem/6-3/develop/ref/javadoc/com/day/cq/wcm/api/reference/ReferenceProvider.html
[persistence-strategies]: persistence-strategies.html
