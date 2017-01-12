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

    function link(scope, element, attr, form) {
      var input = inputMap[scope.parameter.metadata.type];
      var inheritedStateChanged = false;

      scope.type = input.type;
      scope.pattern = input.pattern;
      scope.effectiveValues = [];
      scope.values = [];

      setValueArray(scope.parameter.effectiveValue, scope.effectiveValues);
      setValueArray(scope.parameter.value, scope.values);

      scope.$watch("values", function (newValues, oldValues) {
        var valueArray = _.map(newValues, "value");
        scope.parameter.value = valueArray;
        if (newValues.length !== oldValues.length) {
          form.$setDirty();
        }
      }, true);

      scope.$watch("parameter.inherited", function (isInherited, wasInherited) {
        var effectiveValueArray,
            valueArray;

        if (isInherited === wasInherited) {
          return;
        }

        valueArray = _.map(scope.values, "value");

        if (!inheritedStateChanged && isInherited === false && valueArray.length === 0) {
          effectiveValueArray = _.map(scope.effectiveValues, "value");
          valueArray = effectiveValueArray;
          setValueArray(valueArray, scope.values);
        }
        else if (isInherited === true) {
          scope.effectiveValues = [{
            value: String(scope.parameter.effectiveValue)
          }];
        }

        inheritedStateChanged = true;
        form.$setDirty();
      });
    }

    function setValueArray(src, target) {
      var i,
          tempArray;
      if (src && src.length > 0) {
        tempArray = src;
        for (i = 0; i < tempArray.length; i++) {
          target.push({value: tempArray[i]});
        }
      }
      else {
        target = [];
      }
    }

    return {
      restrict: "E",
      replace: true,
      require: "^form",
      templateUrl: templateList.multifield,
      scope: {
        parameter: "="
      },
      controller: MultifieldController,
      link: link
    };
  }

  MultifieldController.$inject = ["$scope"];

  function MultifieldController($scope) {
    $scope.addNewValue = function (index) {
      $scope.$evalAsync(function () {
        $scope.values.splice(index + 1, 0, { value: undefined });
      });
    };
    $scope.removeValue = function (index) {
      $scope.values.splice(index, 1);
    };
  }
}(angular, _));
