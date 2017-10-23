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

  multifield.$inject = ["templateUrlList", "inputMap", "$rootScope"];

  function multifield(templateList, inputMap, $rootScope) {

    var directive = {
      replace: true,
      templateUrl: templateList.multifield,
      scope: {
        property: "="
      },
      controller: MultifieldController,
      link: link
    };

    return directive;

    function link(scope) {
      var input = inputMap[scope.property.metadata.type];
      var inheritedStateChanged = false;

      scope.type = input.type;
      scope.pattern = input.pattern;
      scope.effectiveValues = [];
      scope.values = [];

      setValueArray(scope.property.effectiveValue, scope.effectiveValues);
      setValueArray(scope.property.value, scope.values);

      scope.$watch("values", function (newValues, oldValues) {
        var valueArray = _.map(newValues, function(newValue) {
          return scope.type === "checkbox" ? Boolean(newValue.value) : newValue.value;
        });
        scope.property.value = valueArray;
        if (newValues.length !== oldValues.length) {
          $rootScope.configForm.$setDirty();
        }
      }, true);

      scope.$watch("property.inherited", function (isInherited, wasInherited) {
        var effectiveValueArray,
            valueArray;

        if (isInherited === wasInherited) {
          return;
        }

        valueArray = _.map(scope.values, "value");

        if (!inheritedStateChanged
            && isInherited === false
            && valueArray.length === 0) {
          effectiveValueArray = _.map(scope.effectiveValues, "value");
          setValueArray(effectiveValueArray, scope.values);
        }
        else if (isInherited === true) {
          scope.effectiveValues = [{
            value: String(scope.property.effectiveValue)
          }];
        }

        inheritedStateChanged = true;
        $rootScope.configForm.$setDirty();
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
