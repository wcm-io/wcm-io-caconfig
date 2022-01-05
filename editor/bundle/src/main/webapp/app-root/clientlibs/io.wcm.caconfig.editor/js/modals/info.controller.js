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

  angular.module("io.wcm.caconfig.modals")
    .controller("InfoController", InfoController);

  InfoController.$inject = ["$timeout", "modalService"];

  function InfoController($timeout, modalService) {
    var that = this;
    that.message = "";

    modalService.addModal(modalService.modal.INFO, {
      element: "#caconfig-infoModal",
      visible: false
    });

    modalService.onEvent(modalService.modal.INFO, modalService.event.CUSTOM_MESSAGE, function (e) {
      var data = e.detail || {};
      that.message = data.message || "";

      $timeout(function() {
        modalService.show(modalService.modal.INFO);
      }, 10);
    });

    modalService.onEvent(modalService.modal.INFO, "hide", function () {
      that.message = "";
    });
  }
}(angular));
