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
   * Directive for a config singleton or config collection item table
   */
  angular.module("io.wcm.caconfig.widgets")
    .directive("caconfigConfigTable", configTable);

  configTable.$inject = ["templateUrlList", "cssClasses", "propertyNames", "$rootScope", "currentConfigService", "$compile", "$timeout"];

  function configTable(templateList, cssClasses, propertyNames, $rootScope, currentConfigService, $compile, $timeout) {
    var propertyRowsCache = {};

    var directive = {
      templateUrl: templateList.configTable,
      scope: {
        config: "=caconfigConfigTable",
        isPreview: "=",
        index: "="
      },
      link: link
    };

    return directive;

    function link(scope, element) {
      scope.i18n = $rootScope.i18n;
      scope.configPropertyInherit = currentConfigService.getConfigPropertyInherit(scope.index);

      scope.configTable = {
        handleConfigPropertyInheritChange: currentConfigService.handleConfigPropertyInheritChange,
        removeCollectionItem: function(index) {
          removeCollectionItem(index);
        },
        breakInheritance: function() {
          breakInheritance(scope, element);
        }
      };

      scope.ctReady = true;

      if (scope.isPreview) {
        $timeout(function() {
          showPropertyRows(scope, element, true);

          element.on("click.detail focusin.detail", function(e) {
            e.preventDefault();
            e.stopPropagation();
            element.off("click.detail focusin.detail");
            showPropertyRows(scope, element);
          });
        }, false);
      }
      else {
        $timeout(function() {
          showPropertyRows(scope, element);
        }, false);
      }
    }

    /**
     * @param  {Object}  scope
     * @param  {jQuery}  element
     * @param  {Boolean} isPreview
     */
    function showPropertyRows(scope, element, isPreview) {
      var tableBody = element.find(".caconfig-configTableBody");
      var propertyRows = getPropertyRowsHtml(scope.config, isPreview);
      var compiledPropertyRows = $compile(propertyRows)(scope);

      element.css("min-height", element.height());
      element.toggleClass(cssClasses.CONFIG_PREVIEW, Boolean(isPreview));
      element.toggleClass(cssClasses.CONFIG_NOT_PREVIEW, !isPreview);

      tableBody.empty().append(compiledPropertyRows);
    }

    /**
     * @param  {Object}  config
     * @param  {Boolean} isPreview
     * @return {String}
     */
    function getPropertyRowsHtml(config, isPreview) {
      var configPropertyTypes,
        propertyRowOptions,
        property,
        propertyRows,
        propertyType,
        numProps,
        i;

      var configName = config.configName + (isPreview ? "-preview" : "");
      var getRowFn = isPreview ? getPropertyRowPreviewHtml : getPropertyRowHtml;

      if (propertyRowsCache[configName]) {
        return propertyRowsCache[configName];
      }

      configPropertyTypes = currentConfigService.getCurrent().propertyTypes;

      propertyRows = "";
      numProps = config.properties.length;

      for (i = 0; i < numProps; i++) {
        property = config.properties[i];

        if (property.name !== propertyNames.CONFIG_PROPERTY_INHERIT) {
          propertyType = configPropertyTypes[property.name];
          propertyRowOptions = {
            propIndex: i,
            type: propertyType,
            editLinkText: $rootScope.i18n.button.edit,
            property: property
          };

          propertyRows += getRowFn(propertyRowOptions);
        }
      }
      propertyRowsCache[configName] = propertyRows;
      return propertyRows;
    }

    /**
     * Get preview version of property row
     * @param  {Object} obj
     * @return {String}
     */
    function getPropertyRowPreviewHtml(obj) {
      var html = "<tr caconfig-property-row-preview=\"config.properties[" + obj.propIndex + "]\"></tr>";
      return html;
    }

    /**
     * @param  {Object} obj
     * @return {String}
     */
    function getPropertyRowHtml(obj) {
      var html = "<tr caconfig-property-row=\"config.properties[" + obj.propIndex + "]\" "
        + "caconfig-property-inheritance-enabled=\"(!config.inherited && configPropertyInherit.value)\">"
        + getPropertyInputHtml(obj)
        + "</tr>";
      return html;
    }

    /**
     * @param  {Object} obj
     * @return {String}
     */
    function getPropertyInputHtml(obj) {
      if (obj.type === "text" || obj.type === "number") {
        return "<td caconfig-property-input-text property=\"config.properties[" + obj.propIndex + "]\"></td>";
      }

      if (obj.type === "checkbox") {
        return "<td caconfig-property-input-checkbox property=\"config.properties[" + obj.propIndex + "]\"></td>";
      }

      if (obj.type === "multivalue") {
        return "<td caconfig-multifield property=\"config.properties[" + obj.propIndex + "]\"></td>";
      }

      if (obj.type === "pathbrowser") {
        return "<td caconfig-pathbrowser property=\"config.properties[" + obj.propIndex + "]\"></td>";
      }

      if (obj.type === "dropdown") {
        return "<td caconfig-property-dropdown property=\"config.properties[" + obj.propIndex + "]\"></td>";
      }

      if (obj.type === "nestedConfig") {
        return "<td caconfig-property-edit-link config-name=\"" + obj.property.nestedConfig.configName + "\" "
          + "link-text=\"" + obj.editLinkText + "\"></td>";
      }

      if (obj.type === "nestedConfigCollection") {
        return "<td caconfig-property-edit-link config-name=\"" + obj.property.nestedConfigCollection.configName + "\" "
          + "link-text=\"" + obj.editLinkText + "\"></td>";
      }

      return "<td>" + obj.property.value + "</td>";
    }

    /**
     * @param  {Number} index - index of item in configs array
     */
    function removeCollectionItem(index) {
      currentConfigService.removeItemFromCurrentCollection(index);
      $rootScope.configForm.$setDirty();
    }

    /**
     * Break config inheritance
     * @param  {object} scope
     * @param  {jQuery} element
     */
    function breakInheritance(scope, element) {
      element.removeClass(cssClasses.CONFIG_INHERITED);
      element.addClass(cssClasses.CONFIG_NOT_INHERITED);

      scope.config.inherited = false;
      scope.configPropertyInherit.value = true;

      currentConfigService.handleConfigPropertyInheritChange(scope.index);
      $rootScope.configForm.$setDirty();
    }
  }
}(angular));
