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
(function (angular, _) {
  "use strict";
  angular.module("io.wcm.caconfig.widgets")
      .directive("caconfigPathbrowser", pathbrowser);

  pathbrowser.$inject = ["templateUrlList", "inputMap", "directivePropertyPrefixes", "$rootScope"];

  function pathbrowser(templateList, inputMap, directivePropertyPrefixes, $rootScope) {
    var directive = {
      restrict: "E",
      replace: true,
      require: "^form",
      templateUrl: templateList.pathbrowser,
      scope: {
        parameter: "="
      },
      link: link
    };

    return directive;

    function link(scope, element, attr, form) {
      var input = inputMap[scope.parameter.metadata.type];
      var inheritedStateChanged = false;

      scope.type = input.type;
      scope.pattern = input.pattern;
      scope.i18n = $rootScope.i18n;

      var prefix = directivePropertyPrefixes.pathbrowser;
      var props = scope.parameter.metadata.properties;
      var options = {};
      for (var prop in props) {
          if (prop && prop.substring(0, prefix.length) != -1) {
              var propName = prop.substring(prefix.length);
              options[propName.charAt(0).toLowerCase() + propName.slice(1)] = props[prop];
          }
      }

      options.rootPath = options.rootPath || "/content";
      options.predicate = options.predicate || "hierarchyNotFile";
      options.pickerSrc = options.pickerSrc || "/libs/wcm/core/content/common/pathbrowser/column.html" + options.rootPath + "?predicate=" + options.predicate;
      options.optionLoader = loadAutocompleteOptions;

      options.element = element.children(".coral-PathBrowser");
      var widget = new CUI.PathBrowser(options);

      scope.$on("$destroy", function() {
          // remove listeners
          widget.off();
      });

      scope.$watch("parameter.inherited", function (isInherited, wasInherited) {
        if (isInherited === wasInherited) {
          return;
        }
        inheritedStateChanged = true;
        form.$setDirty();
      });
    }
  }

  /**
   * Helper method for the CUI:PathBrowser widget
   * @param path
   * @param callback
   * @returns {boolean}
   */
  function loadAutocompleteOptions (path, callback) {
      jQuery.get(path + '.pages.json', {
              predicate: 'hierarchyNotFile'
          },
          function(data) {
              var pages = data.pages;
              var result = [];
              for(var i = 0; i < pages.length; i++) {
                  result.push(pages[i].label);
              }
              if (callback) callback(result);
          }, 'json');
      return false;
  }
}(angular, _));
