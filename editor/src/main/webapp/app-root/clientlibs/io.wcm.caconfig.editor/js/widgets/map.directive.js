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

  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigMap", map);

  map.$inject = ["templateUrlList"];

  function map(templateList, utilities) {

    function link(scope, element, attr) {
      scope.values = [];
      if (scope.parameter.value && scope.parameter.value.length > 0) {
        scope.values = scope.parameter.value;
      } else {
        scope.values.push({key:"", value: ""});
      }
      scope.$watch("values", function() {
        scope.parameter.value = scope.values;
      }, true);
    }

    return {
      restrict: "E",
      replace: true,
      templateUrl: templateList.map,
      scope: {
        parameter: "="
      },
      controller: MapController,
      link: link
    };
  }

  MapController.$inject = ["$scope", "utilities"];

  function MapController($scope, utilities) {
    $scope.addNewValue = function(value) {
      $scope.$evalAsync(function() {
        var indexOf = utilities.indexOfValueObject($scope.values, value);
        $scope.values.splice(indexOf+1, 0, {key:"", value: ""});
      });
    };
    $scope.removeValue = function(value) {
      var indexOf = utilities.indexOfValueObject($scope.values, value);
      $scope.values.splice(indexOf, 1);
      if ($scope.values.length === 0) {
        $scope.values.push({key: "", value: ""});
      }
    };
  }
})(angular);
