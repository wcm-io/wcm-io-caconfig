wcm.io Context-Aware Configuration AEM Sample
=============================================

Deploy sample project
---------------------

You can use this script for a full deployment (application, sample content, configuration) into a local AEM 6.1 instance running at http://localhost:4502:

```
clean_install_deploy_package.sh
```

This script also cleans and builds all maven projects and generates eclipse project files.


Open Configuration Editor
-------------------------

Open configuration editor at<br/>
[http://localhost:4502/content/contextaware-config-sample/en/config.html](http://localhost:4502/content/contextaware-config-sample/en/config.html)

To see how inheritance and override works you can also look at<br/>
[http://localhost:4502/content/contextaware-config-sample/en/sub-page/config.html](http://localhost:4502/content/contextaware-config-sample/en/sub-page/config.html)<br/>
[http://localhost:4502/content/contextaware-config-sample/en/sub-page/sub-page-override/config.html](http://localhost:4502/content/contextaware-config-sample/en/sub-page/sub-page-override/config.html)


Inspect Configuration
---------------------

You can look how the configuration is stored in the repository by browsing through `/conf/contextaware-config-sample`.
