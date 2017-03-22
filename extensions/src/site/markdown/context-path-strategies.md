## Context Path Strategies

### Overview

To provide context-aware configuration you have to define the context first. In most cases a context is a certain subtree below `/content` - e.g. one site or a group of sites. In Sling Context-Aware Configuration this is possible by setting `@sling:configRef` attributes on the root page of each context's subtree (see [Contexts and configuration references][sling-caconfig-contexts]).

But when you have a lot of contexts (e.g. 1,000 sites worldwide), and all your context/config path mappings follow the same schema, it is tedious to define all these `@sling:configRef` properties and keeping them in sync when the content moves.

wcm.io Configuration Extensions for AEM provides two alternatives for defining contexts without the need to set such properties. Both are configured via OSGi and support filtering by context path. You can define different strategies for different content areas by adding multiple factory configurations.

### Context Path Strategy: Absolute Parents

OSGi Factory configuration: "wcm.io Context-Aware Configuration Context Path Strategy: Absolute Parents":

* **Absolute Levels**: List of absolute parent levels that should be considered as context roots. Example: Absolute parent level 1 of `/foo/bar/test` is `/foo/bar`.
* **Context path whitelist**: Expression to match context paths. Context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.
* **Context path blacklist**: Expression to match context paths. Context paths matching this expression are not allowed.
* **Config path patterns**: Expression to derive the config path from the context path. Regex group references like `$1` can be used.
* **Service Ranking**: Priority of context path strategy

Example:

```
  io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy-example
    levels=I["2","3"]
    contextPathRegex="^/content(/.+)$"
    configPathPatterns=["/conf$1"]
```

With this configuration content paths like `/content/brand1/region1` and `/content/brand1/region1/country1` would be detected as contexts and the configuration would be stored in `/conf/brand1/region1` and `/conf/brand1/region1/country1`.


### Context Path Strategy: Root Templates

OSGi Factory configuration: "wcm.io Context-Aware Configuration Context Path Strategy: Root Template":

* **Templates**: List of template paths allowed for context root pages.
* **Min. Level**: Minimum allowed absolute parent level. Example: Absolute parent level 1 of `/foo/bar/test` is `/foo/bar`.
* **Max. Level**: Maximum allowed absolute parent level. Example: Absolute parent level 1 of `/foo/bar/test` is `/foo/bar`.
* **Context path whitelist**: Expression to match context paths. Context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.
* **Config path patterns**: Expression to derive the config path from the context path. Regex group references like `$1` can be used.
* **Service Ranking**: Priority of context path strategy

Example:

```
  io.wcm.caconfig.extensions.contextpath.impl.RootTemplateContextPathStrategy-example
    templatePaths=["/apps/app1/templates/homepage"]
    maxLevel=I"4"
    contextPathRegex="^/content(/.+)$"
    configPathPatterns=["/conf$1"]
```

With this configuration all content pages with the template `/apps/app1/templates/homepage` and a maximum absolute parent level of 4 would be detected as contexts. For a homepage with path `/content/example/en` the configuration would be stored in `/conf/example/en`.
