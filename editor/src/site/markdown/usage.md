## Configuration Editor usage

### Editor GUI

![Configuration levels](images/configuration-editor.png)

The parameters displayed in the editor are fetched dynamically from the parameter providers.

The editor supports:

- Filtering parameters by parameter group and/or application
- Entering parameter values using different widgets, e.g. text field, checkbox, multi value field
- Controlling parameter inheritance from ancestor configuration levels
- Locking a parameter value on this level so it cannot be overwritten on decendant configuration levels
- Display parameter documentation in a separate flyout

The editor is based on AngularJS and CoralUI.


### Defining the editor template

The editor application contains an AEM template definition, but it is deactivated by default. Each application
has to define it's own editor template with fitting `allowedPaths`-Definitions and title depending
on the needs of the application.

Only the template has to be defined, the page component resource type can be referenced. Example:

```json
{
  "jcr:primaryType": "cq:Template",
  "jcr:title": "wcm.io Sample Configuration Editor",

  "allowedPaths": "^/content/[^/]+/[^/]+/tools(/.*)?$",
  "allowedChildren": "",

  "jcr:content": {
    "jcr:primaryType": "cq:PageContent",
    "sling:resourceType": "/apps/wcm-io/caconfig/editor/components/page/editor"
  }
}
```
