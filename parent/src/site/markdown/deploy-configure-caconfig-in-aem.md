## Deploy and configure Context-Aware Configuration in AEM

[Apache Sling Context-Aware Configuration][sling-caconfig] is part of the AEM product since version 6.3. You can also use it in AEM 6.1 or 6.2 by deploying the required bundles and adding some basic configuration. And if you want to use the latest features provided by [wcm.io Context-Aware Configuration][wcmio-caconfig] you need to deploy some updated bundles from Sling in AEM 6.3 as well.


### Apache Sling Context-Aware Configuration Bundles

Links to the latest versions of Apache Sling Context-Aware Configuration bundles:

|---|---|---|
| [Apache Sling Context-Aware Configuration API](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api) |
| [Apache Sling Context-Aware Configuration SPI](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi) |
| [Apache Sling Context-Aware Configuration Implementation](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl) |
| [Apache Johnzon Wrapper Library](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.johnzon) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.johnzon/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.johnzon) |


### Deploying Sling Context-Aware Configuration to AEM 6.1 or AEM 6.2

In AEM 6.1 or AEM 6.2 you need to deploy the latest version of these Sling bundles:

* `org.apache.sling:org.apache.sling.caconfig.api`
* `org.apache.sling:org.apache.sling.caconfig.spi`
* `org.apache.sling:org.apache.sling.caconfig.impl`
* `org.apache.sling:org.apache.sling.commons.johnzon`

You should apply the same configuration to the Sling Context-Aware Configuration bundles that is present in AEM 6.3 (e.g. for support reading `sling:configRef` property stored in `jcr:content` subnodes of AEM content pages):

```
  org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy
    configRefResourceNames=["jcr:content","."]
    configRefPropertyNames=["cq:conf"]

  org.apache.sling.caconfig.resource.impl.def.DefaultConfigurationResourceResolvingStrategy
    fallbackPaths=["/conf/global","/apps","/libs"]
    configCollectionInheritancePropertyNames=["jcr:content/mergeList","mergeList"]
```

You should also extend the filter settings for ignoring property names when reading and writing configuration data to also exclude properties with the `cq:` namespace:

```
  org.apache.sling.caconfig.management.impl.ConfigurationManagementSettingsImpl
    ignorePropertyNameRegex=["^(jcr|cq):.+$"]
```

#### Optional: System user for web console

By default, the web console plugin for Sling Context-Aware Configuration used the user that is logged into the web console for accessing the repository. Alternatively you can create a system user which has read access to `/conf` and `/content` and add an service user mapping for this user (named `sling-caconfig` in this example):

```
  org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-sling-caconfig
    user.mapping=["org.apache.sling.caconfig.impl\=sling-caconfig"]
```


### Updating Sling Context-Aware Configuration in AEM 6.3

In AEM 6.3 you should check which versions of the bundles mentioned above are already installed. You should update at least the SPI and Impl bundle to the latest version:

* `org.apache.sling:org.apache.sling.caconfig.spi`
* `org.apache.sling:org.apache.sling.caconfig.impl`
* `org.apache.sling:org.apache.sling.commons.johnzon`

The additional configuration steps for AEM 6.1 and 6.2 are not required for AEM 6.3 because these configurations are already included.

You only have to add this configuration, which is no yet present in AEM 6.3:

```
  org.apache.sling.caconfig.management.impl.ConfigurationManagementSettingsImpl
    ignorePropertyNameRegex=["^(jcr|cq):.+$"]
```



[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
[wcmio-caconfig]: http://wcm.io/caconfig/
