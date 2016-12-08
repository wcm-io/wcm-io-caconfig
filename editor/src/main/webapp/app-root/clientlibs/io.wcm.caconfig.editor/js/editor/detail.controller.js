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

  angular.module("io.wcm.caconfig.editor")
    .controller("DetailController", DetailController);

  DetailController.$inject = ["$rootScope", "$scope", "$route", "dataService"];

  function DetailController($rootScope, $scope, $route, dataService) {
    $scope.configs = [];
    $scope.configName = $route.current.params.configName;
    $scope.isCollection = angular.isString($route.current.params.isCollection) &&
      ($route.current.params.isCollection !== "");

    // If detail view is loaded directly via deeplink, bypassing overview
    if (!$rootScope.contextPath || !$rootScope.configNamesCollection.length) {
      $rootScope.getConfigNames()
        .then(function success() {
          updateTitle();
        });
    }
    else {
      updateTitle();
    }

    // Load Configuration Details
    dataService.getConfigData($scope.configName, $scope.isCollection).then(
      function success(result){
        $scope.configs = result.data;
      },
      function error() {
        $rootScope.errorModal.show();
      }
    );

    // Storage for collection property "schemas"
    if (!$rootScope.collectionProperties) {
      $rootScope.collectionProperties = {};
    }

    if ($scope.isCollection && !$rootScope.collectionProperties[$scope.configName]) {
      dataService.getConfigData($scope.configName).then(
        function success(result){
          $rootScope.collectionProperties[$scope.configName] = result.data[0].properties;
        },
        function error() {
          $rootScope.errorModal.show();
        }
      );
    }

    $scope.save = function() {
      dataService.saveConfigData($scope.configName, $scope.isCollection, $scope.configs)
        .then(
          function success() {
            $rootScope.go();
          },
          function error() {
            $scope.errorModal.show();
          }
        );
    };

    $scope.addCollectionItem = function() {
      $scope.addCollectionItemModal.show();
    }

    $rootScope.addItem = function() {
      var configName = $scope.configName;
      $scope.configs.push({
        collectionItemName: $("#caconfig-collectionItemName").val(),
        configName: configName,
        properties: angular.copy($rootScope.collectionProperties[configName])
      });
      $("#caconfig-collectionItemName").val("");
    }

    $scope.removeConfig = function() {
      $rootScope.deleteModal.show();
    }

    $scope.removeCollectionItem = function(index) {
      $scope.configs.splice(index, 1);
    }

    $rootScope.deleteConfig = function() {
      var configName = $scope.configName;
      dataService.deleteConfigData(configName).then(
        function success()  {
          $rootScope.go();
        },
        function error() {
          $scope.errorModal.show();
        }
      );
    };

    function updateTitle() {
      $scope.configLabel = dataService.getConfigLabel($scope.configName, $rootScope.configNamesCollection);
      $rootScope.title = $rootScope.i18n.title + ": " + $scope.configLabel;
    }
  };
})(angular);
