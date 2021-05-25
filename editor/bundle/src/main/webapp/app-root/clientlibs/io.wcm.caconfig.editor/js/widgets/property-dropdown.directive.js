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
(function (angular, Coral) {
  "use strict";

  /**
   * Directive for text field input, used for string and numeric properties.
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPropertyDropdown", propertyDropdown);

  propertyDropdown.$inject = ["$rootScope", "$timeout", "templateUrlList", "inputMap"];

  function propertyDropdown($rootScope, $timeout, templateList, inputMap) {
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

      scope.id = Coral.commons.getUID();

      scope.dropdownOptions = [];
      if (scope.property.metadata.properties && scope.property.metadata.properties.dropdownOptions) {
        scope.dropdownOptions = scope.property.metadata.properties.dropdownOptions;
      }

      // if single-selection add blank option as first option
      if (!scope.multivalue) {
        scope.dropdownOptions.unshift({
          value: "",
          description: ""
        });
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

        selectWidget = $select[0];

        Coral.commons.ready(selectWidget, function() {
          selectWidget.multiple = scope.multivalue;

          // We ensure any existing values are in the dropdown
          addValues(selectWidget, scope.property.value);

          if (scope.property.inherited) {
            addValues(selectWidget, scope.property.effectiveValue);
          }

          // Non-inherited/overridden existing values should be preselected
          if (angular.isDefined(scope.property.value) && !scope.property.inherited && !scope.property.overridden) {
            setValue(selectWidget, scope.property.value);
          }

          // If multivalue values are inherited/overridden, we create a dummy tag list
          if (scope.multivalue && (scope.property.inherited || scope.property.overridden)) {
            $dummyTagLists = element.find(".caconfig-dummy-taglist");

            angular.forEach($dummyTagLists, function(dummyTagList) {
              var useEffective = dummyTagList.classList.contains("caconfig-dummy-taglist--effective");
              var valuesToUse = useEffective ? scope.property.effectiveValue : scope.property.value;

              if (!angular.isUndefined(valuesToUse)) {
                valuesToUse.forEach(function(value) {
                  var tag = new Coral.Tag().set({
                    value: value,
                    label: {
                      innerHTML: scope.getOptionText(value)
                    }
                  });
                  dummyTagList.items.add(tag);
                });
              }

              if (useEffective) {
                scope.property.effectiveValue = $rootScope.i18n("button.choose");
                dummyTagList.previousElementSibling.classList.add("is-placeholder");
                scope.$digest();
              }
            });
          }

          // Add change event listen
          selectWidget.on("change", function onChange() {
            scope.property.value = getValue(selectWidget, inputType);

            if ($rootScope.configForm.$pristine) {
              $rootScope.configForm.$setDirty();
              scope.$digest();
            }
          });
        });
      });
    }
  }

  /**
   * @param {Coral.Select} selectWidget
   * @param {string} inputType
   * @returns {Array|string|number}
   */
  function getValue(selectWidget, inputType) {
    var widgetValues = selectWidget.values;
    var singleValue = widgetValues[0];

    if (selectWidget.multiple) {
      return inputType === "number" ? widgetValues.map(Number) : widgetValues;
    }

    if (widgetValues.length === 0) {
      return null;
    }

    if (inputType !== "number") {
      return singleValue;
    }

    return singleValue === "" ? null : Number(singleValue);
  }

  /**
   *
   * @param {Coral.Select} selectWidget
   * @param {Array|string|number} value
   */
  function setValue(selectWidget, value) {
    var newValues = angular.isArray(value) ? value.map(String) : [String(value)];

    selectWidget.items.getAll().forEach(function(item) {
      item.selected = newValues.indexOf(item.value) > -1;
    });
  }

  /**
   * @param {Coral.Select} selectWidget
   * @param {Array|string|number} values
   */
  function addValues(selectWidget, values) {
    var existingValues,
      optionValues;

    if (angular.isUndefined(values)) {
      return;
    }

    existingValues = angular.isArray(values)
      ? values.map(String)
      : [String(values)];

    optionValues = selectWidget.items.getAll().map(function (item) {
      return item.value;
    });

    // We check if current value(s) are already in select options.
    // If they are not, we add them to the dropdown.
    angular.forEach(existingValues, function (existingValue) {
      if (optionValues.indexOf(existingValue) === -1) {
        selectWidget.items.add({
          value: existingValue,
          content: {
            textContent: existingValue
          }
        });
      }
    });
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
}(angular, Coral));
