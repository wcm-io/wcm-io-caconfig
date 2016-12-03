## Configuration API usage


### Configuration interface

Accessing configuration is always done via the [Configuration interface][configuration-interface], which is basically
a simple extension of a Sling ValueMap. Objects of this interface are obtained using `adaptTo` on a context
object (`Resource` or `SlingHttpServletRequest`). The configuration context is detected automatically
and an "effective" configuration with all parameters (configured values, inherited values, default values) is
returned.

The API usage is really simple - example:

```java
Configuration config = resource.adaptTo(Configuration.class);
String value = config.get("param1", String.class);
```

Or using a parameter constant provided by the defining application (recommended usage):

```java
Configuration config = resource.adaptTo(Configuration.class);
String value = config.get(Params.PARAM_1);
```

In this case the type can be omitted as it is already part of the parameter definition.
A default value can be supplied optionally.


### Sling models

Example for accessing configuration from a Sling model class:

```java
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class })
public class MyModel {

  @Self
  Configuration config;

  @PostConstruct
  private void activate() {
    String value = config.get("param1", String.class);
  }

}
```

### Sightly template

Example for accessing configuration from a Sightly template:

```xml
<div data-sly-text="${config.param1}"></div>
```

The configuration data is provided inside the Sightly templates by a BindingsValuesProvider.
Such a solution can be used for JSP or other scripting languages as well.


[configuration-interface]: apidocs/io/wcm/config/api/Configuration.html
