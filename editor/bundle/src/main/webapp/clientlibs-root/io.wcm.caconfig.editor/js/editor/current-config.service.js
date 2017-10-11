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
   * Current Config service
   */
  angular.module("io.wcm.caconfig.editor")
    .service("currentConfigService", CurrentConfigService);

  CurrentConfigService.$inject = ["$rootScope"];

  function CurrentConfigService($rootScope) {
    var that = this;

    var CONFIG_PROPERTY_INHERIT = "sling:configPropertyInherit";
    var collectionItemTemplates = {};
    var current = {
      configName: null,
      isCollection: false,
      configs: [],
      configNameObject: {},
      propertyTypes: {}
    };

    that.setCollectionItemTemplate = function (configName, newItem) {
      if (!collectionItemTemplates[configName]) {
        collectionItemTemplates[configName] = newItem;
      }
    };

    /**
     * Gets "template" newItem object for collections
     * @param  {String} configName
     * @return {Object} (copy of) newItem
     */
    function getCollectionItemTemplate(configName) {
      return angular.copy(collectionItemTemplates[configName]);
    }

    that.getCollectionItemNames = function () {
      return _.map(current.configs, "collectionItemName");
    };

    that.getCurrent = function () {
      return current;
    };

    that.setCurrent = function (data) {
      current.configName = data.configName;
      current.isCollection = data.isCollection;
      current.configs = data.configs;
      current.configNameObject = data.configNameObject;
      current.collectionProperties = data.collectionProperties;
      current.propertyTypes = data.propertyTypes;
    };

    /**
     * [addItemToCurrentCollection description]
     * @param {[type]} collectionItemName [description]
     */
    that.addItemToCurrentCollection = function (collectionItemName) {
      var configName = current.configName;
      var itemTemplate = getCollectionItemTemplate(configName);
      var newIndex = current.configs.length;
      current.configs.push({
        collectionItemName: collectionItemName,
        configName: configName,
        overridden: itemTemplate.overridden,
        properties: itemTemplate.properties,
        isNewItem: true
      });

      that.getConfigPropertyInherit(newIndex);
    };

    that.removeItemFromCurrentCollection = function (index) {
      current.configs.splice(index, 1);
    };

    that.handleInheritedChange = function (property) {
      if (!property.metadata.multivalue
          && !property.inherited && angular.isUndefined(property.value)) {
        property.value = property.effectiveValue;
      }
      else {
        property.effectiveValue = "(" + $rootScope.i18n.config.inherited + ")";
        if (angular.isUndefined(property.value)) {
          property.value = null;
        }
      }
    };

    that.getConfigPropertyInherit = function (index) {
      var config = current.configs[index];
      var configPropertyInherit = _.find(config.properties, {
        name: CONFIG_PROPERTY_INHERIT
      });
      if (!configPropertyInherit) {
        configPropertyInherit = {
          name: CONFIG_PROPERTY_INHERIT,
          value: false
        };
        config.properties.push(configPropertyInherit);
      }
      return configPropertyInherit;
    };

    that.setConfigPropertyInherit = function (index, value) {
      var configPropertyInherit = that.getConfigPropertyInherit(index);
      configPropertyInherit.value = value;
      that.handleConfigPropertyInheritChange(index);
    };

    that.handleConfigPropertyInheritChange = function (index) {
      var config = current.configs[index];
      var configPropertyInherit = that.getConfigPropertyInherit(index);
      if (configPropertyInherit.value) {
        return;
      }
      angular.forEach(config.properties, function (property) {
        if (property.name !== CONFIG_PROPERTY_INHERIT && !property.overridden
            && !property.nestedConfig && !property.nestedConfigCollection) {
          property.inherited = false;
          that.handleInheritedChange(property);
        }
      });
    };
  }

}(angular, _));
