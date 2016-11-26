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
  angular.module('io.wcm.caconfig.editor', ['io.wcm.caconfig.services', 'io.wcm.caconfig.directives'])
    .run(["$rootScope", "parameters", function($rootScope, Parameters) {
      $rootScope.addModal = new CUI.Modal({ element:'#addModal', visible: false });
      $rootScope.confirmModal = new CUI.Modal({ element:'#confirmModal', visible: false });
      $rootScope.errorModal = new CUI.Modal({ element:'#errorModal', type: "error", visible: false });
      
      /**
       * Use the parameters service to load configuration names
       */
      Parameters.loadConfigNames().then(
        function success(result) {
          $rootScope.configNamesCollection = result.data;
        },
        function error() {
          $rootScope.errorModal.show();
        }
      );
      
      /*
      Parameters.loadParameters().then(
        function success(result){
          var parsedData = Parameters.parseData(result.data);
          $rootScope.$evalAsync(function() {
            $rootScope.filters = parsedData.filters;
            $rootScope.parameterCollection = parsedData.parameters;
          });
        },
        function error() {
          $rootScope.errorModal.show();
        }
      );
      */

    }])
    .controller("mainCtrl", ['$scope', "$filter", "parameters", function($scope, $filter, Parameters) {
      $scope.currentFilter = {};
      $scope.displayedCollection = [];

      $scope.hasNonExistingConfig = function() {
        if (!$scope.configNamesCollection) {
          return false;
        }
        for (var configName in $scope.configNamesCollection) {
          if (!configName.exists) {
            return true;
          }
        }
        return false;
      };

      $scope.addConfig = function() {
        $scope.addModal.show();
      };

      $scope.save = function() {
        Parameters.saveParameters($scope.parameterCollection).then(
          function success()  {
            $scope.confirmModal.show();
          },
          function error() {
            $scope.errorModal.show();
          }
        );
      };

      /**
       * Filters the shown parameters based on the currently selected filtered.
       * Triggered when the $scope.currentFilter is modified
       * @param newValue
       * @param oldValue
       */
      function filterDisplayedParameters(newValue, oldValue) {

        var filteredParameters = $scope.parameterCollection;

        for (var propertyName in $scope.currentFilter) {
          if ($scope.currentFilter.hasOwnProperty(propertyName)) {
            var filterValues = $scope.currentFilter[propertyName];

            if (filterValues && filterValues.length > 0) {
              var isVisible = function(parameter, index) {
                var value = parameter[propertyName];
                return filterValues.indexOf(value) !== -1;
              };
              filteredParameters = $filter("filter")(filteredParameters, isVisible);
            }
          }
        }
        $scope.displayedCollection = filteredParameters;
      }

      $scope.$watch('currentFilter', filterDisplayedParameters, true);
      $scope.$watch('parameterCollection', filterDisplayedParameters, true);

    }]);
})(angular);
