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
  /**
   * Services module
   */
  angular.module("io.wcm.caconfig.editor")
    .provider("dataService", dataServiceProvider);

  function dataServiceProvider() {
    var restUrls = {};

    function DataService($http, restUrls) {

      /**
       * Get configuration names.
       * @returns {Promise}
       */
      this.getConfigNames = function () {
        return $http.get(restUrls.configNamesUrl);
      };

      this.getConfigNameObject = function(configName, configNamesCollection) {
        var config = _.find(configNamesCollection, {configName: configName});
        if (angular.isObject(config)) {
          return config;
        }
        return {};
      };

      /**
       * Get configuration data.
       * @param {String} configName
       * @param {Boolean} isCollection
       * @returns {Promise}
       */
      this.getConfigData = function (configName, isCollection) {
        var url = restUrls.configDataUrl;

        if (angular.isString(configName)) {
          url += "?configName=" + configName;

          if (isCollection) {
            url += "&collection=true";
          }
        }
        return $http.get(url, {
          transformResponse: parseConfigData
        });
      };

      /**
       * @param {String} configName
       * @param {Boolean} isCollection
       * @param {Array} configs
       * @returns {Promise}
       */
      this.saveConfigData = function (configName, isCollection, configs) {
        var configData = buildConfigData(configs, isCollection);
        var url = restUrls.configPersistUrl + "?configName=" + configName;

        if (isCollection) {
          url += "&collection=true";
        }
        return $http.post(url, configData);
      }

      /**
       * @param {String} configName
       * @returns {Promise}
       */
      this.deleteConfigData =  function (configName) {
        var url = restUrls.configPersistUrl + "?configName=" + configName;
        return $http({
          method: "DELETE",
          url: url
        });
      }
    }

    this.setRestUrls = function (restUrlsConfig) {
      restUrls = restUrlsConfig;
    };

    this.$get = ["$http", function($http) {
      return new DataService($http, restUrls);
    }];
  }

  // HELPER FUNCTIONS
  // TODO: move to separate factory/service

  /**
   * Parses configuration data.
   * @param {Object} data
   * @returns {Array} configs
   */
  function parseConfigData(data) {
    var configs = [];
    var configData = angular.fromJson(data);

    if (angular.isArray(configData.items)) {
      configs = parseCollectionItems(configData.items);
    }
    else {
      configs.push({
        configName: configData.configName,
        properties: parseProperties(configData.properties)
      });
    }
    return configs;
  }

  /**
   * Parses items (in configuration collection)
   * @param {Array} items
   * @returns {Array} parsed
   */
  function parseCollectionItems(items) {
    var parsed = [];

    angular.forEach(items, function(item) {
      parsed.push({
        collectionItemName: item.collectionItemName,
        configName: item.configName,
        properties: parseProperties(item.properties)
      });
    });

    return parsed;
  }

  /**
   * TODO - extraction of nested properties here insufficient
   * - need to create separate configuration(s), with different configName(s)
   *
   * Parses properties - including extraction of nested properties.
   * @param {Array} properties
   * @returns {Array} parsed
   */
  function parseProperties(properties) {
    var parsed = [];
    var items;

    angular.forEach(properties, function(property) {
      if (!angular.isObject(property.metadata)) {
        property.skip = true;
      }
      else if (property.nestedConfig || property.nestedConfigCollection) {
        property.skip = true;
        property.effectiveValue = "NESTED CONFIGURATIONS NOT YET SUPPORTED";
        parsed.push(property);
      }
      else {
        parsed.push(property);
      }
    });

    return parsed;
  }

  /**
   * @param {Array} configs
   * @param {Boolean} isCollection
   * @returns {json}
   */
  function buildConfigData(configs, isCollection) {
    var configData = {};

    if (isCollection) {
      configData.items = [];

      angular.forEach(configs, function(config) {
        var item = {
          collectionItemName: config.collectionItemName,
          properties: buildProperties(config)
        };
        configData.items.push(item);
      });
    }
    else {
      configData.properties = buildProperties(configs[0]);
    }

    return angular.toJson(configData);
  }

  /**
   * Gets properties from config object
   * @param {Object} config
   * @returns {Object}
   */
  function buildProperties(config) {
    var properties = {};
    angular.forEach(config.properties, function(property) {
      var tempArray;
      if (!property.skip) {
        if (property.value === "" || property.value === null ||
          angular.isUndefined(property.value)) {
          properties[property.name] = null;
        }
        else if (angular.isArray(property.value)) {
          tempArray = _.reject(property.value, function (element) {
              return angular.isUndefined(element) || element === null || element === "";
            });
          properties[property.name] = tempArray.length ? _.clone(tempArray) : null;
        }
        else {
          properties[property.name] = property.value;
        }

      }
    });
    return properties;
  }

})(angular, _);
