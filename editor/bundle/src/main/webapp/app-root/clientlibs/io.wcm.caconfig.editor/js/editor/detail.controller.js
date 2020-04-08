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

  DetailController.$inject = ["$window", "$document", "$rootScope", "$timeout", "$route", "configService", "modalService"];

  function DetailController($window, $document, $rootScope, $timeout, $route, configService, modalService) {
    var that = this;
    var MAX_CONFIGS = Number.POSITIVE_INFINITY;
    var MAX_CONFIGS_PER_PAGE = 32;
    var BOTTOM_OF_PAGE_THRESHOLD = 600;

    that.current = {
      configName: $route.current.params.configName,
      configs: []
    };

    // If detail view was loaded directly via deep link, we need to first loadConfigNames
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
      configService.saveCurrentConfig()
        .then(function (redirect) {
          if (redirect) {
            $rootScope.go(redirect.configName || "");
          }
          else {
            $rootScope.go(that.current.parent ? that.current.parent.configName : "");
          }
        });
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

    function hideLargeCollectionInfo() {
      $document.find(".caconfig-largeCollection").hide();
    }

    that.addCollectionItem = function () {
      modalService.show(modalService.modal.ADD_COLLECTION_ITEM);
      that.configForm.$setDirty();
      that.showAllConfigs();
    };

    function addScrollListener() {
      $document.on("scroll", onScrollToBottom);
    }

    function removeScrollListener() {
      $document.off("scroll", onScrollToBottom);
    }

    /**
     * Triggers showMoreConfigs if user has scrolled to the bottom of page.
     */
    function onScrollToBottom() {
      var windowHeight = $window.innerHeight;
      var documentHeight = $window.document.body.offsetHeight - BOTTOM_OF_PAGE_THRESHOLD;
      if ((windowHeight + $window.pageYOffset) >= documentHeight) {
        $timeout(showMoreConfigs, false);
      }
    }

    function showConfigs() {
      if (that.allConfigsVisible) {
        return;
      }
      // If MAX_CONFIGS_PER_PAGE do not go beyond the height of the window,
      // the user will not be able to trigger the scroll - so we must explicitly increase the amount
      if (($window.document.body.offsetHeight - BOTTOM_OF_PAGE_THRESHOLD) < $window.innerHeight) {
        showMoreConfigs();
        $timeout(showConfigs, false);
      }
      else {
        addScrollListener();
      }
    }

    function showMoreConfigs() {
      if (that.allConfigsVisible) {
        return;
      }
      that.configLimit += MAX_CONFIGS_PER_PAGE;

      if (that.configLimit >= that.current.configs.length) {
        that.showAllConfigs();
      }
    }

    that.showAllConfigs = function() {
      if (that.allConfigsVisible) {
        return;
      }

      that.configLimit = MAX_CONFIGS;
      hideLargeCollectionInfo();
      removeScrollListener();
      that.allConfigsVisible = true;
    };

    that.toBottom = function() {
      $document.find("html, body").animate({scrollTop: document.body.scrollHeight});
    };

    that.toTop = function() {
      $document.find("html, body").animate({scrollTop: 0});
    };

    /**
     * Loads config data and sets various scope properties.
     * Sets up "infinite" scroll loading, if this is a large collection.
     */
    function init() {
      // Load Configuration Details
      configService.loadConfig(that.current.configName)
        .then(function success(currentData) {
          if (angular.isDefined(currentData)) {
            that.current.configs = currentData.configs;
            that.current.originalLength = currentData.configs.length;
            that.current.isCollection = currentData.isCollection;
            that.current.collectionProperties = currentData.collectionProperties;
            that.current.label = currentData.configNameObject.label || that.current.configName;
            that.current.breadcrumbs = currentData.configNameObject.breadcrumbs || [];
            that.current.parent = that.current.breadcrumbs[that.current.breadcrumbs.length - 1];
            that.current.description = currentData.configNameObject.description;
            that.current.contextPath = configService.getState().contextPath;
            $rootScope.title = $rootScope.i18n("title") + ": " + that.current.label;
            $rootScope.configForm = that.configForm;
            that.configLimit = MAX_CONFIGS_PER_PAGE;
            that.current.isLargeCollection = that.current.isCollection && (that.current.originalLength > MAX_CONFIGS_PER_PAGE);
          }
          that.dvReady = true;

          // Setup "Infinite" Scroll
          if (that.current.isLargeCollection) {
            $timeout(showConfigs, false);
          }
          else {
            that.showAllConfigs();
          }
        },
        function error(message) {
          modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
            message: message
          });
        });
    }
  }
}(angular));
