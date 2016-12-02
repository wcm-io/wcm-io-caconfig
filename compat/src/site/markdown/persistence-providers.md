## Parameter Persistence Providers

The wcm.io implementation contains ready-to-use implementations for the `ParameterPersistenceProvider` interface.


### ConfStructurePersistenceProvider

Persistence provider that stores configuration values in pages at `/conf` appending the
configuration id to build the full path.

The configuration is stored in a shadow structure below `/conf`. This is useful if you want to apply special ACLs to this
path, and you want not to package the configuration together with your content (you can still do this of course by
adding both `/config` and `/conf` paths to your package definition). Be aware that when moving the content
below `/content`, the configuration in the shadow structure below `/conf` is not moved, you have to do this
manually.

By default this provider is not enabled, it has to be enabled via OSGi configuration.


### ToolsConfigPagePersistenceProvider

Persistence provider that stores configuration values in pages in a path `tools/config` relative to the
configuration id.

The configuration is stored together with the content below `/content`. This is useful if you want to store
the configuration near to your content and ensure it is packaged within you content packages and moved whenever
you move your content somehwere alse. But beware of security implications - do not store sensitive configuration
information below `/content`, it is difficult to protected it via ACLs there.

By default this provider is not enabled, it has to be enabled via OSGi configuration.
