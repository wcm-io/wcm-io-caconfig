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

  var DEFAULT_ITEM_NAME_PATTERN = /^[\w-]+$/;

  angular.module("io.wcm.caconfig.modals")
    .controller("AddCollectionItemController", AddCollectionItemController);

  AddCollectionItemController.$inject = ["$document", "modalService", "currentConfigService", "$timeout"];

  function AddCollectionItemController($document, modalService, currentConfigService, $timeout) {
    var that = this;
    var $collectionItemNameInput = $document.find("#caconfig-collectionItemName");

    that.denylist = [];
    that.itemTitleRegex = DEFAULT_ITEM_NAME_PATTERN;

    modalService.addModal(modalService.modal.ADD_COLLECTION_ITEM, {
      element: "#caconfig-addCollectionItemModal",
      visible: false
    });

    modalService.onEvent(modalService.modal.ADD_COLLECTION_ITEM, "coral-overlay:open", function () {
      that.newCollectionName = null;
      $collectionItemNameInput.val("");
      that.denylist = currentConfigService.getCollectionItemNames();
      $collectionItemNameInput.focus();
    });

    modalService.onEvent(modalService.modal.ADD_COLLECTION_ITEM, "coral-overlay:close", function () {
      that.newCollectionName = null;
      $collectionItemNameInput.val("");
    });

    that.addItem = function () {
      var collectionItemName = $collectionItemNameInput.val().trim();
      currentConfigService.addItemToCurrentCollection(collectionItemName);

      $timeout(function() {
        $document.find("html, body").animate({scrollTop: document.body.scrollHeight});
      }, 100);
    };
  }
}(angular));
