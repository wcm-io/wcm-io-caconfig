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
    .controller("EditorController", EditorController);

  EditorController.$inject = ["modalService", "$element", "$compile", "$scope"];

  function EditorController(modalService, $element, $compile, $scope) {
    var that = this;

    that.save = function () {
      console.log('this: ', this.richContent);
      console.log('$scope: ',  $scope.richContent);

      modalService.triggerEvent(modalService.modal.EDITOR, 'saved', {content: $scope.richContent});
    };

    modalService.addModal(modalService.modal.EDITOR, {
      element: "#caconfig-editorModal",
      visible: false
    });

    modalService.onEvent(modalService.modal.EDITOR, 'beforeChange', function (e) {
      console.log("ALLLOO");
      console.log(e);

      e.preventDefault();
      return false;
    });
    modalService.onEvent(modalService.modal.EDITOR, 'change', function (e) {
      console.log("ALLLOO2");
      console.log(e);

      e.preventDefault();
      return false;
    });
    modalService.onEvent(modalService.modal.EDITOR, 'show', function (e) {
      console.log($element);

      $compile($element.contents())($scope);
    });
    modalService.onEvent(modalService.modal.EDITOR, 'hide', function (e) {
      console.log("ALLLOO4");
      console.log(e);
    });
  }
}(angular));