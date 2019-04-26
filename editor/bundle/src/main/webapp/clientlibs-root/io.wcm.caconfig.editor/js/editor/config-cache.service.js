/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function (angular) {
  "use strict";

  var STORED_CONFIG_CACHE = "caconfig-configCache";

  /**
   * Config Cache service
   *
   * Storing Config Data information in memory / in local storage.
   */
  angular.module("io.wcm.caconfig.editor")
    .service("configCacheService", ConfigCacheService);

  ConfigCacheService.$inject = ["$window", "inputMap"];

  function ConfigCacheService($window, inputMap) {
    var that = this;
    var configCache;

    /**
     * Gets "configNameObject" for a config from cache.
     * If none exists, then the user has deeplinked to the config with cleared localstorage
     * and will be unable to view or edit the config correctly.
     *
     * @param  {String} configName
     * @return {Object|null}
     */
    that.getConfigNameObject = function(configName) {
      var config = configCache[configName];

      if (angular.isObject(config) && angular.isObject(config.configNameObject)) {
        return config.configNameObject;
      }

      return null;
    };

    /**
     * @param  {String} configName
     * @param  {String} propertyName
     * @return {String}
     */
    that.getPropertyType = function(configName, propertyName) {
      return configCache[configName].propertyTypes[propertyName] || "";
    };

    /**
     * @param  {String} configName
     * @return {Object|null}
     */
    that.getPropertyTypes = function(configName) {
      return configCache[configName].propertyTypes || null;
    };

    function addPropertyTypesToCache(configName, properties) {
      if (!properties.length) {
        return;
      }

      configCache[configName] = configCache[configName] || {};
      configCache[configName].propertyTypes = configCache[configName].propertyTypes || {};

      angular.forEach(properties, function (property) {
        var propertyName = property.name;
        if (propertyName && angular.isUndefined(configCache[configName].propertyTypes[propertyName])) {
          configCache[configName].propertyTypes[propertyName] = determinePropertyType(property);
        }
      });
    }

    function determinePropertyType(property) {
      var input;
      if (property.nestedConfig) {
        return "nestedConfig";
      }
      if (property.nestedConfigCollection) {
        return "nestedConfigCollection";
      }
      if (property.metadata && property.metadata.multivalue) {
        return "multivalue";
      }
      if (property.metadata && property.metadata.properties
            && property.metadata.properties.widgetType === "pathbrowser") {
        return "pathbrowser";
      }
      if (property.metadata && property.metadata.type) {
        input = inputMap[property.metadata.type];
        return input.type || property.metadata.type;
      }
      return "";
    }

    that.plantConfigCache = function (data) {
      configCache = configCache || getStoredConfigCache() || {};
      angular.forEach(data, function (config) {
        var configName = config.configName;
        if (configName) {
          configCache[configName] = configCache[configName] || {};
          configCache[configName].configNameObject = config;
          configCache[configName].isRoot = true;
        }
      });
      return configCache;
    };

    /**
     * @param  {Array}   configs
     * @param  {String=} parentName
     */
    that.updateConfigCache = function (configs, parentName) {
      var configData,
        i;

      for (i = 0; i < configs.length; i++) {
        configData = configs[i];
        addConfigToCache(configData, parentName);
      }
      setStoredConfigCache();
    };

    function addConfigToCache(configData, parentName) {
      var isNested = false;
      var isNestedCollection = false;
      var isCollectionItem = false;
      var children,
        config,
        configName,
        properties;

      if (angular.isObject(configData.nestedConfig)) {
        configName = configData.nestedConfig.configName;
        isNested = true;
      }
      else if (angular.isObject(configData.nestedConfigCollection)) {
        configName = configData.nestedConfigCollection.configName;
        isNested = true;
        isNestedCollection = true;
      }
      else {
        configName = configData.configName;
      }

      if (!configName) {
        return;
      }

      configCache[configName] = configCache[configName] || {};
      config = configCache[configName];

      isCollectionItem = angular.isString(configData.collectionItemName);

      // If config has already been "fully" added to cache
      if (angular.isDefined(config.propertyTypes)
          && angular.isDefined(config.hasChildren)
          && !isCollectionItem
          && !isNestedCollection
          && (config.isRoot || config.configNameObject.breadcrumbs.length)) {
        return;
      }

      if (!isCollectionItem) {
        if (!config.parent && !config.isRoot) {
          if (angular.isDefined(parentName)) {
            config.parent = that.getConfigNameObject(parentName);
          }
          else {
            // User has deeplinked to a nested config with uncached parent.
            // This will cause problems, so we so we abort the process.
            delete configCache[configName];
            window.console.error("Deeplinking error.");
            return;
          }
        }

        if (angular.isUndefined(config.configNameObject)) {
          config.configNameObject = config.configNameObject || {};

          if (isNested) {
            config.configNameObject.configName = configName;
            config.configNameObject.collection = isNestedCollection;
            config.configNameObject.name = configData.name;
            config.configNameObject.description = configData.metadata.description;
            config.configNameObject.label = configData.metadata.label;
          }

          config.configNameObject.breadcrumbs = buildBreadcrumbs(configName);
        }
      }

      properties = getConfigProperties(configData, isNested, isNestedCollection);

      if (angular.isUndefined(config.propertyTypes)) {
        addPropertyTypesToCache(configName, properties);
      }

      children = getChildren(properties);

      if (children.length) {
        config.hasChildren = true;
        that.updateConfigCache(children, configName);
      }
      else {
        config.hasChildren = false;
      }
    }

    /**
     * @param  {Object}  configData
     * @param  {Boolean} isNested
     * @param  {Boolean} isNestedCollection
     * @return {Array}
     */
    function getConfigProperties(configData, isNested, isNestedCollection) {
      var properties = [];
      var itemProperties = [];
      var flattenedItemProperties = [];

      if (isNestedCollection) {
        itemProperties = configData.nestedConfigCollection.items.map(function (item) {
          return item.properties;
        });

        flattenedItemProperties = itemProperties.reduce(function (a, b) {
          return a.concat(b);
        }, []);

        properties = flattenedItemProperties;
      }
      else if (isNested) {
        properties = configData.nestedConfig.properties;
      }
      else {
        properties = configData.properties;
      }

      return angular.isArray(properties) ? properties : [];
    }

    /**
     * Extracts sub/child-configs (nestedConfig or nestedConfigCollection)
     * from config properties
     * @param  {Array} properties
     * @return {Array}
     */
    function getChildren(properties) {
      var children = [];
      children = properties.filter(function (property) {
        return angular.isObject(property.nestedConfig)
          || angular.isObject(property.nestedConfigCollection);
      });

      return children;
    }

    /**
     * @param  {String} configName
     * @return {Array}
     */
    function buildBreadcrumbs(configName) {
      var config = configCache[configName];
      var breadcrumbs = [];
      var configNameObject,
        parent;

      if (!config || !config.parent) {
        return breadcrumbs;
      }

      configNameObject = config.configNameObject;

      if (configNameObject.breadcrumbs) {
        return configNameObject.breadcrumbs;
      }

      parent = angular.copy(config.parent);

      if (parent.collection) {
        parent.itemName = configName.replace(parent.configName, "")
          .replace(configNameObject.name, "")
          .replace(/^\//, "") // remove slash at start
          .replace(/\/$/, "") // remove slash at end
          .replace(/\//g, " / "); // add spaces around remaining slashes, for view
      }

      breadcrumbs.push(parent);
      breadcrumbs = buildBreadcrumbs(parent.configName).concat(breadcrumbs);

      return breadcrumbs;
    }

    function setStoredConfigCache() {
      $window.localStorage.setItem(STORED_CONFIG_CACHE, angular.toJson(configCache));
    }

    /**
     * @return {object|null}
     */
    function getStoredConfigCache() {
      var storedConfigCache = angular.fromJson($window.localStorage.getItem(STORED_CONFIG_CACHE));
      if (angular.isObject(storedConfigCache)) {
        return storedConfigCache;
      }
      return null;
    }

    that.removeStoredConfigCache = function () {
      $window.localStorage.removeItem(STORED_CONFIG_CACHE);
    };

  }

}(angular));
