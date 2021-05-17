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
(function (angular, Granite) {
  "use strict";

  angular.module("io.wcm.caconfig.editor", [
    "ngRoute"
  ])
    .run(initRun);

  initRun.$inject = ["$rootScope", "$location"];

  function initRun($rootScope, $location) {

    $rootScope.redirectUrl = null;

    $rootScope.go = function (path) {
      path = path ? String(path) : "";
      if (path.charAt(0) !== "/") {
        path = "/" + path;
      }
      $location.path(path);
    };

    $rootScope.goToRedirectUrl = function () {
      $rootScope.go($rootScope.redirectUrl);
      $rootScope.redirectUrl = null;
    };

    $rootScope.i18n = function (key) {
      return Granite.I18n.get("io.wcm.caconfig.editor." + key);
    };
  }

}(angular, Granite));
