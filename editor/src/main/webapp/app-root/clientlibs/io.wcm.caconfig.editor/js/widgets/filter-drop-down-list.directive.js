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
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigFilterDropDownList", filterDropDownList);

    filterDropDownList.$inject = ["templateUrlList"];

    function filterDropDownList(templateList) {

      function link(scope, element, attr) {
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

      return {
        restrict: "E",
        replace: true,
        scope: {
          model: "=",
          currentFilter: "="
        },
        templateUrl: templateList.filterDropDownList,
        link: link
      };
    }
})(angular);