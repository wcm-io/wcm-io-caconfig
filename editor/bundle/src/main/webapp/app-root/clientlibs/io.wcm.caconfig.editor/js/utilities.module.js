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

  var uid = ["0", "0", "0"];
  var DIGIT_9 = 57;
  var LETTER_Z = 90;

  /**
   * Utilities module.
   */
  angular.module("io.wcm.caconfig.utilities", [])
    .service("utilities", UtilitiesService);

  function UtilitiesService() {
    var that = this;

    /**
     * Generates unique id
     * @return {String}
     */
    that.nextUid = function () {
      var index = uid.length;
      var digit;

      while (index) {
        index--;
        digit = uid[index].charCodeAt(0);
        if (digit === DIGIT_9) {
          uid[index] = "A";
          return uid.join("");
        }
        if (digit === LETTER_Z) {
          uid[index] = "0";
        }
        else {
          uid[index] = String.fromCharCode(digit + 1);
          return uid.join("");
        }
      }
      uid.unshift("0");
      return uid.join("");
    };
  }

}(angular));
