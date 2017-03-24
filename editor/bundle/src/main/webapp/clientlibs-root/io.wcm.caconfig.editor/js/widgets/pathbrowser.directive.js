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
      .directive("caconfigPathbrowser", pathbrowser);

  pathbrowser.$inject = ["templateUrlList", "inputMap"];

  function pathbrowser(templateList, inputMap) {
    var directive = {
      restrict: "E",
      replace: true,
      require: "^form",
      templateUrl: templateList.pathbrowser,
      scope: {
        parameter: "="
      },
      controller: PathbrowserController,
      link: link
    };

    return directive;

    function link(scope, element, attr, form) {
      var input = inputMap[scope.parameter.metadata.type];
      var inheritedStateChanged = false;

      scope.type = input.type;
      scope.pattern = input.pattern;

      scope.$watch("parameter.inherited", function (isInherited, wasInherited) {
        if (isInherited === wasInherited) {
          return;
        }

        inheritedStateChanged = true;
        form.$setDirty();
      });
    }
  }

  PathbrowserController.$inject = ["$scope"];

  function PathbrowserController($scope) {
    $scope.choosePath = function () {
      /*
      $scope.$evalAsync(function () {
        $scope.values.splice(index + 1, 0, { value: undefined });
      });
      */
    };
  }
}(angular, _));
