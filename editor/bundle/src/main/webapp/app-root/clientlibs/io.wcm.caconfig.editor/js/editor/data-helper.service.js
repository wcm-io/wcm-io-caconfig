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
   * Parses data returned from GET calls.
   * Builds payloads for POST calls.
   */
  angular.module("io.wcm.caconfig.editor")
    .service("dataHelperService", DataHelperService);

  function DataHelperService() {

    /**
     * Parses configuration data.
     * @param {Object} data
     * @returns {Array} configs
     */
    this.parseConfigData = function (data) {
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
        configs: configs,
        newItem: newItem,
        collectionProperties: collectionProperties
      };
    };

    /**
     * @param {Object} current
     * @returns {json}
     */
    this.buildConfigData = function (current) {
      var configData = {};

      if (current.isCollection) {
        configData.properties = {};
        configData.items = [];

        angular.forEach(current.configs, function (config) {
          var item = {
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
  }

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
    for (i = 0; i < config.properties.length; i++) {
      property = config.properties[i];

      if (!property.overridden && !property.inherited
          && !property.nestedConfig && !property.nestedConfigCollection) {

        if (property.name === "sling:configPropertyInherit") {
          properties[property.name] = Boolean(property.value);
        }
        else if (angular.isUndefined(property.value) || property.value === "") {
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

    }
    return properties;
  }

}(angular, _));
