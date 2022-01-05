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
    .controller("ErrorController", ErrorController);

  ErrorController.$inject = ["$rootScope", "$timeout", "modalService"];

  function ErrorController($rootScope, $timeout, modalService) {
    var that = this;
    var defaultMessage = $rootScope.i18n("modal.error.message");
    that.message = defaultMessage;

    modalService.addModal(modalService.modal.ERROR, {
      element: "#caconfig-errorModal",
      visible: false
    });

    modalService.onEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, function (e) {
      var response,
        message;

      var data = e.detail || {};

      response = data.response || {};
      message = data.message;

      if (response.status === 403 && response.data && angular.isString(response.data)) {
        that.message = response.data;
      }
      else if (angular.isString(message) && message.length) {
        that.message = message;
      }
      else {
        that.message = defaultMessage;
      }
      $timeout(function() {
        modalService.show(modalService.modal.ERROR);
      }, 10);
    });

    modalService.onEvent(modalService.modal.ERROR, "hide", function () {
      that.message = defaultMessage;
    });
  }
}(angular));
