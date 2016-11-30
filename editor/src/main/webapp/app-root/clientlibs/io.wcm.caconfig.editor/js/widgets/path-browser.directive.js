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
   * Path browser directive to allow select pages from the siteadmin.
   * Utilizes the CUI.PathBrowser
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPathBrowser", pathBrowser);

  pathBrowser.$inject = ["templateUrlList", "utilities"];

  function pathBrowser(templateList, utilities) {

    function link(scope, element, attr) {
      var widget = new CUI.PathBrowser({element: element,
        optionLoader: utilities.loadAutocompleteOptions,
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

    return {
      restrict: "E",
      replace: true,
      templateUrl: templateList.pathBrowser,
      scope: {
        parameter: "=",
        rootPath: "@"
      },
      link: link
    }
  }
})(angular);
