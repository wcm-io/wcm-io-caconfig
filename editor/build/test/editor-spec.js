describe("Editor", function() {
  var scope, createController, httpBackend;

  beforeEach(function() {
    window.CUI = {};
    CUI["Modal"] = function(){
      return {
        show: function() {}
      }
    };
    module("io.wcm.config.editor", "io.wcm.config.utilities", "testApp");

    angular.module("testApp", function() { }).config(function(parametersProvider){
      var i18n = {};
      i18n.applicationFilter = "Filter Application";
      i18n.groupFilter = "Filter Group";
      parametersProvider.setConfig({i18n:i18n, url:"http://localhost/test.json",
        lockedParameterName: "lockedParameterNames"});
    });

    jasmine.getJSONFixtures().fixturesPath='/base/test/fixtures';
    inject(function ($rootScope, $controller, $httpBackend) {
      scope = $rootScope.$new();
      httpBackend = $httpBackend;

      createController = function () {
        return $controller('mainCtrl', {
          '$scope': scope
        });
      };

      httpBackend.whenGET("http://localhost/test.json").respond(
        getJSONFixture('parameters.json')
      );
      httpBackend.flush();
      createController();

    });
  });

  it("should filter the parameter collection on group selection", function() {
    expect(scope.displayedCollection.length).toBe(0);
    scope.$apply(function() {
      scope.currentFilter = {"group":["Dealer Locator"]}
    });
    expect(scope.displayedCollection.length).toBe(2);
  });

  it("should reset the displayed parameter collection on deselection", function() {
    expect(scope.displayedCollection.length).toBe(0);
    scope.$apply(function() {
      scope.currentFilter = {"group":["Dealer Locator"]}
    });
    expect(scope.displayedCollection.length).toBe(2);
    scope.$apply(function() {
      scope.currentFilter = {"group":[]}
    });
    expect(scope.displayedCollection.length).toBe(5);
  });

  it("should filter the parameter collection on application selection", function() {
    expect(scope.displayedCollection.length).toBe(0);
    scope.$apply(function() {
      scope.currentFilter = {"application":["/apps/sample"]}
    });
    expect(scope.displayedCollection.length).toBe(2);
  });

  it("should reset the displayed collection on application deselection", function() {
    expect(scope.displayedCollection.length).toBe(0);
    scope.$apply(function() {
      scope.currentFilter = {"application":["/apps/sample"]}
    });
    expect(scope.displayedCollection.length).toBe(2);
    scope.$apply(function() {
      scope.currentFilter = {"application":[]}
    });
    expect(scope.displayedCollection.length).toBe(5);
  });
});
