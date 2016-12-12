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
  angular.module("io.wcm.caconfig.widgets")
      .directive("caconfigMultifield", multifield);

  multifield.$inject = ["templateUrlList", "inputMap"];

  function multifield(templateList, inputMap) {

    function link(scope, element, attr) {
      var input = inputMap[scope.parameter.metadata.type];
      scope.type = input.type;
      scope.pattern = input.pattern;
      scope.required = input.required;
      scope.values = [];
      if (scope.parameter.value && scope.parameter.value.length > 0) {
        var values = scope.parameter.value;
        for (var i = 0; i < values.length; i++) {
          scope.values.push({value: values[i]});
        }
      }
      scope.$watch("values", function() {
        var values = _.map(scope.values, "value");
        scope.parameter.value = values;
      }, true);
    }

    return {
      restrict: "E",
      replace: true,
      templateUrl: templateList.multifield,
      scope: {
        parameter: "="
      },
      controller: MultifieldController,
      link: link
    }
  }

  MultifieldController.$inject = ["$scope"];

  function MultifieldController($scope) {
    $scope.addNewValue = function(index) {
      $scope.$evalAsync(function() {
        $scope.values.splice(index + 1, 0, { value: undefined });
      });
    };
    $scope.removeValue = function(index) {
      $scope.values.splice(index, 1);
    };
  }
})(angular, _);
