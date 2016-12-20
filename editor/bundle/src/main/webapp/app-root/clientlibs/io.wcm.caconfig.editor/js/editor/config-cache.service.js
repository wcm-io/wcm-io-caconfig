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
(function (angular, _) {
  "use strict";

  var STORED_CONFIG_CACHE = "caconfig-configCache";

  /**
   * Config Cache service
   *
   * Storing Config Data information in memory / in local storage.
   */
  angular.module("io.wcm.caconfig.editor")
    .service("configCacheService", configCacheService);

  configCacheService.$inject = ["$window"];

  function configCacheService($window) {
    var configCache = {};

    /**
     * Gets "configNameObject" for a config,
     * first checking the configCache,
     * then falling back to the "storedConfigCache" (in local storage)
     *
     * @param  {String} configName
     * @return {Object}
     */
    function getConfigNameObject(configName) {
      var storedConfigCache;
      var config = configCache[configName];

      if (angular.isObject(config) && angular.isObject(config.configNameObject)) {
        return config.configNameObject;
      }

      storedConfigCache = getStoredConfigCache();
      config = storedConfigCache[configName];

      if (angular.isObject(config) && angular.isObject(config.configNameObject)) {
        return config.configNameObject;
      }

      return {};
    };

    this.getConfigNameObject = getConfigNameObject;

    this.plantConfigCache = function (data) {
      configCache = configCache || {};
      angular.forEach(data, function (config) {
        var configName = config.configName;
        if (configName) {
          configCache[configName] = configCache[configName] || {};
          configCache[configName].configNameObject = config;
        }
      });
    };

    this.updateConfigCache = function (data, parent) {
      addConfigsToCache(data, parent);
    };

    /**
     * @param  {Array}   data
     * @param  {Object=} parent - configNameObject
     */
    function addConfigsToCache(data, parentName) {
      var configData;

      for (var i = 0; i < data.length; i++) {
        configData = data[i];
        addConfigToCache(configData, parentName);
      }
      setStoredConfigCache();
    }

    function addConfigToCache(configData, parentName) {
      var configName;
      var isNested = false;
      var isCollection = false;
      var config;
      var parent;
      var properties;
      var children;

      if (angular.isObject(configData.nestedConfig)) {
        configName = configData.nestedConfig.configName;
        isNested = true;
      }
      else if (angular.isObject(configData.nestedConfigCollection)) {
        configName = configData.nestedConfigCollection.configName;
        isNested = true;
        isCollection = true;
      }
      else {
        configName = configData.configName;
      }

      if (!configName) {
        return;
      }

      configCache[configName] = configCache[configName] || {};
      config = configCache[configName];

      // if already has been added to cache
      if (!angular.isUndefined(config.hasChildren) && !(isNested && isCollection)) {
        return;
      }

      parent = getConfigNameObject(parentName);

      config.parent = angular.equals(parent, {}) ? null : parent;
      config.configNameObject = config.configNameObject || {};

      if (isNested) {
        config.configNameObject.configName = configName;
        config.configNameObject.collection = isCollection;
        config.configNameObject.name = configData.name;
        config.configNameObject.description = configData.metadata.description;
        config.configNameObject.label = configData.metadata.label;
      }

      config.configNameObject.breadcrumbs = buildBreadcrumbs(configName);

      properties = getConfigProperties(configData, isNested, isCollection);
      children = getChildren(properties);

      if (children.length) {
        config.hasChildren = true;
        addConfigsToCache(children, configName);
      }
      else {
        config.hasChildren = false;
      }
    }

    /**
     * @param  {Object}  config
     * @param  {Boolean} isNested
     * @param  {Boolean} isCollection
     * @return {Array}
     */
    function getConfigProperties(configData, isNested, isCollection) {
      var properties = [];

      if (isNested && isCollection) {
        properties = _.flatten(_.map(configData.nestedConfigCollection.items, "properties"));
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
      children = _.filter(properties, function (property) {
        return angular.isObject(property.nestedConfig) ||
          angular.isObject(property.nestedConfigCollection);
        });

      return children;
    }

    /**
     * @param  {String} configName
     * @return {Array}
     */
    function buildBreadcrumbs(configName) {
      var config = configCache[configName];
      var configNameObject;
      var breadcrumbs = [];
      var parent;

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
          .replace(/\//g, "");
      }

      breadcrumbs.push(parent);
      breadcrumbs = buildBreadcrumbs(parent.configName).concat(breadcrumbs);

      return breadcrumbs;
    }

    function setStoredConfigCache() {
      $window.localStorage.setItem(STORED_CONFIG_CACHE, angular.toJson(configCache));
    }

    function getStoredConfigCache() {
      var storedConfigCache = angular.fromJson($window.localStorage.getItem(STORED_CONFIG_CACHE));
      if (angular.isObject(storedConfigCache)) {
        return storedConfigCache;
      }
      return {};
    }

    this.removeStoredConfigCache = function () {
      $window.localStorage.removeItem(STORED_CONFIG_CACHE);
    }

  }

})(angular, _);
