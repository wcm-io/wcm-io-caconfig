"use strict";

describe("ConfigCacheService", function () {
  var configCacheService,
      mockWindow;
  var store = {};

  beforeEach(function() {
    module(function($provide) {
      $provide.service("$window", function() {
        this.localStorage = {
          getItem: function(key) {
            return store[key];
          },
          setItem: function(key, value) {
            store[key] = value;
          },
          removeItem: function(key) {
            delete store[key];
          }
        };
      });
    });
    module("io.wcm.caconfig.editor");
    inject(function (_configCacheService_, $window) {
      configCacheService = _configCacheService_;
      mockWindow = $window;
    });
  });

  afterEach(function () {
    store = {};
  });

  describe("plantConfigCache", function() {
    it("should return an object", function () {
      var data,
          configCache;
      jasmine.getJSONFixtures().fixturesPath = "base/src/test/webapp/fixtures";
      data = getJSONFixture("configNames.json");
      configCache = configCacheService.plantConfigCache(data);
      expect(typeof configCache).toBe("object");
    });
  });

  describe("getConfigNameObject", function() {
    it("should return an object", function () {
      var configNameObject = configCacheService.getConfigNameObject("TEST");
      expect(typeof configNameObject).toBe("object");
    });
  });

});