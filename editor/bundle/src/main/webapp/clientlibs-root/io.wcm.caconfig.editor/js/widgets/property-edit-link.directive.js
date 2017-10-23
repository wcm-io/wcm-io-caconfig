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
   * Directive for a link to a nested configuration
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPropertyEditLink", propertyEditLink);

  propertyEditLink.$inject = ["templateUrlList", "$rootScope"];

  function propertyEditLink(templateList, $rootScope) {

    var directive = {
      templateUrl: templateList.propertyEditLink,
      scope: {
        configName: "@",
        linkText: "@"
      },
      replace: true,
      link: link
    };

    return directive;

    function link(scope) {
      scope.configForm = $rootScope.configForm;
      scope.go = $rootScope.go;
      scope.saveWarning = $rootScope.saveWarning;
      scope.linkText = $rootScope.i18n.button.edit;
    }
  }
}(angular));
