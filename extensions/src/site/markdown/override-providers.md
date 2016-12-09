## Parameter Override Providers

The wcm.io implementation contains ready-to-use implementations for the `ParameterOverrideProvider` interface.


### Common syntax for all providers

Each parameter override provider provides a map with parameter default values or parameter overrides. The map
consists of key/value pairs with a common syntax:

* Key: `[{scope}[:locked]]{parameterName}`
* Value: any value

The `[{scope}]` can be:

* `[default]` - overrides default value
* `[/x/y/z]` - overrides the configured value for configuration id `/x/y/z`
* missing or `[locked]` - overrides the configured value for all configuration ids
* If the scope value is suffixed with the string ":locked" this configuration parameter cannot be overridden in nested configuration scopes.


### OsgiConfigOverrideProvider

Allows to define configuration property default values or overrides from OSGi configuration.

You can provide multiple providers using a factory configuration, each of them provides map of key/value-pairs.
Each map entry is a single string with keys separated from the values by `=`.

Example:

```
[/content/site1/en]param1=New value
param2=55
[default]param3=on
```

This provider is not active by default, it has to be activated via OSGi configuration.


### SystemPropertyOverrideProvider

Allows to define configuration property default values or overrides from system environment properties.

The parameters are defined when starting the JVM using the `-D` command line parameter. Each parameter defines
one map entry in a single string with keys separated from the values by `=`. All parameter names have to be
prefixed with the string `config.override.`.

Example:

```
-D"config.override.[/content/site1/en]param1=New value"
-Dconfig.override.param2=55
-Dconfig.override.[default]param3=on
```

This provider is not active by default, it has to be activated via OSGi configuration.


### RequestHeaderOverrideProvider

Allows to define configuration property default values or overrides from incoming request headers. This is useful
if you want to override you configuration parameters on a running instance from outside, e.g from a Selenium
test script.

The request header name is the key, and the header value the value.

This provider is not active by default, it has to be activated via OSGi configuration.
_This must never be activated on an instance available via the public internet for security reasons!_
