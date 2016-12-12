## Configuration Editor usage

### Editor GUI

![Configuration levels](images/configuration-editor.png)

The editor supports:

- TBD

The editor is based on AngularJS and CoralUI.


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
