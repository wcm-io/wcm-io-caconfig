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

  function map(templateList) {

    function link(scope, element, attr, form) {
      var originalLength;
      scope.values = [];
      if (scope.parameter.value && scope.parameter.value.length > 0) {
        scope.values = scope.parameter.value;
      }
      originalLength = scope.values.length;
      scope.$watch("values", function () {
        scope.parameter.value = scope.values;
        if (values.length !== originalLength) {
          form.$setDirty();
        }
      }, true);
    }

    return {
      restrict: "E",
      replace: true,
      require: "^form",
      templateUrl: templateList.map,
      scope: {
        parameter: "="
      },
      controller: MapController,
      link: link
    };
  }

  MapController.$inject = ["$scope"];

  function MapController($scope) {
    $scope.addNewValue = function (index) {
      $scope.$evalAsync(function () {
        $scope.values.splice(index + 1, 0, { key: "", value: "" });
      });
    };
    $scope.removeValue = function (index) {
      $scope.values.splice(index, 1);
    };
  }
})(angular);
