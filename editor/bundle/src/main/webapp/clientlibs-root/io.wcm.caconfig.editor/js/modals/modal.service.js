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
   * Modals service
   */
  angular.module("io.wcm.caconfig.modals")
    .service("modalService", ModalService);

  ModalService.$inject = ["uiService"];

  function ModalService(uiService) {
    var that = this;

    that.event = {
      CUSTOM_MESSAGE: "caconfig-customMessage"
    };

    that.modal = {
      ADD_CONFIG: "addConfig",
      ADD_COLLECTION_ITEM: "addCollectionItem",
      DELETE_CONFIG: "deleteConfig",
      INFO: "info",
      ERROR: "error",
      SAVE_CONFIG: "saveConfig"
    };

    /**
     * @param {String} modalName
     * @param {Object} options
     */
    that.addModal = function (modalName, options) {
      uiService.addUI(uiService.component.MODAL, modalName, options);
    };

    /**
     * @param {String} modalName
     * @param {String} eventName
     * @param {Function} callback
     */
    that.onEvent = function (modalName, eventName, callback) {
      uiService.onEvent(uiService.component.MODAL, modalName, eventName, callback);
    };

    /**
     * @param {String} modalName
     * @param {String} eventName
     * @param {Object=} data
     */
    that.triggerEvent = function (modalName, eventName, data) {
      uiService.triggerEvent(uiService.component.MODAL, modalName, eventName, data);
    };

    /**
     * @param {String} modalName
     */
    that.show = function (modalName) {
      uiService.callMethod(uiService.component.MODAL, modalName, uiService.method.SHOW);
    };
  }

}(angular));
