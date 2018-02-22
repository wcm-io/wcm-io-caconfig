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

  var CONFIG_SELECT = "config";

  angular.module("io.wcm.caconfig.modals")
    .controller("AddConfigController", AddConfigController);

  AddConfigController.$inject = ["$document", "$rootScope", "modalService", "configService", "uiService"];

  /* eslint-disable max-params */
  function AddConfigController($document, $rootScope, modalService, configService, uiService) {
  /* eslint-enable max-params */
    var that = this;

    modalService.addModal(modalService.modal.ADD_CONFIG, {
      element: "#caconfig-addConfigModal",
      visible: false
    });

    modalService.onEvent(modalService.modal.ADD_CONFIG, "caconfig-setup", function () {
      var $select,
        $selectClone;

      $document.find("#caconfig-configurationSelectClone").remove();

      $select = $document.find("#caconfig-configurationSelect").hide()
        .removeClass("coral-Select");
      $selectClone = $document.find("#caconfig-configurationSelect")
        .clone()
        .addClass("coral-Select")
        .css("display", "inline-block")
        .attr("id", "caconfig-configurationSelectClone");

      $select.before($selectClone);

      uiService.addUI(uiService.component.SELECT, CONFIG_SELECT, {
        element: $selectClone
      });
    });

    that.getConfigNames = function () {
      return configService.getState().configNames;
    };

    that.addConfig = function () {
      var configName = uiService.callMethod(uiService.component.SELECT, CONFIG_SELECT, uiService.method.GET_VALUE);
      $rootScope.go(configName);
    };
  }
}(angular));