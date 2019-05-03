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
   * Directive for text field input, used for string and numeric properties.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPropertyDropdown", propertyDropdown);

  propertyDropdown.$inject = ["$rootScope", "$timeout", "templateUrlList", "inputMap", "utilities", "uiService"];

  function propertyDropdown($rootScope, $timeout, templateList, inputMap, utilities, uiService) {
    var directive = {
      templateUrl: templateList.propertyDropdown,
      scope: {
        property: "="
      },
      controller: PropertyDropdownController,
      replace: true,
      link: link
    };

    return directive;

    function link(scope, element) {
      var selectWidget,
        inheritanceWatch;
      var input = inputMap[scope.property.metadata.type];
      var inputType = input.type;

      scope.id = utilities.nextUid();
      scope.dropdownOptions = scope.property.metadata.properties.dropdownOptions;
      scope.i18n = $rootScope.i18n;

      // If property is inherited, we need to set the value if inheritance is cleared.
      // This only needs to be done once.
      if (scope.property.inherited) {
        inheritanceWatch = scope.$watch("property.inherited", function (isInherited, wasInherited) {
          if (isInherited === false && wasInherited === true) {
            selectWidget.setValue(String(scope.property.effectiveValue));

            // remove the watch
            inheritanceWatch();
          }
        });
      }

      scope.$evalAsync(function () {
        scope.dropdownReady = true;
      });


      $timeout(function() {
        var $select = element.find("#" + scope.id);

        selectWidget = uiService.addUI(uiService.component.SELECT, scope.property.name, {
          element: $select
        });

        selectWidget.on("selected", function() {
          scope.property.value = inputType === "number" ? Number(selectWidget.getValue()) : selectWidget.getValue();
          $rootScope.configForm.$setDirty();
          scope.$digest();
        });

        if (!scope.property.inherited && !scope.property.overridden
                                      && angular.isDefined(scope.property.value)) {
          selectWidget.setValue(String(scope.property.value));
        }
      });
    }
  }

  PropertyDropdownController.$inject = ["$scope"];

  function PropertyDropdownController($scope) {
    var optionDictionary = {};

    $scope.getOptionText = function (optionValue) {
      var optionText;

      if (angular.isUndefined(optionValue)) {
        return "";
      }

      if (angular.isDefined(optionDictionary[optionValue])) {
        return optionDictionary[optionValue];
      }

      angular.forEach($scope.dropdownOptions, function (option) {
        if (option.value === optionValue) {
          optionText = option.description;
          return;
        }
      });

      if (angular.isUndefined(optionText)) {
        optionText = optionValue;
      }

      optionDictionary[optionValue] = optionText;

      return optionText;
    };
  }
}(angular));
