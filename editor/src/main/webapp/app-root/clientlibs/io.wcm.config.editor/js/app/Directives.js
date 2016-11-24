/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
  angular.module('io.wcm.config.directives', ["io.wcm.config.templateUrlList", "io.wcm.config.utilities", "io.wcm.config.templates"])
  /**
   * Directive for displaying the filters on the editor page. Wraps internally the CUI.Select widget.
   * Parent Scope provides the model for the filter:
   * {
   *   name: "Filter Application",
   *   filterParameter:"application",
   *   options: [
   *     {
   *      "value": "/apps/viessmann",
   *      "label": "Viessmann Responsive"
   *     }
   *   ]
   * }
   *
   * filterParameter specifies the property name of the parameter, which will be used to apply the filtering
   * Every change of the selection is applied to the "currentFilter" property from the parent scope. The parent scope has a
   * watcher registered to this property and filters the displayed collection of the parameter n every change
   */
    .directive("filterDropDownList", ['templateUrlList', function (templateList) {
      return {
        restrict: "E",
        replace: true,
        scope: {
          model: "=",
          currentFilter: "="
        },
        templateUrl: templateList.filterDropDownList,
        link: function (scope, element, attr) {
          var widget;

          function handleSelectionChange() {
            scope.$apply(function() {
              scope.currentFilter[scope.model.filterParameter] = widget.getValue()
            });
          }

          function initWidget () {
            widget = new CUI.Select({ element: element[0] });
            widget.on("selected", handleSelectionChange);
            widget.on("itemremoved", handleSelectionChange);
          }

          scope.$evalAsync(initWidget);
        }
      }
    }])
  /**
   * Directive to render the "i" button with a popover for the decription of the parameter. Wraps a coral UI Button and
   * CUI.Popover widget. This directive transcludes the popup content elements. Example:
   * <description-popup>
   *   <popup-content>
   *     Description Text
   *   </popup-content>
   * </description-popup>
   *
   */
    .directive("descriptionPopup", ['templateUrlList', "EditorUtilities", function (templateList, utils) {
      return {
        restrict: "E",
        replace: true,
        templateUrl: templateList.popupContainer,
        transclude: true,
        link: function(scope, element, attr) {
          var widget;

          scope.id = utils.nextUid();
          scope.$evalAsync(function () {
            widget = new CUI.Popover({element: $("coral-Popover", element)});
          });
        }
      }
    }])
  /**
   * Renders the wrapping elements for the coral ui popover. The content itself is transcluded. The content can also contain markup
   *   <popup-content>
   *     Description Text
   *   </popup-content>
   *
   */
    .directive("popupContent", ['templateUrlList', function (templateList) {
      return {
        restrict: "E",
        replace: true,
        transclude: true,
        templateUrl: templateList.popupContent
      }
    }])
  /**
   * General directive for the widget column to edit the parameters value.
   */
    .directive("parameterValue", ['templateUrlList', "EditorUtilities", function (templateList, utils) {
      return {
        restrict: "A",
        replace: false,
        require: '^form',
        templateUrl: templateList.parameterValue,
        scope: {
          parameter: '=parameterValue',
          type: '@widgetType'
        },
        link: function(scope, element, attr, ctrl) {
          scope.originalType = scope.type;
          scope.newValue = scope.parameter.value;

          function getDisabledType() {
            if (scope.originalType === "checkbox") {
              return "disabledCheckbox";
            } else {
              return "disabled";
            }
          }
          scope.$watch("parameter.inherited", function(newvalue, oldvalue){
            if (newvalue === true || (scope.parameter.locked === true && scope.parameter.lockedInherited === true)) {
              scope.type = getDisabledType();
              scope.newValue = scope.parameter.value;
              scope.parameter.value = scope.parameter.inheritedValue;
            } else {
              scope.type = scope.originalType;
              scope.parameter.value = scope.newValue;
            }
          });
        }
      }
    }])
    .directive("multifield", ['templateUrlList', "EditorUtilities", function (templateList, utils) {
      return {
        restrict: "E",
        replace: true,
        templateUrl: templateList.textMultifield,
        scope: {
          parameter: '='
        },
        controller: ["$scope", function($scope) {
          $scope.addNewValue = function(value) {
            $scope.$evalAsync(function() {
              var indexOf = utils.indexOfValueObject($scope.values, value);
              $scope.values.splice(indexOf+1, 0, {value: ""});
            });
          };
          $scope.removeValue = function(value) {
            var indexOf = utils.indexOfValueObject($scope.values, value);
            $scope.values.splice(indexOf, 1);
            if ($scope.values.length == 0) {
              $scope.values.push({value: ""});
            }
          };
        }],
        link: function (scope, element, attr) {
          scope.values = [];
          if (scope.parameter.value && scope.parameter.value.length > 0) {
            var stringValues = scope.parameter.value;
            for (var i = 0; i < stringValues.length; i++) {
              scope.values.push({value: stringValues[i]});
            }
          } else {
            scope.values.push({value: ""});
          }
          scope.$watch('values', function() {
            var stringValues = _.pluck(scope.values, "value");
            scope.parameter.value = stringValues;
          }, true);
        }
      }
    }])
    .directive("map", ['templateUrlList', "EditorUtilities", function (templateList, utils) {
      return {
        restrict: "E",
        replace: true,
        templateUrl: templateList.map,
        scope: {
          parameter: '='
        },
        controller: ["$scope", function($scope) {
          $scope.addNewValue = function(value) {
            $scope.$evalAsync(function() {
              var indexOf = utils.indexOfValueObject($scope.values, value);
              $scope.values.splice(indexOf+1, 0, {key:"", value: ""});
            });
          };
          $scope.removeValue = function(value) {
            var indexOf = utils.indexOfValueObject($scope.values, value);
            $scope.values.splice(indexOf, 1);
            if ($scope.values.length == 0) {
              $scope.values.push({key: "", value: ""});
            }
          };
        }],
        link: function (scope, element, attr) {
          scope.values = [];
          if (scope.parameter.value && scope.parameter.value.length > 0) {
            scope.values = scope.parameter.value;
          } else {
            scope.values.push({key:"", value: ""});
          }
          scope.$watch('values', function() {
            scope.parameter.value = scope.values;
          }, true);
        }
      }
    }])
  /**
   * Path browser directive to allow select pages from the siteadmin. Utilizes the CUI.PathBrowser
   */
    .directive("pathBrowser",['templateUrlList', "EditorUtilities", function (templateList, utils) {
      return {
        restrict: "E",
        replace: true,
        templateUrl: templateList.pathBrowser,
        scope: {
          parameter: '=',
          rootPath: "@"
        },
        link: function (scope, element, attr) {
          var widget = new CUI.PathBrowser({element: element,
            optionLoader: utils.loadAutocompleteOptions,
            rootPath: scope.rootPath
          });

          // apply value changes to the model
          widget.dropdownList.on("selected", function() {
            scope.$apply(function() {
              scope.parameter.value = widget.inputElement.val()
            });
          });

          scope.$on("$destroy", function(){
            // remove listeners
            widget.off();
          })
        }
      }
    }]);
})(angular);
