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
   * Controller for details view
   * (for singletons, collections and nested configs/collection)
   */
  angular.module("io.wcm.caconfig.editor")
    .controller("DetailController", DetailController);

  DetailController.$inject = ["$rootScope", "$scope", "$route", "configService", "currentConfigService", "modalService"];

  function DetailController($rootScope, $scope, $route, configService, currentConfigService, modalService) {
    $scope.configName = $route.current.params.configName;
    $scope.configs = [];

    // If detail view was loaded directly via deeplink, we need to first loadConfigNames
    if (!configService.getContextPath() || !configService.getConfigNames().length) {
      configService.loadConfigNames()
        .then(function success() {
          init();
        });
    }
    else {
      init();
    }

    $rootScope.saveWarning = function (redirectUrl) {
      $rootScope.redirectUrl = null;
      if (angular.isString(redirectUrl)) {
        $rootScope.redirectUrl = redirectUrl;
      }
      modalService.show(modalService.modal.SAVE_CONFIG);
    };

    $scope.saveConfig = function () {
      if ($scope.configs.length === 0) {
        $scope.removeConfig();
      }
      else {
        configService.saveCurrentConfig()
          .then(function (redirect) {
            if (redirect) {
              $rootScope.go(redirect.configName || "");
            }
            else {
              $rootScope.go($scope.parent ? $scope.parent.configName : "");
            }
          });
      }
    };

    $rootScope.deleteConfig = function () {
      configService.deleteCurrentConfig()
        .then(function (redirect) {
          if (redirect) {
            $rootScope.go(redirect.configName || "");
          }
          else {
            $rootScope.go($scope.parent ? $scope.parent.configName : "");
          }
        });
    };

    $scope.addCollectionItem = function () {
      modalService.show(modalService.modal.ADD_COLLECTION_ITEM);
    };

    $scope.removeConfig = function () {
      modalService.show(modalService.modal.DELETE_CONFIG);
    };

    $scope.removeCollectionItem = function (index) {
      currentConfigService.removeItemFromCurrentCollection(index);
    };

    /**
     * Loads config data and sets various $scope properties
     */
    function init() {
      // Load Configuration Details
      configService.loadConfig($scope.configName)
        .then(function (current) {
          $scope.configs = current.configs;
          $scope.originalLength = current.configs.length;
          $scope.isCollection = current.isCollection;
          $scope.isNewCollection = current.isCollection && current.configs.length === 0;
          $scope.label = current.configNameObject.label || $scope.configName;
          $scope.breadcrumbs = current.configNameObject.breadcrumbs || [];
          $scope.parent = $scope.breadcrumbs[$scope.breadcrumbs.length - 1];
          $scope.description = current.configNameObject.description;
          $scope.contextPath = configService.getContextPath();
          $rootScope.title = $rootScope.i18n.title + ": " + $scope.label;
        });
    }
  }
}(angular));
