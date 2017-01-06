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
   * Config service
   *
   * Interface between dataService and controllers (and storage)
   */
  angular.module("io.wcm.caconfig.editor")
    .service("configService", ConfigService);

  ConfigService.$inject = ["dataService", "configCacheService", "currentConfigService", "modalService"];

  function ConfigService(dataService, configCacheService, currentConfigService, modalService) {

    var state = {
      contextPath: null,
      configNames: []
    };
    this.state = state;

    /**
     * [loadConfigNames description]
     * @return {Promise} [description]
     */
    this.loadConfigNames = function () {
      return dataService.getConfigNames().then(
        function success(result) {
          state.contextPath = result.data.contextPath;
          state.configNames = result.data.configNames;
          configCacheService.plantConfigCache(result.data.configNames);
        },
        function error() {
          modalService.show(modalService.modal.ERROR);
        }
      );
    };

    /**
     * Loads config from REST url
     * Triggers update of configCache
     * @param  {String}  configName
     * @param  {Boolean} isCollection
     * @return {Promise}
     */
    this.loadConfig = function (configName) {
      var configNameObject = configCacheService.getConfigNameObject(configName);
      var isCollection = Boolean(configNameObject.collection);

      return dataService.getConfigData(configName, isCollection)
        .then(
          function success(result) {
            var current = {};

            if (isCollection) {
              currentConfigService.setCollectionItemTemplate(configName, result.data.newItem);
            }
            configCacheService.updateConfigCache(result.data.configs);
            current = {
              configName: configName,
              isCollection: isCollection,
              configs: result.data.configs,
              configNameObject: configNameObject
            };
            currentConfigService.setCurrent(current);
            return current;
          },
          function error() {
            modalService.show(modalService.modal.ERROR);
          }
        );
    };

    this.saveCurrentConfig = function () {
      var current = currentConfigService.getCurrent();
      var parent = current.configNameObject.parent || "";
      return dataService.saveConfigData(current.configName, current.isCollection, current.configs)
        .then(
          function success() {
            configCacheService.removeStoredConfigCache();
            return parent;
          },
          function error() {
            modalService.show(modalService.modal.ERROR);
          }
        );
    };

    this.deleteCurrentConfig = function () {
      var current = currentConfigService.getCurrent();
      var parent = current.configNameObject.parent || "";
      return dataService.deleteConfigData(current.configName).then(
        function success() {
          configCacheService.removeStoredConfigCache();
          return parent;
        },
        function error() {
          modalService.show(modalService.modal.ERROR);
        }
      );
    };
  }

}(angular));
