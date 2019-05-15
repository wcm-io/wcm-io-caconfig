/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
        property: "=",
        multivalue: "="
      },
      controller: PropertyDropdownController,
      replace: true,
      link: link
    };

    return directive;

    function link(scope, element) {
      var selectWidget,
        inheritanceWatch,
        effectiveValue,
        $dummyTagLists;
      var input = inputMap[scope.property.metadata.type];
      var inputType = input.type;

      scope.id = utilities.nextUid();
      scope.dropdownOptions = [];
      if (scope.property.metadata.properties && scope.property.metadata.properties.dropdownOptions) {
        scope.dropdownOptions = scope.property.metadata.properties.dropdownOptions;
      }
      scope.i18n = $rootScope.i18n;

      // If property is inherited, we need to set the value if inheritance is cleared.
      // This only needs to be done once.
      if (scope.property.inherited) {
        effectiveValue = scope.property.effectiveValue;

        inheritanceWatch = scope.$watch("property.inherited", function (isInherited, wasInherited) {
          if (isInherited === false && wasInherited === true) {
            setValue(selectWidget, effectiveValue);

            if ($dummyTagLists && $dummyTagLists.length) {
              $dummyTagLists.remove();
            }

            // remove the watch
            inheritanceWatch();
          }
        });
      }

      scope.$evalAsync(function () {
        scope.dropdownReady = true;
      });

      $timeout(function () {
        var $select = element.find("#" + scope.id);
        var optionValues,
          existingValues;

        selectWidget = uiService.addUI(uiService.component.SELECT, scope.property.name, {
          element: $select,
          multiple: scope.multivalue
        });

        // Add empty option for single value string properties.
        if (!scope.multivalue && inputType === "text") {
          selectWidget.addOption({
            value: "",
            display: ""
          }, 1);
        }

        // If value exists it should be preselected
        if (!scope.property.inherited && !scope.property.overridden
                  && angular.isDefined(scope.property.value)) {

          existingValues = angular.isArray(scope.property.value)
            ? scope.property.value.map(String)
            : [String(scope.property.value)];

          optionValues = selectWidget.getItems().map(function (item) {
            return item.getValue();
          });

          // We check if current value(s) are already in select options.
          // If they are not, we add them to the dropdown.
          angular.forEach(existingValues, function (existingValue) {
            if (optionValues.indexOf(existingValue) === -1) {
              selectWidget.addOption({
                value: existingValue,
                display: existingValue
              });
            }
          });

          setValue(selectWidget, scope.property.value);
        }
        // If multivalue values are inherited, we create a dummy tag list.
        else if (scope.multivalue) {
          $dummyTagLists = element.find(".caconfig-dummy-taglist");

          angular.forEach($dummyTagLists, function(dummyTagList, ix) {
            var useEffective = dummyTagList.classList.contains("caconfig-dummy-taglist--effective");
            var valuesToUse = useEffective ? scope.property.effectiveValue : scope.property.value;

            var values = angular.isUndefined(valuesToUse)
              ? []
              : valuesToUse.map(function(value) {
                return {
                  value: value,
                  display: scope.getOptionText(value)
                };
              });

            uiService.addUI(uiService.component.TAG_LIST, scope.property.name + "-dummy-" + ix, {
              element: dummyTagList,
              values: values
            });

            if (useEffective) {
              scope.property.effectiveValue = $rootScope.i18n("button.choose");
            }
          });
        }

        // Add change event listeners
        if (scope.multivalue) {
          $select.find(".coral-TagList").data("tagList")
            .on("itemadded", onChange)
            .on("itemremoved", onChange);
        }
        else {
          selectWidget.on("selected", onChange);
        }

        function onChange() {
          scope.property.value = getValue(selectWidget, inputType);

          if ($rootScope.configForm.$pristine) {
            $rootScope.configForm.$setDirty();
            scope.$digest();
          }
        }
      });
    }
  }

  /**
   * @param {CUI.Select} selectWidget
   * @param {string} inputType
   * @returns {Array|string|number}
   */
  function getValue(selectWidget, inputType) {
    var widgetValue = selectWidget.getValue();

    if (inputType !== "number") {
      return widgetValue;
    }

    return angular.isArray(widgetValue) ? widgetValue.map(Number) : Number(widgetValue);
  }

  /**
   *
   * @param {CUI.Select} selectWidget
   * @param {Array|string|number} value
   */
  function setValue(selectWidget, value) {
    selectWidget.setValue(angular.isArray(value) ? value.map(String) : String(value));
  }

  PropertyDropdownController.$inject = ["$scope"];

  function PropertyDropdownController($scope) {
    var optionDictionary = {};

    /**
     * @param {string|number|Array} optionValue
     * @returns {string|Array} optionText
     */
    $scope.getOptionText = function getOptionText (optionValue) {
      var optionText;

      if (angular.isUndefined(optionValue)) {
        return "";
      }

      if (angular.isDefined(optionDictionary[optionValue])) {
        return optionDictionary[optionValue];
      }

      if (angular.isArray(optionValue)) {
        optionText = optionValue.map(getOptionText);
      }
      else {
        angular.forEach($scope.dropdownOptions, function (option) {
          if (option.value === optionValue) {
            optionText = option.description;
            return;
          }
        });
      }

      if (angular.isUndefined(optionText)) {
        optionText = optionValue;
      }

      optionDictionary[optionValue] = optionText;

      return optionText;
    };
  }
}(angular));
