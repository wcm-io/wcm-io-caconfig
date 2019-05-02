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

  propertyDropdown.$inject = ["$rootScope", "$timeout", "templateUrlList", "inputMap",  "utilities", "uiService"];

  function propertyDropdown($rootScope, $timeout, templateList, inputMap, utilities, uiService) {
    var directive = {
      templateUrl: templateList.propertyDropdown,
      scope: {
        property: "="
      },
      replace: true,
      link: link,
      transclude: true
    };

    return directive;

    function link(scope, element, attrs) {
      var $select = element.find(".coral-Select");

      console.log("attrs", attrs);

      scope.id = utilities.nextUid();
      scope.dropdownOptions = scope.property.metadata.properties.dropdownOptions;
      scope.i18n = $rootScope.i18n;

      console.log("running link?", scope);
      console.log("$select", $select);

      $timeout(function() {
        uiService.addUI(uiService.component.SELECT, scope.property.name, {
          element: $select
        });

        // uiService.onEvent(uiService.component.SELECT, scope.property.name, "name" function() {

        // });
      }, false);
    }
  }
}(angular));
