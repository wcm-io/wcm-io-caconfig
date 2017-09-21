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
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigRichtexteditor", richtexteditor);

  richtexteditor.$inject = ["templateUrlList", "modalService"];

  function richtexteditor(templateList, modalService) {
    var directive = {
      restrict: "E",
      replace: true,
      require: "^form",
      templateUrl: templateList.richtexteditor,
      scope: {
        parameter: "=caconfigParameter",
        isConfigInherited: "=caconfigIsConfigInherited"
      },
      link: link
    };

    return directive;

    function link(scope, element, attr, form) {
      scope.openPopup = function () {
        var modal_instance = modalService.getComponent(modalService.modal.EDITOR);
        modalService.setEditorValue(scope.parameter.value);
        var save = function(e, data) {
                form.$setDirty(true);
                scope.parameter.value = data.content;
                modal_instance.off('saved', scope.save);
        };
        modal_instance.show();
        modal_instance.on('saved', save);
      };
    }
  }
}(angular));
