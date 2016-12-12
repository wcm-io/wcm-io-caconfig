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

  angular.module("io.wcm.caconfig.editor")
    .controller("OverviewController", OverviewController);

  OverviewController.$inject = ["$rootScope", "$scope", "$filter", "dataService"];

  function OverviewController($rootScope, $scope, $filter, dataService) {
    $rootScope.getConfigNames();
    $rootScope.title = $rootScope.i18n.title;

    $scope.hasNonExistingConfig = function() {
      if (!$scope.configNamesCollection) {
        return false;
      }
      for (var i in $scope.configNamesCollection) {
        if (!$scope.configNamesCollection[i].exists) {
          return true;
        }
      }
      return false;
    };

    $scope.showNonExistingConfigs = function() {
      $("#caconfig-configurationSelectClone").remove();

      var $select = $("#caconfig-configurationSelect").hide().removeClass("coral-Select");
      var $selectClone = $("#caconfig-configurationSelect")
        .clone()
        .addClass("coral-Select")
        .css("display", "inline-block")
        .attr("id", "caconfig-configurationSelectClone");

      $select.before($selectClone);

      $rootScope.configurationSelect = new CUI.Select({
        element: $selectClone
      });
      $scope.addConfigModal.show();
    };

    $rootScope.addConfig = function() {
      var configName = $rootScope.configurationSelect.getValue();
      $rootScope.go(configName);
    };
  }
})(angular);