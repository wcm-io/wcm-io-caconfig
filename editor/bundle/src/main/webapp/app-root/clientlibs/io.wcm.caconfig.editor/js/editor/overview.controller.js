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

  var CONFIG_SELECT = "config";

  angular.module("io.wcm.caconfig.editor")
    .controller("OverviewController", OverviewController);

  OverviewController.$inject = ["$rootScope", "$scope", "configService", "uiService", "modalService"];

  function OverviewController($rootScope, $scope, configService, uiService, modalService) {
    $rootScope.title = $rootScope.i18n.title;

    configService.loadConfigNames()
      .then(function success() {
        $scope.contextPath = configService.getContextPath();
        $scope.configNames = configService.getConfigNames();
      });

    $scope.hasNonExistingConfig = function () {
      var i;

      if (!$scope.configNames) {
        return false;
      }

      for (i = 0; i < $scope.configNames.length; i++) {
        if (!$scope.configNames[i].exists) {
          return true;
        }
      }
      return false;
    };

    $scope.showNonExistingConfigs = function () {
      var $select,
          $selectClone;

      $("#caconfig-configurationSelectClone").remove();

      $select = $("#caconfig-configurationSelect").hide()
        .removeClass("coral-Select");
      $selectClone = $("#caconfig-configurationSelect")
        .clone()
        .addClass("coral-Select")
        .css("display", "inline-block")
        .attr("id", "caconfig-configurationSelectClone");

      $select.before($selectClone);

      uiService.addUI(uiService.component.SELECT, CONFIG_SELECT, {
        element: $selectClone
      });

      modalService.show(modalService.modal.ADD_CONFIG);
    };

    $rootScope.addConfig = function () {
      var configName = uiService.callMethod(uiService.component.SELECT, CONFIG_SELECT, uiService.method.GET_VALUE);
      $rootScope.go(configName);
    };
  }
}(angular, jQuery));