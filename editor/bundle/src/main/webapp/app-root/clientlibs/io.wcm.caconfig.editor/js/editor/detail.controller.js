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
   * Controller for details view
   * (for singletons, collections and nested configs/collection)
   */
  angular.module("io.wcm.caconfig.editor")
    .controller("DetailController", DetailController);

  DetailController.$inject = ["$rootScope", "$scope", "$route", "configService", "modalService"];

  function DetailController($rootScope, $scope, $route, configService, modalService) {
    $scope.configName = $route.current.params.configName;
    $scope.configs = [];

    // If detail view was loaded directly via deeplink, we need to first loadConfigNames
    if (!configService.getContextPath() || !configService.getConfigNames().length) {
      configService.loadConfigNames()
        .then(function success() {
          init($scope.configName);
        });
    }
    else {
      init($scope.configName);
    }

    $scope.save = function () {
      if ($scope.configs.length === 0) {
        $scope.removeConfig();
      }
      else {
        configService.saveConfig($scope.configName, $scope.isCollection, $scope.configs)
          .then(function () {
            $rootScope.go();
          });
      }
    };

    $scope.addCollectionItem = function () {
      modalService.show(modalService.modal.ADD_COLLECTION_ITEM);
    }

    $rootScope.getCollectionItemNames = function () {
      return _.map($scope.configs, "collectionItemName");
    }

    $rootScope.addItem = function () {
      var configName = $scope.configName;
      $scope.configs.push({
        collectionItemName: $("#caconfig-collectionItemName").val().trim(),
        configName: configName,
        properties: configService.getCollectionItemTemplate(configName)
      });
    }

    $scope.removeConfig = function () {
      modalService.show(modalService.modal.DELETE_CONFIG);
    }

    $scope.removeCollectionItem = function (index) {
      $scope.configs.splice(index, 1);
    }

    $rootScope.deleteConfig = function () {
      var configName = $scope.configName;
      configService.deleteConfig(configName)
        .then(function () {
          $rootScope.go();
        });
    };

    /**
     * Sets various $scope properties and loads config data
     */
    function init() {
      var configNameObject = configService.getConfigNameObject($scope.configName);

      if (!configNameObject) {
        modalService.show(modalService.modal.ERROR);
        $rootScope.go();
        return;
      }

      $scope.isCollection = !!configNameObject.collection;
      $scope.label = configNameObject.label || $scope.configName;
      $scope.breadcrumbs = configNameObject.breadcrumbs || [];
      $rootScope.title = $rootScope.i18n.title + ": " + $scope.label;
      $scope.description = configNameObject.description;
      $scope.contextPath = configService.getContextPath();

      // Load Configuration Details
      configService.loadConfig($scope.configName, $scope.isCollection)
        .then(function (result) {
          $scope.configs = result.data;
        });
    }
  };
})(angular, _);
