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
    .service("uiService", uiService);

  function uiService() {
    var ui = {};

    this.component = {
      MODAL: "Modal",
      POPOVER: "Popover",
      SELECT: "Select"
    };

    this.method = {
      SHOW: "show",
      GET_VALUE: "getValue"
    };

    /**
     * Create instance of UI component
     *
     * @param {String} componentType
     * @param {String} name
     * @param {Object=} options
     */
    this.addUI = function (componentType, name, options) {
      ui[componentType] = ui[componentType] || {};
      ui[componentType][name] = new CUI[componentType](options);
    };

    /**
     * Attach event to UI instance
     *
     * @param {String}   componentType
     * @param {String}   name
     * @param {String}   eventName
     * @param {Function} callback
     */
    this.addEvent = function (componentType, name, eventName, callback) {
      ui[componentType][name].on(eventName, callback);
    };

    /**
     * @param  {String} componentType
     * @param  {String} name
     * @param  {String} methodName
     * @return {*}
     */
    this.callMethod = function (componentType, name, methodName) {
      if (ui[componentType] &&
          ui[componentType][name] &&
          angular.isFunction(ui[componentType][name][methodName])) {
        return ui[componentType][name][methodName]();
      }
    };
  }

})(angular, CUI);
