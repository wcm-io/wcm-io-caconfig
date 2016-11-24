describe("Directives", function() {
  var scope, element, parameter, $compile;
  beforeEach(function() {
    window.CUI = {};
    CUI["Modal"] = function(){
      return {
        show: function() {}
      }
    };
    module("io.wcm.config.directives", "io.wcm.config.test.templates");
  });

  describe("parameterValue", function() {
    beforeEach(function () {
      inject(function ($rootScope, $compile, $templateCache) {
        var template =  $templateCache.get("editorTable.html")
        element = angular.element(template);
        scope = $rootScope.$new();
        scope.displayedCollection = [];
        parameter = {
          "label": "String Parameter",
          "name": "string-parameter",
          "value": "Current Value",
          "group": "Group",
          "application": "/apps/sample",
          "description":"Description",
          "inherited": false,
          "inheritedValue": "Inherited Value",
          "locked": false,
          "widgetType": "textfield",
          "lockedInherited": false
        };
        scope.displayedCollection.push(parameter);
        element = $compile(element)(scope);
        element.scope().$digest();
      });
    });

    xit("should show inherited value when inherited is selected", function() {
      expect(element).not.toBeNull();
      var input = $(".coral-Textfield", element);
      expect(input.val()).toBe("Current Value");
      parameter.inherited = true;
      scope.$digest();

      input = $(".coral-Textfield", element);
      expect(input.length).toBe(0);
      expect($("span", element).text()).toBe("Inherited Value");
    });

    xit("should show previously entered value if inherited was deselected", function() {
      parameter.value = "Newly entered value";
      parameter.inherited = true;
      scope.$digest();

      parameter.inherited = false;
      scope.$digest();
      var input = $(".coral-Textfield", element);
      expect(input.val()).toBe("Newly entered value")
    });

    it("should be disabled, if the parameter was locked in the higher level", function() {
      parameter.locked = true;
      parameter.lockedInherited = true;
      parameter.inherited = true;
      scope.$digest();
      var input = $(".coral-Textfield", element);
      expect(input.length).toBe(0);

      parameter.inherited = false;
      scope.$digest();
      var input = $(".coral-Textfield", element);
      expect(input.length).toBe(0);
    });
  });
});
