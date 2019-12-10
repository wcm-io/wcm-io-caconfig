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
    .directive("caconfigPropertyRow", propertyRow);

  propertyRow.$inject = ["templateUrlList", "currentConfigService"];

  function propertyRow(templateList, currentConfigService) {

    var directive = {
      restrict: "A",
      templateUrl: templateList.propertyRow,
      scope: {
        property: "=caconfigPropertyRow",
        propertyInheritanceEnabled: "=caconfigPropertyInheritanceEnabled"
      },
      transclude: true,
      replace: true,
      link: link
    };

    return directive;

    function link(scope) {
      scope.propertyRow = {
        handleInheritedChange: currentConfigService.handleInheritedChange
      };
    }
  }
}(angular));
