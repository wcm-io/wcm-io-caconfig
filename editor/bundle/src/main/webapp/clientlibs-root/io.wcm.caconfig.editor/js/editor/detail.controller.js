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
    var CONFIG_PROPERTY_INHERIT = "sling:configPropertyInherit";
    var CONFIG_COLLECTION_INHERIT = "sling:configCollectionInherit";
    var that = this;
    var forceFormModified = false;

    that.current = {
      configName: $route.current.params.configName,
      configs: []
    };

    // If detail view was loaded directly via deeplink, we need to first loadConfigNames
    if (!configService.getState().contextPath || !configService.getState().configNames.length) {
      configService.loadConfigNames()
        .then(init);
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

    that.saveConfig = function () {
      // if (that.current.configs.length === 0 && Boolean(that.current.collectionProperties[CONFIG_COLLECTION_INHERIT])) {
      //   that.removeConfig();
      // }
      // else {
        configService.saveCurrentConfig()
          .then(function (redirect) {
            if (redirect) {
              $rootScope.go(redirect.configName || "");
            }
            else {
              $rootScope.go(that.current.parent ? that.current.parent.configName : "");
            }
          });
      // }
    };

    that.removeConfig = function() {
      modalService.show(modalService.modal.DELETE_CONFIG);
    };

    $rootScope.deleteConfig = function () {
      configService.deleteCurrentConfig()
        .then(function (redirect) {
          if (redirect) {
            $rootScope.go(redirect.configName || "");
          }
          else {
            $rootScope.go(that.current.parent ? that.current.parent.configName : "");
          }
        });
    };

    that.addCollectionItem = function () {
      modalService.show(modalService.modal.ADD_COLLECTION_ITEM);
    };

    that.removeCollectionItem = function (index) {
      currentConfigService.removeItemFromCurrentCollection(index);
    };

    that.isModified = function (formPristine) {
      return !formPristine || forceFormModified
        || that.current.originalLength !== that.current.configs.length;
    };

    that.handleInheritedChange = function (property) {
      if (!property.metadata.multivalue
          && !property.inherited && angular.isUndefined(property.value)) {
        property.value = property.effectiveValue;
      }
      else {
        property.effectiveValue = "(" + $rootScope.i18n.config.inherited + ")";
        if (angular.isUndefined(property.value)) {
          property.value = null;
        }
      }
    };

    that.getConfigPropertyInherit = function (config) {
      var configPropertyInherit = _.find(config.properties, {
        name: CONFIG_PROPERTY_INHERIT
      });
      if (!configPropertyInherit) {
        configPropertyInherit = {
          name: CONFIG_PROPERTY_INHERIT,
          value: false
        };
        config.properties.push(configPropertyInherit);
      }
      return configPropertyInherit;
    };

    that.setConfigPropertyInherit = function (config, value) {
      var configPropertyInherit = that.getConfigPropertyInherit(config);
      configPropertyInherit.value = value;
      that.handleConfigPropertyInheritChange(config);
    };

    that.handleConfigPropertyInheritChange = function (config) {
      var configPropertyInherit = that.getConfigPropertyInherit(config);
      if (configPropertyInherit.value) {
        return;
      }
      angular.forEach(config.properties, function (property) {
        if (property.name !== CONFIG_PROPERTY_INHERIT && !property.overridden
            && !property.nestedConfig && !property.nestedConfigCollection) {
          property.inherited = false;
          that.handleInheritedChange(property);
        }
      });
    };

    that.breakInheritance = function (config) {
      config.inherited = false;
      that.setConfigPropertyInherit(config, true);
      forceFormModified = true;
    };

    /**
     * Loads config data and sets various properties
     */
    function init() {
      // Load Configuration Details
      configService.loadConfig(that.current.configName)
        .then(function (currentData) {
          that.current.configs = setDefaultValues(currentData.configs);
          that.current.originalLength = currentData.configs.length;
          that.current.isCollection = currentData.isCollection;
          that.current.isNewCollection = currentData.isCollection && currentData.configs.length === 0;
          that.current.collectionProperties = currentData.collectionProperties;
          that.current.label = currentData.configNameObject.label || that.current.configName;
          that.current.breadcrumbs = currentData.configNameObject.breadcrumbs || [];
          that.current.parent = that.current.breadcrumbs[that.current.breadcrumbs.length - 1];
          that.current.description = currentData.configNameObject.description;
          that.current.contextPath = configService.getState().contextPath;
          $rootScope.title = $rootScope.i18n.title + ": " + that.current.label;
        });
    }

    function setDefaultValues(configs) {
      angular.forEach(configs, function (config) {
        angular.forEach(config.properties, function (property) {
          if (property["default"] && angular.isUndefined(property.value)) {
            property.value = property.effectiveValue;
          }
        });
      });
      return configs;
    }
  }
}(angular));
