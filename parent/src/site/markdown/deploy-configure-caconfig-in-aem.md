## Deploy and configure Context-Aware Configuration in AEM

[Apache Sling Context-Aware Configuration][sling-caconfig] is part of the AEM product since version 6.3. You can also use it in AEM 6.1 or 6.2 by deploying the required bundles and adding some basic configuration. And if you want to use the latest features provided by [wcm.io Context-Aware Configuration][wcmio-caconfig] you need to deploy some updated bundles from Sling in AEM 6.3 as well.

You do not need to deploy additional bundles in AEM 6.4.

### Apache Sling Context-Aware Configuration Bundles

Links to the latest versions of Apache Sling Context-Aware Configuration bundles:

|---|---|---|
| [Apache Sling Context-Aware Configuration API](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.api) |
| [Apache Sling Context-Aware Configuration SPI](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.spi) |
| [Apache Sling Context-Aware Configuration Implementation](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.caconfig.impl) |
| [Apache Johnzon Wrapper Library](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.johnzon) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.johnzon/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.commons.johnzon) |


### Deploy Sling Context-Aware Configuration to AEM 6.1/6.2

In AEM 6.1 or AEM 6.2 you need to deploy the latest version of these Sling bundles:

* `org.apache.sling:org.apache.sling.caconfig.api`
* `org.apache.sling:org.apache.sling.caconfig.spi`
* `org.apache.sling:org.apache.sling.caconfig.impl`
* `org.apache.sling:org.apache.sling.commons.johnzon`

You should apply the same configuration to the Sling Context-Aware Configuration bundles that is present in AEM 6.3 (e.g. for support reading `sling:configRef` property stored in `jcr:content` subnodes of AEM content pages and ignoring properties with `cq:` namespace):

```
  org.apache.sling.caconfig.resource.impl.def.DefaultContextPathStrategy
    configRefResourceNames=["jcr:content","."]
    configRefPropertyNames=["cq:conf"]

  org.apache.sling.caconfig.resource.impl.def.DefaultConfigurationResourceResolvingStrategy
    fallbackPaths=["/conf/global","/apps","/libs"]
    configCollectionInheritancePropertyNames=["jcr:content/sling:configCollectionInherit", "jcr:content/mergeList","mergeList"]

  org.apache.sling.caconfig.management.impl.ConfigurationManagementSettingsImpl
    ignorePropertyNameRegex=["^(jcr|cq):.+$"]
    configCollectionPropertiesResourceNames=["jcr:content","."]
```

If you are using AEM 6.1, make sure to install the latest service pack as well (e.g. AEM 6.1 SP2).

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

Some configuration for Sling Context-Aware configuration already ships with AEM 6.3, but you should add this additional configuration:

```
  org.apache.sling.caconfig.resource.impl.def.DefaultConfigurationResourceResolvingStrategy
    fallbackPaths=["/conf/global","/apps","/libs"]
    configCollectionInheritancePropertyNames=["jcr:content/sling:configCollectionInherit", "jcr:content/mergeList","mergeList"]

  org.apache.sling.caconfig.management.impl.ConfigurationManagementSettingsImpl
    ignorePropertyNameRegex=["^(jcr|cq):.+$"]
    configCollectionPropertiesResourceNames=["jcr:content","."]
```

If you are using AEM 6.3 and want to write configuration by the Configuration Editor or the Context-Aware Configuration Management API you should also install the [wcm.io Context-Aware Configuration Extensions][wcmio-caconfig-extensions] and activate the ["AEM Page" persistence strategy][wcmio-caconfig-extensions-persistence-aempage] - otherwise you may get subtle problems e.g. when using nested configuration collections.


[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
[wcmio-caconfig]: http://wcm.io/caconfig/
[wcmio-caconfig-extensions]: http://wcm.io/caconfig/extensions/
[wcmio-caconfig-extensions-persistence-aempage]: http://wcm.io/caconfig/extensions/persistence-strategies.html
