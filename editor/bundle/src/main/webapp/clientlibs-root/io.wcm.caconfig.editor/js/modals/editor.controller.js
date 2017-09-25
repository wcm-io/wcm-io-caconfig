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

  EditorController.$inject = ["modalService", "$scope"];

  function EditorController(modalService, $scope) {
    var that = this,
      init = function () {

        // modal creation
        modalService.addModal(modalService.modal.EDITOR, {
          element: "#caconfig-editorModal",
          visible: false
        });

        modalService.onEvent(modalService.modal.EDITOR, 'show', function () {
            $scope.richContent = modalService.getEditorValue();
        });
      };

    /**
     * Triggers some 'saved' event so that the modal opener can handle data
     */
    that.save = function () {
      modalService.triggerEvent(modalService.modal.EDITOR, 'saved', { content: $scope.richContent });
    };

    /**
     * Initialisation
     */
    init();
  }
}(angular));