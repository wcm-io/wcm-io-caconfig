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

  configTable.$inject = ["templateUrlList", "$rootScope", "currentConfigService", "$compile"];

  function configTable(templateList, $rootScope, currentConfigService, $compile) {
    var CONFIG_PROPERTY_INHERIT = "sling:configPropertyInherit";
    var CONFIG_INHERITED_CLASS = "caconfig-config-inherited";
    var CONFIG_NOT_INHERITED_CLASS = "caconfig-config-not-inherited";

    var propertyRowsCache = {};

    var directive = {
      require: "^form",
      templateUrl: templateList.configTable,
      scope: {
        config: "=caconfigConfigTable",
        collectionItemName: "@",
        index: "="
      },
      link: link
    };

    return directive;

    function link(scope, element, attrs, form) {
      var tableBody = element.find(".caconfig-configTableBody");
      var compiledPropertyRows,
          propertyRows;

      scope.i18n = $rootScope.i18n;
      scope.configPropertyInherit = currentConfigService.getConfigPropertyInherit(scope.index);

      scope.configTable = {
        handleConfigPropertyInheritChange: currentConfigService.handleConfigPropertyInheritChange,
        removeCollectionItem: function(index) {
          removeCollectionItem(index, form);
        },
        breakInheritance: function() {
          breakInheritance(scope, element, form);
        }
      };

      scope.ctReady = true;
      propertyRows = getPropertyRows(scope.config);
      compiledPropertyRows = $compile(propertyRows)(scope);
      tableBody.append(compiledPropertyRows);
    }

    function getPropertyRows(config) {
      var configPropertyTypes,
          propertyRowArray,
          propertyRowOptions,
          property,
          propertyRows,
          propertyType,
          numProps,
          i;

      if (propertyRowsCache[config.configName]) {
        return propertyRowsCache[config.configName];
      }

      configPropertyTypes = currentConfigService.getCurrent().propertyTypes;
      propertyRowArray = [];
      numProps = config.properties.length;

      for (i = 0; i < numProps; i++) {
        property = config.properties[i];

        if (property.name !== CONFIG_PROPERTY_INHERIT) {
          propertyType = configPropertyTypes[property.name];
          propertyRowOptions = {
            propIndex: i,
            type: propertyType,
            editLinkText: $rootScope.i18n.button.edit,
            property: property
          };

          propertyRowArray.push(getPropertyRow(propertyRowOptions));
        }
      }
      propertyRows = propertyRowArray.join("");
      propertyRowsCache[config.configName] = propertyRows;
      return propertyRows;
    }

    function getPropertyRow(obj) {
      var html = "<tr caconfig-property-row='config.properties[" + obj.propIndex + "]' "
        + "caconfig-property-inheritance-enabled='(!config.inherited && configPropertyInherit.value)'>"
        + getPropertyInputEl(obj)
        + "</tr>";
      return html;
    }

    function getPropertyInputEl(obj) {
      if (obj.type === "text" || obj.type === "number") {
        return "<td caconfig-property-input-text property='config.properties[" + obj.propIndex + "]'></td>";
      }

      if (obj.type === "checkbox") {
        return "<td caconfig-property-input-checkbox property='config.properties[" + obj.propIndex + "]'></td>";
      }

      if (obj.type === "multivalue") {
        return "<td caconfig-multifield property='config.properties[" + obj.propIndex + "]'></td>";
      }

      if (obj.type === "pathbrowser") {
        return "<td caconfig-pathbrowser property='config.properties[" + obj.propIndex + "]'></td>";
      }

      if (obj.type === "nestedConfig") {
        return "<td caconfig-property-edit-link config-name='" + obj.property.nestedConfig.configName + "' "
          + "link-text='" + obj.editLinkText + "'></td>";
      }

      if (obj.type === "nestedConfigCollection") {
        return "<td caconfig-property-edit-link config-name='" + obj.property.nestedConfigCollection.configName + "' "
          + "link-text='" + obj.editLinkText + "'></td>";
      }

      return "<td>" + obj.property.value + "</td>";
    }

    function removeCollectionItem(index, form) {
      currentConfigService.removeItemFromCurrentCollection(index);
      form.$setDirty();
    }

    function breakInheritance(scope, element, form) {
      element.removeClass(CONFIG_INHERITED_CLASS);
      element.addClass(CONFIG_NOT_INHERITED_CLASS);

      scope.config.inherited = false;
      scope.configPropertyInherit.value = true;

      currentConfigService.handleConfigPropertyInheritChange(scope.index);
      form.$setDirty();
    }
  }
}(angular));
