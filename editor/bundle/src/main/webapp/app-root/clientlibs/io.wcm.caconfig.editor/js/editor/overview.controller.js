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

  OverviewController.$inject = ["$rootScope", "configService", "modalService", "publishService"];

  function OverviewController($rootScope, configService, modalService, publishService) {
    var that = this;

    $rootScope.title = $rootScope.i18n("title");
    that.state = configService.getState();
    configService.loadConfigNames()
      .then(function() {
        that.ovReady = true;
      });

    that.hasNonExistingConfig = function () {
      var i;

      if (!that.state.configNames) {
        return false;
      }

      for (i = 0; i < that.state.configNames.length; i++) {
        if (!that.state.configNames[i].exists) {
          return true;
        }
      }
      return false;
    };

    that.showNonExistingConfigs = function () {
      modalService.triggerEvent(modalService.modal.ADD_CONFIG, "caconfig-setup");
      modalService.show(modalService.modal.ADD_CONFIG);
    };

    that.publishPage = function () {
      publishService.publishPage();
    };
  }
}(angular));
