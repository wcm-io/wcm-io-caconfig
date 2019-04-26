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

  ConfigService.$inject = ["$q", "dataService", "configCacheService", "currentConfigService", "modalService"];

  function ConfigService($q, dataService, configCacheService, currentConfigService, modalService) {
    var that = this;

    var state = {
      contextPath: null,
      configNames: []
    };

    that.getState = function () {
      return state;
    };

    /**
     * Loads configNames from REST url.
     * Sets up configCache.
     * @return {Promise}
     */
    that.loadConfigNames = function () {
      return dataService.getConfigNames().then(
        function success(response) {
          state.contextPath = response.data.contextPath;
          state.configNames = response.data.configNames;
          configCacheService.plantConfigCache(response.data.configNames);
        },
        function error(response) {
          modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
            response: response
          });
        }
      );
    };

    /**
     * Loads config from REST url.
     * Triggers update of configCache.
     * Sets config as current.
     * @param  {String}  configName
     * @param  {Boolean} isCollection
     * @return {Promise}
     */
    that.loadConfig = function (configName) {
      var configNameObject = configCacheService.getConfigNameObject(configName);
      var isCollection;

      // Most likely caused by user deeplinking to uncached configuration.
      if (!configNameObject) {
        configCacheService.removeStoredConfigCache();
        return $q.reject();
      }

      isCollection = Boolean(configNameObject.collection);

      return dataService.getConfigData(configName, isCollection)
        .then(
          function success(response) {
            var current = {};
            if (isCollection) {
              currentConfigService.setCollectionItemTemplate(configName, response.data.newItem);
            }
            // if collection, but no items, use newItem "template" to display properties
            if (isCollection && !(response.data.configs && response.data.configs.length)) {
              response.data.newItem.isNewItem = true;
              configCacheService.updateConfigCache([response.data.newItem]);
            }
            else {
              configCacheService.updateConfigCache(response.data.configs);
            }

            current = {
              configName: configName,
              isCollection: isCollection,
              configs: response.data.configs,
              configNameObject: configNameObject,
              collectionProperties: response.data.collectionProperties,
              propertyTypes: configCacheService.getPropertyTypes(configName)
            };
            currentConfigService.setCurrent(current);
            return current;
          },
          function error(response) {
            modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
              response: response
            });
          }
        );
    };

    /**
     * Triggers POST of updated config to REST service.
     * @return {Object|null} parent config, if any - otherwise null.
     */
    that.saveCurrentConfig = function () {
      var current = currentConfigService.getCurrent();
      var parent = current.configNameObject.parent || null;
      return dataService.saveConfigData(current)
        .then(
          function success() {
            return parent;
          },
          function error(response) {
            modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
              response: response
            });
          }
        );
    };

    /**
     * Triggers DELETE of current config.
     * @return {Object|null} parent config, if any - otherwise null.
     */
    that.deleteCurrentConfig = function () {
      var current = currentConfigService.getCurrent();
      var parent = current.configNameObject.parent || null;
      return dataService.deleteConfigData(current.configName).then(
        function success() {
          configCacheService.removeConfigFromCache(current.configName);
          return parent;
        },
        function error(response) {
          modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
            response: response
          });
        }
      );
    };
  }

}(angular));
