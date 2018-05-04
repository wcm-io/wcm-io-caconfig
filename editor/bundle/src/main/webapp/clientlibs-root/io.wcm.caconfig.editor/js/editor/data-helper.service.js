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

  /**
   * Parses data returned from GET calls.
   * Builds payloads for POST calls.
   */
  angular.module("io.wcm.caconfig.editor")
    .service("dataHelperService", DataHelperService);

  DataHelperService.$inject = ["propertyNames"];

  function DataHelperService(propertyNames) {
    var that = this;

    /**
     * Parses configuration data.
     * @param {Object} data
     * @returns {Object}
     */
    that.parseConfigData = function (data) {
      var configs = [];
      var configData = angular.fromJson(data);
      var newItem = null;
      var collectionProperties = {};

      if (angular.isArray(configData.items)) {
        configs = configData.items;
        newItem = configData.newItem;
        collectionProperties = configData.properties || {};
      }
      else {
        configs.push(configData);
      }
      return {
        configs: setDefaultValues(configs),
        newItem: newItem !== null ? setDefaultValues([newItem])[0] : null,
        collectionProperties: collectionProperties
      };
    };

    /**
     * Checks configs' properties and sets default values where appropriate
     * @param {Array} configs
     * @returns {Array} configs
     */
    function setDefaultValues(configs) {
      var propsWithDefault = [];
      if (configs.length) {
        angular.forEach(configs[0].properties, function (property, ix) {
          if (property["default"]) {
            propsWithDefault.push(ix);
          }
        });

        angular.forEach(propsWithDefault, function (ix) {
          angular.forEach(configs, function (config) {
            var property = config.properties[ix];
            if (angular.isUndefined(property.value)) {
              property.value = property.effectiveValue;
            }
          });
        });
      }
      return configs;
    }

    /**
     * @param {Object} current
     * @returns {json}
     */
    that.buildConfigData = function (current) {
      var configData = {};

      if (current.isCollection) {
        configData.properties = {};
        configData.items = [];

        angular.forEach(current.configs, function (config) {
          var item;
          if (config.inherited) {
            return;
          }
          item = {
            collectionItemName: config.collectionItemName,
            properties: buildProperties(config)
          };
          configData.items.push(item);
        });

        configData.properties = current.collectionProperties || {};
      }
      else {
        configData.properties = buildProperties(current.configs[0]);
      }
      return angular.toJson(configData);
    };

    /**
     * Gets properties from config object
     * @param {Object} config
     * @returns {Object}
     */
    function buildProperties(config) {
      var properties = {};
      var i,
        property,
        tempArray;
      for (i = 0; (config.properties && i < config.properties.length); i++) {
        property = config.properties[i];

        if (property.name === propertyNames.CONFIG_PROPERTY_INHERIT) {
          properties[property.name] = Boolean(property.value);
        }
        else if (!property.overridden && !property.inherited
            && !property.nestedConfig && !property.nestedConfigCollection) {
          if (angular.isUndefined(property.value) || property.value === "") {
            properties[property.name] = null;
          }
          else if (angular.isArray(property.value)) {
            tempArray = property.value.filter(function (element) {
              return angular.isDefined(element) && element !== null && element !== "";
            });
            properties[property.name] = tempArray.length ? angular.copy(tempArray) : null;
          }
          else {
            properties[property.name] = property.value;
          }

        }

      }
      return properties;
    }
  }

}(angular));
