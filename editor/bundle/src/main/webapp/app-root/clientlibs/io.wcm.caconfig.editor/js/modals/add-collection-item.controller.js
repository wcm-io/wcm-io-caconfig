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

  var DEFAULT_ITEM_NAME_PATTERN = /^[\w\-]+$/;

  angular.module("io.wcm.caconfig.modals")
    .controller("AddCollectionItemController", AddCollectionItemController);

  AddCollectionItemController.$inject = ["$scope", "modalService", "currentConfigService"];

  function AddCollectionItemController($scope, modalService, currentConfigService) {
    $scope.blacklist = [];
    $scope.itemTitleRegex = DEFAULT_ITEM_NAME_PATTERN;

    modalService.addModal(modalService.modal.ADD_COLLECTION_ITEM, {
      element: "#caconfig-addCollectionItemModal",
      visible: false
    });

    modalService.addEvent(modalService.modal.ADD_COLLECTION_ITEM, "show", function () {
      $scope.newCollectionName = null;
      $scope.blacklist = currentConfigService.getCollectionItemNames();
      $("#caconfig-collectionItemName").focus();
    });

    $scope.addItem = function () {
      var collectionItemName = $("#caconfig-collectionItemName").val().trim();
      currentConfigService.addItemToCurrentCollection(collectionItemName);
    };
  }
})(angular);