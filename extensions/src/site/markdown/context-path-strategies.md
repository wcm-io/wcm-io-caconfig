## Context Path Strategies

### Overview

To provide context-aware configuration you have to define the context first. In most cases a context is a certain subtree below `/content` - e.g. one site or a group of sites. In Sling Context-Aware Configuration this is possible by setting `@sling:configRef` attributes on the root page of each context's subtree (see [Contexts and configuration references][sling-caconfig-contexts]).

But when you have a lot of contexts (e.g. 1,000 sites worldwide), and all your context/config path mappings follow the same schema, it is tedious to define all these `@sling:configRef` properties and keeping them in sync when the content moves.

wcm.io Context-Aware Configuration Extensions for AEM provides two alternatives for defining contexts without the need to set such properties. Both are configured via OSGi and support filtering by context path. You can define different strategies for different content areas by adding multiple factory configurations.

### Context Path Strategy: Absolute Parents

OSGi factory configuration: "wcm.io Context-Aware Configuration Context Path Strategy: Absolute Parents":

* **Absolute Levels**: List of absolute parent levels that should be considered as context roots. Example: Absolute parent level 1 of `/foo/bar/test` is `/foo/bar`.
* **Context path whitelist**: Expression to match context paths. Context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.
* **Context path blacklist**: Expression to match context paths. Context paths matching this expression are not allowed.
* **Config path patterns**: Expression to derive the config path from the context path. Regex group references like `$1` can be used.
* **Service Ranking**: Priority of context path strategy

This strategy detects context paths at absolute parent levels (starting at `/content` with level=0). It only accepts paths that match the whitelist and does not match the blacklist, and uses the nearest accepted parent if the current path is not accepted.

Example:

```
  io.wcm.caconfig.extensions.contextpath.impl.AbsoluteParentContextPathStrategy-example
    levels=I["2","3"]
    contextPathRegex="^/content(/.+)$"
    configPathPatterns=["/conf$1"]
    contextPathBlacklistRegex="^.*/tools(/config(/.+)?)?$"
```

With this configuration the strategy produces the following results:

|   |Content path                               |Detected context path             |Derived config path
|---|-------------------------------------------|----------------------------------|-------------------------------
|a) |`/content/brand1/region1`                  |`/content/brand1/region1`         |`/conf/brand1/region1`
|b) |`/content/brand1/region1/country1`         |`/content/brand1/region1/country1`|`/conf/brand1/region1/country1`
|c) |`/content/brand1/region1/country1/en/page1`|`/content/brand1/region1/country1`|`/conf/brand1/region1/country1`
|d) |`/content/brand1/region1/tools/config`     |`/content/brand1/region1`         |`/conf/brand1/region1`

Explanation:

* a) Direct match with level 2
* b) Direct match with level 3
* c) Page with level 5, next matching absolute parent is level 3
* d) Configuration editor page with level 4 which is blacklisted, next matching absolute parent is level 2


### Context Path Strategy: Root Templates

OSGi factory configuration: "wcm.io Context-Aware Configuration Context Path Strategy: Root Template":

* **Templates**: List of template paths allowed for context root pages.
* **Min. Level**: Minimum allowed absolute parent level. Example: Absolute parent level 1 of `/foo/bar/test` is `/foo/bar`.
* **Max. Level**: Maximum allowed absolute parent level. Example: Absolute parent level 1 of `/foo/bar/test` is `/foo/bar`.
* **Context path whitelist**: Expression to match context paths. Context paths matching this expression are allowed. Use groups to reference them in configPathPatterns.
* **Config path patterns**: Expression to derive the config path from the context path. Regex group references like `$1` can be used.
* **Service Ranking**: Priority of context path strategy


This strategy detects context paths by matching parent pages against a list of allowed templates for context root. All page between min and max level up to a page with a page matching the templates are defined as context paths.

Example:

```
  io.wcm.caconfig.extensions.contextpath.impl.RootTemplateContextPathStrategy-example
    templatePaths=["/apps/app1/templates/homepage"]
    minLevel=I"1"
    maxLevel=I"4"
    contextPathRegex="^/content(/.+)$"
    configPathPatterns=["/conf$1"]
```

If a page with the configured template exists at level 3 (e.g. `/content/tenant/country/en`), the levels 1, 2, 3 would be considered as context paths (e.g. `/content/tenant`, `/content/tenant/country`, `/content/tenant/country/en`).
