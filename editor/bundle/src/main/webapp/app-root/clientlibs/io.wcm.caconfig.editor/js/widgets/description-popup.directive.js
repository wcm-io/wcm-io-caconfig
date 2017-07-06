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
(function (angular, $) {
  "use strict";

  /**
   * Directive to render the "i" button with a popover for the decription of the parameter.
   * Wraps a Coral UI Button and CUI.Popover widget.
   * This directive transcludes the popup content elements.
   *
   * @example
   * <caconfig-description-popup>
   *   <caconfig-popup-content>
   *     Description Text
   *   </caconfig-popup-content>
   * </caconfig-description-popup>
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigDescriptionPopup", descriptionPopup);

  descriptionPopup.$inject = ["templateUrlList", "utilities", "uiService"];

  function descriptionPopup(templateList, utilities, uiService) {

    var directive = {
      restrict: "E",
      replace: true,
      templateUrl: templateList.popupContainer,
      transclude: true,
      link: link
    };

    return directive;

    function link(scope, element) {
      scope.id = utilities.nextUid();
      scope.$evalAsync(function () {
        uiService.addUI(uiService.component.POPOVER, scope.id, {
          element: $("coral-Popover", element)
        });
      });
    }
  }
}(angular, jQuery));
