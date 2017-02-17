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
   * Data Services module
   * $http calls to the REST API
   */
  angular.module("io.wcm.caconfig.editor")
    .provider("dataService", DataServiceProvider);

  function DataServiceProvider() {
    var configUrls = {};

    function DataService($http, dataHelperService, restUrls) {

      /**
       * Get configuration names.
       * @returns {Promise}
       */
      this.getConfigNames = function () {
        return $http.get(restUrls.configNamesUrl);
      };

      /**
       * Get configuration data.
       * @param {String} configName
       * @param {Boolean} isCollection
       * @returns {Promise}
       */
      this.getConfigData = function (configName, isCollection) {
        var url = restUrls.configDataUrl;

        if (angular.isString(configName)) {
          url += "?configName=" + configName;

          if (isCollection) {
            url += "&collection=true";
          }
        }
        return $http.get(url, {
          transformResponse: dataHelperService.parseConfigData
        });
      };

      /**
       * @param {Object} current
       * @returns {Promise}
       */
      this.saveConfigData = function (current) {
        var configData = dataHelperService.buildConfigData(current);
        var url = restUrls.configPersistUrl + "?configName=" + current.configName;

        if (current.isCollection) {
          url += "&collection=true";
        }
        return $http.post(url, configData);
      };

      /**
       * @param {String} configName
       * @returns {Promise}
       */
      this.deleteConfigData = function (configName) {
        var url = restUrls.configPersistUrl + "?configName=" + configName;
        return $http({
          method: "DELETE",
          url: url
        });
      };
    }

    this.setRestUrls = function (restUrlsConfig) {
      configUrls = restUrlsConfig;
    };

    this.$get = ["$http", "dataHelperService", function ($http, dataHelperService) {
      return new DataService($http, dataHelperService, configUrls);
    }];
  }

}(angular));
