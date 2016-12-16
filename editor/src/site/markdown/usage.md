## Configuration Editor usage

### Basic concepts

The Configuration Editor is a template which is used inside the '/content' tree. It allows to edit configurations for the inner-most context that is detected within the context tree (e.g. by defining `sling:configRef` properties). Where the configuration itself is stored depends on your system settings, by default it's stored in `/conf`.

You cannot define the contexts via the configuration editor, you have to set the `sling:configRef` manually or within the page properties of your content templates, or by defining custom context path strategies. But once your contexts are defined you can create a config editor page within each context and edit the configuration parameters.

The configuration editor supports only editing configuration for which configuration metadata is present. This is normally done by deploying configuration annotation classes with your applications.

See [Apache Sling Context-Aware Configuration documentation][sling-caconfig] for more details.


### Installation

In most cases you will deploy the configuration editor bundle `io.wcm.caconfig.editor` together with your application. In this case you should define your own template definition for it which controls where editor config pages can created (see next section).

Alternatively you can deploy this AEM package which contains the config editor bundle together with a template definitions which allows all paths below `/content`:
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.editor.package/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm/io.wcm.caconfig.editor.package)

When you are using AEM 6.1 or 6.2 you have to additionally deploy the Apache Sling Context-Aware Configuration bundles (API, SPI, Impl) to AEM.


### Defining the editor template

The editor application contains an AEM template definition, but it is deactivated by default. Each application
has to define it's own editor template with fitting `allowedPaths`-Definitions and title depending
on the needs of the application.

Only the template has to be defined, the page component resource type can be referenced. Example:

```json
{
  "jcr:primaryType": "cq:Template",
  "jcr:title": "Configuration Editor",

  "allowedPaths": "^/content(/.*)?$",

  "jcr:content": {
    "jcr:primaryType": "cq:PageContent",
    "sling:resourceType": "/apps/wcm-io/caconfig/editor/components/page/editor"
  }
}
```


### Editor GUI

![Configuration Overview](images/configuration-overview.png)

When opening the Configuration Editor an overview of all configurations is displayed for which some configuration data is present. By using the "Add" button you can enter new configuration data for other configurations where no data exists yet.

![Singleton Configuration](images/configuration-editor-singleton.png)

For a singleton configuration all configuration parameters are displayed and can be changed. With the "Save" button the changes are persisted, the "Delete" button removes the whole configuration.

![ConfigurationL Collection](images/configuration-editor-list.png)

For a configuration collection all existing collection items are displayed, and new ones can be added after entering a name. Single items or the whole configuration collection can be removed.



The editor is based on AngularJS and CoralUI.




[sling-caconfig]: http://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
