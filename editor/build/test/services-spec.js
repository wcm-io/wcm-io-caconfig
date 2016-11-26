describe("parameters service", function() {
  var parameters, httpBackend, utils;

  beforeEach(function() {
    module("io.wcm.caconfig.services", "io.wcm.caconfig.utilities", "testApp");

    angular.module("testApp", function() { }).config(function(parametersProvider){
      var i18n = {};
      i18n.applicationFilter = "Filter Application";
      i18n.groupFilter = "Filter Group";
      parametersProvider.setConfig({i18n: i18n, url:"http://localhost/test.json", lockedParameterName: "lockedParameterNames"});
    });
  });

  describe("loadParameters", function() {

    beforeEach(function() {

      jasmine.getJSONFixtures().fixturesPath='/base/test/fixtures';
      inject(function(_parameters_, $httpBackend) {
        httpBackend = $httpBackend;
        parameters = _parameters_;

        httpBackend.whenGET("http://localhost/test.json").respond(
          getJSONFixture('parameters.json')
        );
      });
    });

    it("should load parameters from configured url", function() {
      parameters.loadParameters().then(function(respond) {
        expect(respond.data.parameters).not.toBeUndefined();
        expect(respond.data.parameters.length).toEqual(5);
      });
      httpBackend.flush();
    });

    it("should extract group filters from parameters", function() {
      parameters.loadParameters().then(function(respond) {
        var results = parameters.parseData(respond.data);
        expect(results).not.toBeUndefined();

        expect(results.filters).not.toBeUndefined();
        expect(results.filters[0]).not.toBeUndefined();
        expect(results.filters[0].options.length).toBe(2);
        expect(results.filters[0].name).toBe("Filter Group");
      });
      httpBackend.flush();

    });

    it("should extract application filters from parameters", function() {
      parameters.loadParameters().then(function(respond) {
        var results = parameters.parseData(respond.data);
        expect(results).not.toBeUndefined();

        expect(results.filters).not.toBeUndefined();
        expect(results.filters[1]).not.toBeUndefined();
        expect(results.filters[1].options.length).toBe(2);
        expect(results.filters[1].name).toBe("Filter Application");
      });
      httpBackend.flush();
    });

    it("should add parameters to the result", function() {
      parameters.loadParameters().then(function(respond) {
        var results = parameters.parseData(respond.data);
        expect(results).not.toBeUndefined();

        expect(results.parameters).not.toBeUndefined();
        expect(results.parameters.length).toBe(5);
      });
      httpBackend.flush();
    });
  });

  describe("saveParameters", function() {
    var loadedData;
    beforeEach(function() {
      jasmine.getJSONFixtures().fixturesPath='/base/test/fixtures';
      inject(function(_parameters_, $httpBackend, _EditorUtilities_) {
        httpBackend = $httpBackend;
        parameters = _parameters_;
        utils = _EditorUtilities_;
        loadedData = getJSONFixture('parameters.json');
      });
    });

    it("should post as regular form", function() {
      httpBackend.whenPOST('http://localhost/test.json').respond(function(method, url, data, headers){
        expect(method).toEqual("POST");
        expect(headers["Content-Type"]).toBe("application/x-www-form-urlencoded;charset=utf-8");
        expect(angular.isObject(data)).toBeFalsy("data is posted as json");
        return [200, {}, {}];
      });
      parameters.saveParameters(loadedData.parameters);
      httpBackend.flush();
    });

    it("should post only modified values, locked parameter names and charset", function() {

      httpBackend.whenPOST('http://localhost/test.json').respond(function(method, url, data, headers){
        var nameValuePairs = data.split("&");
        expect(nameValuePairs).not.toBeUndefined();
        expect(nameValuePairs.length).toBe(6, "wrong number of parameters");
        expect(
          utils.contains(nameValuePairs, encodeURIComponent("checkbox-param") + "=" + encodeURIComponent("true"))).toBeTruthy("parameter is missing");
        expect(
          utils.contains(nameValuePairs, encodeURIComponent("path-param") + "=" + "")).toBeTruthy("parameter is missing");
        expect(
          utils.contains(nameValuePairs, encodeURIComponent("digits") + "=" + "123")).toBeTruthy("parameter is missing");
        expect(
          utils.contains(nameValuePairs, encodeURIComponent("lockedParameterNames") + "=string-param")).toBeTruthy("parameter is missing");
        expect(
          utils.contains(nameValuePairs, encodeURIComponent("lockedParameterNames") + "=digits")).toBeTruthy("parameter is missing");
        expect(
          utils.contains(nameValuePairs, "_charset_=utf-8")).toBeTruthy("parameter is missing");
        return [200, {}, {}];
      });
      parameters.saveParameters(loadedData.parameters);
      httpBackend.flush();
    });

  });


});
