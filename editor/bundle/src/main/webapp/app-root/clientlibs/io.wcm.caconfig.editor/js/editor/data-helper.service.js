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
   * Config service
   */
  angular.module("io.wcm.caconfig.editor")
    .service("dataHelperService", dataHelperService);

  function dataHelperService() {
    /**
     * Parses configuration data.
     * @param {Object} data
     * @returns {Array} configs
     */
    this.parseConfigData = function (data) {
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
    };

    /**
     * @param {Array} configs
     * @param {Boolean} isCollection
     * @returns {json}
     */
    this.buildConfigData = function (configs, isCollection) {
      var configData = {};

      if (isCollection) {
        configData.items = [];

        angular.forEach(configs, function (config) {
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
    };
  }

  // HELPER FUNCTIONS (for helper service)

  /**
   * Parses items (in configuration collection)
   * @param {Array} items
   * @returns {Array} parsed
   */
  function parseCollectionItems(items) {
    var parsed = [];

    angular.forEach(items, function (item) {
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

    angular.forEach(properties, function (property) {
      if (!angular.isObject(property.metadata)) {
        property.skip = true;
      }
      else if (property.nestedConfig || property.nestedConfigCollection) {
        property.skip = true;
        parsed.push(property);
      }
      else {
        parsed.push(property);
      }
    });

    return parsed;
  }

  /**
   * Gets properties from config object
   * @param {Object} config
   * @returns {Object}
   */
  function buildProperties(config) {
    var properties = {};
    var property;
    var tempArray;
    for (var i = 0; i < config.properties.length; i++) {
      property = config.properties[i];

      if (property.skip) {
        continue;
      }

      if (property.value === "" ||
          property.value === null ||
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
    return properties;
  }

})(angular, _);
