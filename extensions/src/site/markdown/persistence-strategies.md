## Persistence Strategies

### Overview

By default Sling Context-Aware Configuration stores configuration in a hierarchy of nodes below `/conf` using `nt:unstructured` node types. This is simple enough, but it makes it difficult to apply operations like replication on it in AEM.

Thus it would be good when configuration can be stored in `cq:Page` nodes as it is done by the "AEM ConfMgr" for AEM. AEM 6.3 ships with such an Persistence Strategy, but it only supports read access to configuration, not write access.

wcm.io Extensions for AEM provide additionally persistence strategy implementations


### Persistence Strategy: AEM Page

OSGi configuration: "wcm.io Context-Aware Configuration Persistence Strategy: AEM Page".

The strategy is disabled by default and can be enabled like this:

```
  io.wcm.caconfig.extensions.persistence.impl.PagePersistenceStrategy
    enabled=B"true"
```

When enabled it stores and reads configuration in `/conf` wrapped in `cq:Page` nodes. The configuration itself is stored in the `jcr:content` subnode. If nested configurations are used they are stored in subnodes of this `jcr:content` node.

It uses the same persistence format as the default persistence in AEM 6.3, but also supports writing configuration.


### Persistence Strategy: Tools Config Page

OSGi configuration: "wcm.io Context-Aware Configuration Persistence Strategy: Tools Config Page":


The strategy is disabled by default and can be enabled like this:

```
  io.wcm.caconfig.extensions.persistence.impl.ToolsConfigPagePersistenceStrategy
    enabled=B"true"
```

This persistence strategy only makes sense when used together with one of the [Context Path Strategies][context-path-strategies] provided by wcm.io Context-Aware Configuration Extensions for AEM.

You have to define an additional `configPathPattern` entry to add the `tools/config` path of each context to the list of paths where configuration can be read and stored. Example using the Absolute Path strategy:

```
  io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy-default
    levels=I["2","3"]
    contextPathRegex="^/content(/.+)$"
    configPathPatterns=["/conf$1","/content$1/tools/config/jcr:content"]
```

Consider this example content structure:

```
/content
  /brand1
    /region1
      /country1
        /tools
          /config
```

The path `/content/brand1/region1/country1` is detected as config root. Configuration for it is searched first in the page located at `/content/brand1/region1/country1/tools/config`, and if it not found there in `/conf/brand1/region1/country1` and then following the Sling Context-Aware configuration inheritance concepts.

Usually the page `/content/brand1/region1/country1/tools/config` is associated with the template of the [wcm.io Context-Aware Configuration Editor][wcmio-caconfig-editor]. Configuration that is saves is stored directly in this page. The author can simply replicate the stored configuration to the publish instance by activating it.


[context-path-strategies]: context-path-strategies.html
[wcmio-caconfig-editor]: http://wcm.io/caconfig/editor/
