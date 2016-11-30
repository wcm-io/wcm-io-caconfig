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

  DetailController.$inject = ["$rootScope", "$scope", "$route", "dataService", "utilities"];

  function DetailController($rootScope, $scope, $route, dataService, utilities) {
      $rootScope.toBeDeleted = [];
      $scope.configs = [];
      $scope.configName = $route.current.params.configName;
      $scope.isCollection = angular.isString($route.current.params.isCollection) &&
        ($route.current.params.isCollection !== "");

      if ($scope.configName && $scope.configName.length) {
        $rootScope.title = $rootScope.i18n.title + ": " + $scope.configName;
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

      $scope.persist = function() {
        if ($scope.toBeDeleted.length) {
          $scope.deleteModal.show();
        }
        else {
          save();
        }
      };

      $scope.addCollectionItem = function() {
        $scope.addCollectionItemModal.show();
      }

      $rootScope.addItem = function() {
        $scope.configs.push({
          collectionItemName: $("#caconfig-collectionItemName").val(),
          configName: $scope.configName
        });
        $scope.persist();
      }

      $rootScope.persistWithDeletion = function(toBeDeleted) {
        if (toBeDeleted.length && !$scope.configs.length) {
          dataService.deleteConfig($scope.configName).then(
            function success()  {
              removeConfigName(toBeDeleted[0]);
              $rootScope.toBeDeleted = [];
              $rootScope.go();
            },
            function error() {
              $scope.errorModal.show();
            }
          );
        }
        else {
          save();
        }
      };

      $scope.remove = function(config, isCollection) {
        if (isCollection) {
          var indexOfItem = utilities.indexOfMatchingObject($scope.configs, config, "collectionItemName");
          if (indexOfItem !== -1) {
            $scope.configs.splice(indexOfItem, 1);
            $rootScope.toBeDeleted.push(config);
          }
          else {
            $scope.errorModal.show();
          }
        }
        else {
          $scope.configs = [];
          $rootScope.toBeDeleted = [ config ];
        }
      }

      function save() {
        dataService.saveConfigData($scope.configName, $scope.isCollection, $scope.configs)
          .then(
            function success()  {
              $rootScope.toBeDeleted = [];
              $scope.successModal.show();
            },
            function error() {
              $scope.errorModal.show();
            }
          );
      }

      function removeConfigName(config) {
        var indexOfConfig = utilities.indexOfMatchingObject($rootScope.configNamesCollection, config, "configName");
        if (indexOfConfig !== -1) {
          $rootScope.configNamesCollection[indexOfConfig].exists = false;
        }
      }
    };
})(angular);
