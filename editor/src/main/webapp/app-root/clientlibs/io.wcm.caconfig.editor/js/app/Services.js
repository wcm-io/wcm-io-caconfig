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
   * Services module
   */
  angular.module('io.wcm.caconfig.services', ['io.wcm.caconfig.utilities'])
    .provider("parameters", function() {
      var config = {};

      function Parameter($http, $q, config, utils) {

        /**
         * Load configuration names.
         */
        this.loadConfigNames = function () {
          return $http.get(config.configNamesUrl);
        };

        /**
         * Extracts the filter options for the specific filter
         * @param filter
         * @param propertyName
         * @param parameters
         */
        function extractFilter(filter, propertyName, parameters) {
          _.map(parameters, function(parameter){
            var value = parameter[propertyName];
            if (value) {
              var option = {value: value, label: value};
              if (utils.indexOfValueObject(filter.options, option) == -1) {
                filter.options.push(option);
              }
            }
          });
        }

        /**
         *
         * @param parameters
         */
        function extractFilters(parameters) {
          var applicationFilter = {
            name: config.i18n.applicationFilter, filterParameter:"application", options:[]
          };
          var groupFilter = {
            name: config.i18n.groupFilter, filterParameter:"group", options:[]
          };
          extractFilter(groupFilter, "group", parameters);
          extractFilter(applicationFilter, "application", parameters);

          var result = [];
          result.push(groupFilter);
          result.push(applicationFilter);
          return result;
        }

        this.loadParameters = function () {
          return $http.get(config.configDataUrl);
        };

        this.parseData = function(data) {
          var result = {
            filters: [],
            parameters: []
          };
          if (data.parameters){
            result.parameters = data.parameters;
            result.filters = extractFilters(data.parameters);
          }
          return result;
        };

        this.saveParameters = function(data) {

          function transformRequestAsFormPost(data, getHeaders){
            var headers = getHeaders();
            headers["Content-Type" ] = "application/x-www-form-urlencoded;charset=utf-8";
            return serializeData(data);
          }

          function serializeData(data) {
            if ( ! angular.isObject( data ) ) {
              return( ( data == null ) ? "" : data.toString() );
            }

            var lockedParameters = [];
            var buffer = [];
            _.map(data, function(parameter){
              if (!parameter.inherited) {
                if (Array.isArray(parameter.value)) {
                  for (var i=0; i<parameter.value.length; i++) {
                    if (parameter.value[i].key && parameter.value[i].value) {
                      // map data type 
                      buffer.push(
                          encodeURIComponent(parameter.name + "$key") + "=" +
                          encodeURIComponent(parameter.value[i] == null ? "" : parameter.value[i].key)
                      );
                      buffer.push(
                          encodeURIComponent(parameter.name) + "=" +
                          encodeURIComponent(parameter.value[i] == null ? "" : parameter.value[i].value)
                      );
                    }
                    else {
                      // string array data type
                      buffer.push(
                          encodeURIComponent(parameter.name) + "=" +
                          encodeURIComponent(parameter.value[i] == null ? "" : parameter.value[i])
                      );
                    }
                  }
                }
                else {
                  // all other data types
                  buffer.push(
                      encodeURIComponent(parameter.name) + "=" +
                      encodeURIComponent(parameter.value == null ? "" : parameter.value)
                  );
                }
              }
              if (parameter.locked && !parameter.lockedInherited) {
                buffer.push(
                    encodeURIComponent(config.lockedParameterName) + "=" +
                    encodeURIComponent(parameter.name)
                );
              }
            });

            var serialzedData = "";
            if (buffer.length > 0) {
              serialzedData = buffer.join( "&" );
            }
            
            // append the Sling specific _charset_:utf-8
            serialzedData = serialzedData + "&_charset_=utf-8";
            
            return serialzedData;
          }

          return $http({
            method: "post",
            url: config.url,
            transformRequest: transformRequestAsFormPost,
            data: data
          });
        };
      }

      this.setConfig = function(configData) {
        config = configData;
      };

      this.$get = ["$http", "$q", "EditorUtilities", function($http, $q, utils) {
        return new Parameter($http, $q, config, utils);
      }];
    });

})(angular);
