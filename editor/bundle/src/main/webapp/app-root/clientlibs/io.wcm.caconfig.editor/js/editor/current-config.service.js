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
    .service("currentConfigService", currentConfigService);

  currentConfigService.$inject = [];

  function currentConfigService() {
    var collectionItemTemplates = {};
    var current = {
      configName: null,
      isCollection: false,
      configs: [],
      configNameObject: {}
    };

    this.setCollectionItemTemplate = function (configName, newItem) {
      if(!collectionItemTemplates[configName]) {
        collectionItemTemplates[configName] = newItem;
      }
    }

    /**
     * Gets "template" newItem object for collections
     * @param  {String} configName
     * @return {Object} (copy of) newItem
     */
    function getCollectionItemTemplate(configName) {
      return angular.copy(collectionItemTemplates[configName]);
    }

    this.getCollectionItemNames = function () {
      return _.map(current.configs, "collectionItemName");
    }

    this.getCurrent = function () {
      return current;
    };

    this.setCurrent = function (data) {
      current.configName = data.configName;
      current.isCollection = data.isCollection;
      current.configs = data.configs;
      current.configNameObject = data.configNameObject;
    };

    /**
     * [addItemToCurrentCollection description]
     * @param {[type]} collectionItemName [description]
     */
    this.addItemToCurrentCollection = function (collectionItemName) {
      var configName = current.configName;
      var itemTemplate = getCollectionItemTemplate(configName);
      current.configs.push({
        collectionItemName: collectionItemName,
        configName: configName,
        overridden: itemTemplate.overridden,
        properties: itemTemplate.properties,
        isNewItem: true
      });
    };

    this.removeItemFromCurrentCollection = function (index) {
      current.configs.splice(index, 1);
    };

  }

})(angular, _);
