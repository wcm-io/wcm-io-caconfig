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

  var STORED_CONFIG_TREE = "caconfig-configTree";

  /**
   * Config service
   *
   * Interface between dataService and controllers
   */
  angular.module("io.wcm.caconfig.editor")
    .service("configService", configService);

  configService.$inject = ["$window", "dataService", "modalService"];

  function configService($window, dataService, modalService) {
    var contextPath = null;
    var configNames = [];
    var collectionItemTemplates = {};
    var configTree = {};

    // GETTERS

    /**
     * @return {String|null} contextPath
     */
    this.getContextPath = function () {
      return contextPath;
    };

    /**
     * @return {Array} configNames
     */
    this.getConfigNames = function () {
      return configNames;
    };

    /**
     * Gets "configNameObject" for a config,
     * first checking the configTree,
     * then falling back to the "storedConfigTree" (in local storage)
     *
     * @param  {String} configName
     * @return {Object|null}
     */
    this.getConfigNameObject = function (configName) {
      var storedConfigTree;
      var config = configTree[configName];

      if (angular.isObject(config) && angular.isObject(config.configNameObject)) {
        return config.configNameObject;
      }

      storedConfigTree = getStoredConfigTree();
      config = storedConfigTree[configName];

      if (angular.isObject(config) && angular.isObject(config.configNameObject)) {
        return config.configNameObject;
      }

      return null;
    };

    this.getCollectionItemTemplate = function (configName) {
      return angular.copy(collectionItemTemplates[configName]);
    };


    // DATA OPERATIONS
    this.loadConfigNames = function () {
      return dataService.getConfigNames().then(
        function success(result) {
          contextPath = result.data.contextPath;
          configNames = result.data.configNames;
          plantConfigTree(configNames);
        },
        function error() {
          modalService.show(modalService.modal.ERROR);
        }
      );
    };

    this.loadConfig = function (configName, isCollection) {
      if (isCollection && !collectionItemTemplates[configName]) {
        loadCollectionItemTemplates(configName);
      }

      return dataService.getConfigData(configName, isCollection).then(
        function success(result){
          updateConfigTree(result.data);
          return result;
        },
        function error() {
          modalService.show(modalService.modal.ERROR);
        }
      );
    };

    this.saveConfig = function (configName, isCollection, configs) {
      return dataService.saveConfigData(configName, isCollection, configs)
        .then(
          function success(){
            removeStoredConfigTree();
          },
          function error() {
            modalService.show(modalService.modal.ERROR);
          }
        );
    };

    this.deleteConfig = function (configName) {
      return dataService.deleteConfigData(configName).then(
        function success()  {
          removeStoredConfigTree();
        },
        function error() {
          modalService.show(modalService.modal.ERROR);
        }
      );
    };

    // HELPERS
    function loadCollectionItemTemplates(configName) {
      dataService.getConfigData(configName).then(
        function success(result){
          collectionItemTemplates[configName] = _.reject(result.data[0].properties, "skip");
        },
        function error() {
          modalService.show(modalService.modal.ERROR);
        }
      );
    }

    function plantConfigTree(data) {
      configTree = configTree || {};
      angular.forEach(data, function (config) {
        var configName = config.configName;
        if (configName && !configTree[configName]) {
          configTree[configName] = {};
          configTree[configName].configNameObject = config;
        }
      });
    }

    /**
     * TODO - refactor
     *
     * @param  {Array}   data
     * @param  {Object=} parent - configNameObject
     */
    function updateConfigTree(data, parent) {

      angular.forEach(data, function (config) {
        var branch;
        var configName = config.configName;
        var configNameObject = {};
        var isNestedConfig = angular.isObject(config.nestedConfig);
        var isNestedConfigCollection = angular.isObject(config.nestedConfigCollection);
        var properties = [];
        var children = [];

        if (isNestedConfig || isNestedConfigCollection) {
          configName = isNestedConfig ? config.nestedConfig.configName :
            config.nestedConfigCollection.configName;
        }

        if (configName && !configTree[configName]) {
          configTree[configName] = {};
        }

        branch = configTree[configName];

        if (branch && angular.isUndefined(branch.hasChildren)) {

          branch.configNameObject = branch.configNameObject || {};

          if (angular.isObject(parent)) {
            branch.parent = parent;
          }

          branch.configNameObject.breadcrumbs = buildBreadcrumbs(configName);

          if (angular.isArray(config.properties)) {
            properties = config.properties;
          }
          else if (isNestedConfig || isNestedConfigCollection) {
            branch.configNameObject.collection = isNestedConfigCollection;
            branch.configNameObject.configName = configName;
            branch.configNameObject.description = config.metadata.description;
            branch.configNameObject.label = config.metadata.label;

            if (isNestedConfig) {
              properties = config.nestedConfig.properties || [];
            }
          }

          children = _.filter(properties, function (property) {
            return angular.isObject(property.nestedConfig) ||
              angular.isObject(property.nestedConfigCollection);
            });

          if (children.length) {
            branch.hasChildren = true;
            updateConfigTree(children, branch.configNameObject);
          }
          else {
            branch.hasChildren = false;
          }
        }
      });
      setStoredConfigTree();
    }

    function setStoredConfigTree() {
      $window.localStorage.setItem(STORED_CONFIG_TREE, angular.toJson(configTree));
    }

    function getStoredConfigTree() {
      var storedConfigTree = angular.fromJson($window.localStorage.getItem(STORED_CONFIG_TREE));
      if (angular.isObject(storedConfigTree)) {
        return storedConfigTree;
      }
      return {};
    }

    function removeStoredConfigTree() {
      $window.localStorage.removeItem(STORED_CONFIG_TREE);
    }

    function buildBreadcrumbs(configName) {
      var branch = configTree[configName];
      var breadcrumbs = [];
      var parent;

      if (!branch || !branch.parent) {
        return breadcrumbs;
      }

      if (branch.configNameObject.breadcrumbs) {
        return branch.configNameObject.breadcrumbs;
      }

      parent = branch.parent;
      breadcrumbs.push(parent);
      breadcrumbs = buildBreadcrumbs(parent.configName).concat(breadcrumbs);

      return breadcrumbs;
    }
  }

})(angular, _);
