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
   * General directive for the widget column to edit the parameters value.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigParameterValue", parameterValue);

  parameterValue.$inject = ["templateUrlList", "inputMap", "$rootScope"];

  function parameterValue(templateList, inputMap, $rootScope) {

    function link(scope, element, attr, ctrl) {
      var input;

      scope.go = $rootScope.go;
      scope.i18n = $rootScope.i18n;

      if (scope.parameter.nestedConfig) {
        scope.type = "nestedConfig";
      }
      else if (scope.parameter.nestedConfigCollection) {
        scope.type = "nestedConfigCollection";
      }
      else if (scope.parameter.metadata && scope.parameter.metadata.multivalue) {
        scope.type = "multivalue";
      }
      else if (scope.parameter.metadata && scope.parameter.metadata.type) {
        input = inputMap[scope.parameter.metadata.type];
        scope.type = input.type || scope.parameter.metadata.type;
        scope.pattern = input.pattern || /^.*$/;
        scope.required = input.required;
      }
      else {
        scope.type = null;
      }
    }

    return {
      restrict: "A",
      replace: false,
      require: "^form",
      templateUrl: templateList.parameterValue,
      scope: {
        parameter: "=caconfigParameterValue"
      },
      link: link
    }
  }
})(angular);
