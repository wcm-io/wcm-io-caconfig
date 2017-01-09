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
   * Controller for details view
   * (for singletons, collections and nested configs/collection)
   */
  angular.module("io.wcm.caconfig.editor")
    .controller("DetailController", DetailController);

  DetailController.$inject = ["$rootScope", "$route", "configService", "currentConfigService", "modalService"];

  function DetailController($rootScope, $route, configService, currentConfigService, modalService) {
    var current = {
      configName: $route.current.params.configName,
      configs: []
    };

    this.current = current;
    this.removeConfig = removeConfig;

    // If detail view was loaded directly via deeplink, we need to first loadConfigNames
    if (!configService.state.contextPath || !configService.state.configNames.length) {
      configService.loadConfigNames()
        .then(function success() {
          init();
        });
    }
    else {
      init();
    }

    $rootScope.saveWarning = function (redirectUrl) {
      $rootScope.redirectUrl = null;
      if (angular.isString(redirectUrl)) {
        $rootScope.redirectUrl = redirectUrl;
      }
      modalService.show(modalService.modal.SAVE_CONFIG);
    };

    this.saveConfig = function () {
      if (current.configs.length === 0) {
        removeConfig();
      }
      else {
        configService.saveCurrentConfig()
          .then(function (redirect) {
            if (redirect) {
              $rootScope.go(redirect.configName || "");
            }
            else {
              $rootScope.go(current.parent ? current.parent.configName : "");
            }
          });
      }
    };

    function removeConfig() {
      modalService.show(modalService.modal.DELETE_CONFIG);
    }

    $rootScope.deleteConfig = function () {
      configService.deleteCurrentConfig()
        .then(function (redirect) {
          if (redirect) {
            $rootScope.go(redirect.configName || "");
          }
          else {
            $rootScope.go(current.parent ? current.parent.configName : "");
          }
        });
    };

    this.addCollectionItem = function () {
      modalService.show(modalService.modal.ADD_COLLECTION_ITEM);
    };

    this.removeCollectionItem = function (index) {
      currentConfigService.removeItemFromCurrentCollection(index);
    };

    this.isModified = function (formPristine) {
      return !formPristine || current.originalLength !== current.configs.length;
    };

    this.handleInheritedChange = function (property) {
      if (!property.inherited && angular.isUndefined(property.value)) {
        property.value = property.effectiveValue;
      }
      else {
        property.effectiveValue = "(" + $rootScope.i18n.config.inherited + ")";
      }
    };

    /**
     * Loads config data and sets various $scope properties
     */
    function init() {
      // Load Configuration Details
      configService.loadConfig(current.configName)
        .then(function (currentData) {
          current.configs = currentData.configs;
          current.originalLength = currentData.configs.length;
          current.isCollection = currentData.isCollection;
          current.isNewCollection = currentData.isCollection && currentData.configs.length === 0;
          current.label = currentData.configNameObject.label || current.configName;
          current.breadcrumbs = currentData.configNameObject.breadcrumbs || [];
          current.parent = current.breadcrumbs[current.breadcrumbs.length - 1];
          current.description = currentData.configNameObject.description;
          current.contextPath = configService.state.contextPath;
          $rootScope.title = $rootScope.i18n.title + ": " + current.label;
        });
    }
  }
}(angular));
