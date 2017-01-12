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
"use strict";

describe("DataHelperService", function () {
  var dataHelperService;

  beforeEach(function() {
    module("io.wcm.caconfig.editor");
    inject(function (_dataHelperService_) {
      dataHelperService = _dataHelperService_;
    });
  });

  describe("parseConfigData", function() {
    var configProperties = {
      prop1: "test config prop"
    };

    var collectionProperties = {
      collectionProp: "test collection prop"
    };

    var newItem = {
      properties: configProperties
    };

    var configData = {
      newItem: newItem,
      properties: configProperties
    };

    var collectionData = {
      items: [null, null],
      newItem: newItem,
      properties: collectionProperties
    };

    var parsedCollectionData,
        parsedConfigData;

    beforeEach(function() {
      parsedConfigData = dataHelperService.parseConfigData(JSON.stringify(configData));
      parsedCollectionData = dataHelperService.parseConfigData(JSON.stringify(collectionData));
    });

    it("should parse a single config", function () {
      expect(parsedConfigData.configs.length).toEqual(1);
      expect(parsedConfigData.configs[0].properties).toEqual(configProperties);
    });

    it("should parse multiple items in a collection", function () {
      expect(parsedCollectionData.configs.length).toEqual(2);
      expect(parsedCollectionData.configs).toEqual([null, null]);
    });

    it("should parse newItem only if it is a collection", function () {
      expect(parsedConfigData.newItem).toEqual(null);
      expect(parsedCollectionData.newItem.properties).toEqual(configProperties);
    });

    it("should parse properties as collectionProperties only if it is a collection", function () {
      expect(parsedConfigData.collectionProperties).toEqual({});
      expect(parsedCollectionData.collectionProperties).toEqual(collectionProperties);
    });

  });


  describe("buildConfigData", function() {

    var config0,
        config1,
        config2,
        config3,
        nullConfig,
        currentCollection,
        currentConfig,
        emptyCollection,
        configData,
        configDataObj,
        nullConfigData,
        nullConfigDataObj,
        collectionData,
        collectionDataObj,
        emptyCollectionData,
        emptyCollectionDataObj;

    var ITEM_0 = "collection item 0";
    var ITEM_1 = "collection item 1";
    var ITEM_2 = "collection item 2";
    var COLLECTION_PROP = "collectionProperty";
    var COLLECTION_VALUE = "should apply if collection";
    var PROP_0 = "stringParam";
    var VALUE_0 = "This is a string parameter";
    var PROP_1 = "integerParam";
    var VALUE_1 = 123;
    var NESTED_CONFIG = "sub config";
    var NESTED_COLLECTION = "sub list";
    var EMPTY_STRING = "emptyString";
    var EMPTY_ARRAY_0 = "emptyArray0";
    var EMPTY_ARRAY_1 = "emptyArray1";
    var UNDEFINED_PROP = "valueIsUndefined";

    var collectionProperties = {};
    var prop0 = { name: PROP_0, value: VALUE_0 };
    var prop1 = { name: PROP_1, value: VALUE_1 };
    var inheritedProp0 = { name: PROP_0, value: VALUE_0, inherited: true };
    var overriddenProp1 = { name: PROP_1, value: VALUE_1, overridden: true };
    var nestedConfigProp = { name: NESTED_CONFIG, nestedConfig: {} };
    var nestedCollectionProp = { name: NESTED_COLLECTION, nestedConfigCollection: {} };

    collectionProperties[COLLECTION_PROP] = COLLECTION_VALUE;

    config0 = {
      properties: [prop0, prop1]
    };
    config1 = {
      collectionItemName: ITEM_0,
      properties: [prop0]
    };
    config2 = {
      collectionItemName: ITEM_1,
      properties: [prop1]
    };
    config3 = {
      collectionItemName: ITEM_2,
      properties: [
        inheritedProp0,
        overriddenProp1,
        nestedConfigProp,
        nestedCollectionProp
      ]
    };

    currentConfig = {
      isCollection: false,
      collectionProperties: collectionProperties,
      configs: [config0]
    };

    nullConfig = {
      isCollection: false,
      configs: [{
        properties: [
          {
            name: EMPTY_STRING,
            value: ""
          },
          {
            name: UNDEFINED_PROP,
            value: undefined
          },
          {
            name: EMPTY_ARRAY_0,
            value: []
          },
          {
            name: EMPTY_ARRAY_1,
            value: [null, undefined, ""]
          }
        ]
      }]
    };

    currentCollection = {
      isCollection: true,
      collectionProperties: collectionProperties,
      configs: [config1, config2, config3]
    };

    emptyCollection = {
      isCollection: true,
      collectionProperties: collectionProperties,
      configs: []
    };

    beforeEach(function() {
      configData = dataHelperService.buildConfigData(currentConfig);
      configDataObj = JSON.parse(configData);
      nullConfigData = dataHelperService.buildConfigData(nullConfig);
      nullConfigDataObj = JSON.parse(nullConfigData);
      collectionData = dataHelperService.buildConfigData(currentCollection);
      collectionDataObj = JSON.parse(collectionData);
      emptyCollectionData = dataHelperService.buildConfigData(emptyCollection);
      emptyCollectionDataObj = JSON.parse(emptyCollectionData);
    });

    it("should return a (JSON) string", function () {
      expect(typeof configData).toEqual("string");
      expect(typeof configDataObj).toEqual("object");
      expect(typeof collectionData).toEqual("string");
      expect(typeof collectionDataObj).toEqual("object");
    });

    it("should create a properties object with key/value pairs from config", function () {
      var configProps = configDataObj.properties;
      expect(typeof configProps).toEqual("object");
      expect(configProps[PROP_0]).toBeDefined();
      expect(configProps[PROP_0]).toBe(VALUE_0);
      expect(configProps[PROP_1]).toBeDefined();
      expect(configProps[PROP_1]).toBe(VALUE_1);
    });

    it("should set undefined and empty values to null", function () {
      var emptyProps = nullConfigDataObj.properties;
      expect(emptyProps[EMPTY_STRING]).toBeDefined();
      expect(emptyProps[EMPTY_STRING]).toBe(null);
      expect(emptyProps[EMPTY_ARRAY_0]).toBeDefined();
      expect(emptyProps[EMPTY_ARRAY_0]).toBe(null);
      expect(emptyProps[EMPTY_ARRAY_1]).toBeDefined();
      expect(emptyProps[EMPTY_ARRAY_1]).toBe(null);
      expect(emptyProps[UNDEFINED_PROP]).toBeDefined();
      expect(emptyProps[UNDEFINED_PROP]).toBe(null);
    });

    it("should populate an items array if collection", function () {
      var configItems = configDataObj.items;
      var collectionItems = collectionDataObj.items;
      var emptyItems = emptyCollectionDataObj.items;
      var item0Props = collectionItems[0].properties;
      var item1Props = collectionItems[1].properties;

      expect(configItems).toBeUndefined();
      expect(collectionItems).toBeDefined();
      expect(collectionItems.length).toEqual(currentCollection.configs.length);
      expect(emptyItems).toBeDefined();
      expect(emptyItems.length).toBe(emptyCollection.configs.length);

      expect(collectionItems[0].collectionItemName).toBe(ITEM_0);
      expect(collectionItems[1].collectionItemName).toBe(ITEM_1);
      expect(item0Props[PROP_0]).toBeDefined();
      expect(item0Props[PROP_0]).toBe(VALUE_0);
      expect(item1Props[PROP_1]).toBeDefined();
      expect(item1Props[PROP_1]).toBe(VALUE_1);
    });

    it("should include collectionProperties (only) if collection", function () {
      var configProps = configDataObj.properties;
      var collectionProps = collectionDataObj.properties;

      expect(configProps[COLLECTION_PROP]).toBeUndefined();
      expect(collectionProps[COLLECTION_PROP]).toBeDefined();
      expect(collectionProps[COLLECTION_PROP]).toBe(COLLECTION_VALUE);
    });

    it("should ignore inherited, overridden, nested props", function () {
      var collectionItems = collectionDataObj.items;
      var item2Props = collectionItems[2].properties;

      expect(collectionItems[2].collectionItemName).toBe(ITEM_2);
      expect(item2Props[PROP_0]).toBeUndefined();
      expect(item2Props[PROP_1]).toBeUndefined();
      expect(item2Props[NESTED_CONFIG]).toBeUndefined();
      expect(item2Props[NESTED_COLLECTION]).toBeUndefined();
    });
  });

});