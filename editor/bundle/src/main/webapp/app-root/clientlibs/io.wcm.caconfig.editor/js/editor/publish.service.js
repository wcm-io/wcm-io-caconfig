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
(function (angular, Granite) {
  "use strict";

  /**
   * Publish Services module
   * $http calls to the REST API
   */
  angular.module("io.wcm.caconfig.editor")
    .provider("publishService", PublishServiceProvider);

  function PublishServiceProvider() {
    var publishUrls = {};
    var DOCUMENT_REFERRER_KEY = "document.referrer";

    function PublishService($http, $httpParamSerializer, uiService, configService, modalService, restUrls) {
      var that = this;

      var publish = function (path) {
        $http.post(restUrls.replicationUrl, {
          _charset_: "utf-8",
          cmd: "Activate",
          path: path
        }, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded"
          },
          transformRequest: $httpParamSerializer
        })
          .then(
            function publishSuccess() {
              uiService.hideLoading();
              modalService.triggerEvent(modalService.modal.INFO, modalService.event.CUSTOM_MESSAGE, {
                message: Granite.I18n.get("The page has been published")
              });
            },
            function publishError(response) {
              uiService.hideLoading();
              // Error while publishing
              modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
                response: response,
                message: Granite.I18n.get("Failed to publish the selected page(s).")
              });
            }
          );
      };

      that.managePublication = function() {
        uiService.showLoading();
        location.href = restUrls.managePublicationUrl + "?item=" + restUrls.currentPagePath;
      }

      var navigateToQuickPublishWizard = function(path) {
        location.href = restUrls.quickPublishUrl + "?item=" + path + "&editmode";
      };

      that.quickPublish = function() {
        var path = restUrls.currentPagePath;
        sessionStorage.setItem(DOCUMENT_REFERRER_KEY, JSON.stringify(location.href));

        uiService.showLoading();

        $http.post(restUrls.referencesUrl, {
          path: path
        },
        {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded"
          },
          transformRequest: $httpParamSerializer,
          cache: false,
          responseType: "json"
        })
          .then(
            function success(response) {
              if (response.data && response.data.assets && response.data.assets.length === 0) {
                // Publish directly as there is no asset
                publish(path);
              }
              else {
                // Assets found then navigate to wizard
                navigateToQuickPublishWizard(path);
              }
            },
            function error(response) {
              uiService.hideLoading();
              // Error while getting assets
              modalService.triggerEvent(modalService.modal.ERROR, modalService.event.CUSTOM_MESSAGE, {
                response: response,
                message: Granite.I18n.get("Failed to retrieve references for the selected page.")
              });
            }
          );
      };
    }

    this.setRestUrls = function (restUrlsPublish) {
      publishUrls = restUrlsPublish;
    };

    this.$get = ["$http", "$httpParamSerializer", "uiService", "configService", "modalService",
      function ($http, $httpParamSerializer, uiService, configService, modalService) {
        return new PublishService($http, $httpParamSerializer, uiService, configService, modalService, publishUrls);
      }
    ];
  }

}(angular, Granite));
