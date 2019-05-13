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
(function (angular, CUI) {
  "use strict";

  /**
   * Wrapper service for CoralUI
   */
  angular.module("io.wcm.caconfig.utilities")
    .service("uiService", UIService);

  UIService.$inject = ["$document"];

  function UIService($document) {
    var that = this;
    var ui = {};

    that.component = {
      MODAL: "Modal",
      MULTIFIELD: "Multifield",
      POPOVER: "Popover",
      SELECT: "Select"
    };

    that.method = {
      SHOW: "show",
      GET_VALUE: "getValue"
    };

    /**
     * Create instance of UI component
     *
     * @param {String} componentType
     * @param {String} componentName
     * @param {Object=} options
     */
    that.addUI = function (componentType, componentName, options) {
      ui[componentType] = ui[componentType] || {};
      ui[componentType][componentName] = new CUI[componentType](options);
    };

    /**
     * Attach event listener to UI instance
     *
     * @param {String}   componentType
     * @param {String}   componentName
     * @param {String}   eventName
     * @param {Function} callback
     */
    that.onEvent = function (componentType, componentName, eventName, callback) {
      ui[componentType][componentName].on(eventName, callback);
    };

    /**
     * @param {String}  componentType
     * @param {String}  componentName
     * @param {String}  eventName
     * @param {Object=} data
     */
    that.triggerEvent = function (componentType, componentName, eventName, data) {
      data = data || {};
      ui[componentType][componentName].$element.trigger(eventName, data);
    };

    /**
     * @param  {String} componentType
     * @param  {String} componentName
     * @param  {String} methodName
     * @return {*}
     */
    that.callMethod = function (componentType, componentName, methodName) {
      if (ui[componentType] && ui[componentType][componentName]
          && angular.isFunction(ui[componentType][componentName][methodName])) {
        return ui[componentType][componentName][methodName]();
      }
      return null;
    };

    that.showLoading = function () {
      $document.find(".caconfig-loading").show();
    };

    that.hideLoading = function () {
      $document.find(".caconfig-loading").hide();
    };
  }
}(angular, CUI));
