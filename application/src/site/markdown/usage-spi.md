## Configuration SPI usage

### Providing parameter definitions

Each application can provide parameter definitions if it has parameters that have to be configured individually
per context. This is done by implementing an OSGi service implementing the [ParameterProvider][parameter-provider]
interface. By extending the convenience class [AbstractParameterProvider][abstract-parameter-provider] it is possible
to provide all parameters defined as constants in a constant class.

The parameter definitions are constructed using a [ParameterBuilder][parameter-builder] - example:

```java
public static final Parameter<String> PARAM_1 =
    ParameterBuilder.create("param1", String.class, APPLICATION_ID)
    .defaultValue("value1")
    .build();
```

The following types are supported for parameters:

- String
- String Array
- Integer
- Long
- Double
- Boolean
- Map (stored as String Array with key-value pairs internally)

For each parameter definition can be specified:

- Parameter internal name
- Type
- Id of the providing application
- Default value
- Default value derived from an OSGi configuration property specified by service class name and property name
- Custom properties e.g. defining the behavior in the configuration editor


### Application provider

Each application which wants to use context-specific configuration should implement the
[ApplicationProvider][application-provider] interface. This registers the application id and a display label.

Additionally the implementing service can detect via the `matches` if a resource "belongs" to this application or
not (e.g. by checking the path or template). If yes the configuration finder strategies for this application are
applied. For convenience [AbstractPathApplicationProvider][abstract-path-application-provider] can be extended.

Other libraries can hook into this application detection as well, e.g when registering multiple Sling model
implementations to an interface and picking the right implementation for each application based on the resource
context by annotating the Sling models implementation with this [Application][application-annotation] annotation.


### Configuration finder strategy

Each application should provide a [ConfigurationFinderStrategy][finder-strategy] implementation which is able
to detect the configuration scopes in the resource hierarchy. Basically it returns a list of configuration ids
= configuration scope root paths that could be found for a given resource.

For convenience [AbstractAbsoluteParentConfigurationFinderStrategy][abstract-finder-strategy] can be used
if the configuration contexts are always located on a certain hierarchy level.


### Parameter persistence provider

Services implementing the [ParameterPersistenceProvider][persistence-provider] interfaces define how and where
context-specific configuration data is stored. This can be inside the repository within the content path of the
configuration scope, or in a shadow hierarchy e.g. at `/config`, or in an external data source.

The wcm.io implementation contains ready-to-use implementations for this interface:<br/>
[Parameter Persistence Providers][core-persistence-providers].

They have to be activated in the OSGi configuration. If multiple are activated they are treated ordered by
service ranking:

- When writing the configuration data always the first one is used (with lowest service ranking)
- When reading configuration data all active providers are asked to read the data - the first one that
  founds configuration data "wins"

Thus it is possible to start with a certain persistence implementation and switch later to another one, still
supporting the configuration written with the implementation used before.


### Parameter override provider

It is possible to override configured parameter values, either the default value, or the configured value for a
specific or for all configuration contexts, bypassing the value provided by the user in the configuration.

This can be helpful in certain situation e.g.:

- Overriding an URL for a backend system based on staging environment (e.g. QA, Prelive, Live)
- Providing a different default value based on staging environment unless it is explicitly defined in the context
- Disabling a malfunctioning feature centrally for all contexts until a fix is deployed
- Reconfiguring a specific feature for an automated acceptance test

Services implementing the [ParameterOverrideProvider][override-provider] can define from which source such an
parameter override definition can be provided.

The wcm.io implementation contains ready-to-use implementations for this interface:<br/>
[Parameter Override Providers][core-override-providers].

They have to be activated in the OSGi configuration. If multiple are activated they are treated ordered by
service ranking.


### Preconditions and limitations

The current implementation has some preconditions and limitations:

- Parameter names: The parameter names have to be globally unique across all applications/parameter providers.
  Although it would be technically possible to read them in different 'namespaces' per application, this would
  break the simplicity of the ValueMap API when accessing the configuration directly via it's string name. So
  currently only a warning is logged if different parameter provider supply parameter definitions with the same name.

- Flat parameter list: For each configuration context only a flat list of parameters is supported. It can be
  filtered in the configuration editor by parameter groups and applications.

- Configuration Id: Only resource paths pointing to the real content affected by the configuration scope are supported.
  Although technically most parts of the SPI support arbitrary configuration ids whose meaning is hidden
  in the implementation details of the configuration finder strategy implementation this does not work when
  merging configuration ids from different configuration finder strategies provided by different applications. To
  support the inheritance accross configuration scopes the configuration management has to understand the format
  of the configuration Ids, thus the limitation to real paths.




[parameter-builder]: apidocs/io/wcm/config/api/ParameterBuilder.html
[parameter-provider]: apidocs/io/wcm/config/spi/ParameterProvider.html
[abstract-parameter-provider]: apidocs/io/wcm/config/spi/helpers/AbstractParameterProvider.html
[application-provider]: apidocs/io/wcm/config/spi/ApplicationProvider.html
[abstract-path-application-provider]: apidocs/io/wcm/config/spi/helpers/AbstractPathApplicationProvider.html
[finder-strategy]: apidocs/io/wcm/config/spi/ConfigurationFinderStrategy.html
[abstract-finder-strategy]: apidocs/io/wcm/config/spi/helpers/AbstractAbsoluteParentConfigurationFinderStrategy.html
[persistence-provider]: apidocs/io/wcm/config/spi/ParameterPersistenceProvider.html
[override-provider]: apidocs/io/wcm/config/spi/ParameterOverrideProvider.html
[application-annotation]: apidocs/io/wcm/config/spi/annotations/Application.html
[core-persistence-providers]: ../core/persistence-providers.html
[core-override-providers]: ../core/override-providers.html
