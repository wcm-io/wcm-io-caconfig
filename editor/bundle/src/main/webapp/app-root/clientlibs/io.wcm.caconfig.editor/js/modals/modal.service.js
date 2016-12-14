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
    .service("modalService", modalService);

  modalService.$inject = ["uiService"];

  function modalService(uiService) {

    this.modal = {
      ADD_CONFIG: "addConfig",
      ADD_COLLECTION_ITEM: "addCollectionItem",
      DELETE_CONFIG: "deleteConfig",
      ERROR: "error"
    }

    this.addModal = function (name, options) {
      uiService.addUI(uiService.component.MODAL, name, options);
    };

    this.addEvent = function (name, eventName, callback) {
      uiService.addEvent(uiService.component.MODAL, name, eventName, callback);
    };

    this.show = function (name) {
      uiService.callMethod(uiService.component.MODAL, name, uiService.method.SHOW);
    };
  }

})(angular);
