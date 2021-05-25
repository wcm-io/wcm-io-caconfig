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
(function (angular, CUI) {
  "use strict";

  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigPathbrowser", pathbrowser);

  pathbrowser.$inject = ["templateUrlList", "inputMap", "directivePropertyPrefixes", "$rootScope", "$http", "configService"];

  function pathbrowser(templateList, inputMap, directivePropertyPrefixes, $rootScope, $http, configService) {
    var directive = {
      replace: true,
      templateUrl: templateList.pathbrowser,
      scope: {
        property: "="
      },
      link: link
    };

    return directive;

    function link(scope, element) {
      var input = inputMap[scope.property.metadata.type];
      var prefix = directivePropertyPrefixes.pathbrowser;
      var props = scope.property.metadata.properties;
      var options = {};
      var widget;

      scope.type = input.type;
      scope.i18n = $rootScope.i18n;

      angular.forEach(props, function (value, prop) {
        var propName;
        // if the property starts with the prefix "pathbrowser" followed by a pathbrowser property name
        // remove the "pathbrowser" prefix and use the remaining part as option name
        if (prop && prop.length > prefix.length && prop.substring(0, prefix.length) !== -1) {
          propName = prop.substring(prefix.length);
          options[propName.charAt(0).toLowerCase() + propName.slice(1)] = props[prop];
        }
      });

      // get root path from config
      options.rootPath = options.rootPath || "/content";
      // if rootPathContext is set set root path to current context path
      if (options.rootPathContext === "true") {
        options.rootPath = configService.getState().contextPath || options.rootPath;
        delete options.rootPathContext;
      }

      options.predicate = options.predicate || "hierarchyNotFile";
      options.pickerSrc = options.pickerSrc || "/libs/wcm/core/content/common/pathbrowser/column.html"
        + options.rootPath + "?predicate=" + options.predicate;
      options.optionLoader = loadAutocompleteOptions;

      options.element = element.children(".coral-PathBrowser");
      widget = new CUI.PathBrowser(options);

      scope.$on("$destroy", function() {
        // remove listeners
        widget.off();
      });
    }

    /**
     * Helper method for the CUI:PathBrowser widget
     * @param  {String}    path
     * @param  {Function=} callback
     * @return {Boolean}
     */
    function loadAutocompleteOptions (path, callback) {
      $http({
        url: path + ".pages.json",
        params: {
          predicate: "hierarchyNotFile"
        },
        responseType: "json"
      })
        .then(function success(response) {
          var pages = response.data.pages;
          var result = [];
          var i;
          for (i = 0; i < pages.length; i++) {
            result.push(pages[i].label);
          }
          if (callback) {
            callback(result);
          }
        });

      return false;
    }
  }
}(angular, CUI));
