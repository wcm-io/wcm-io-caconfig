var APP_URL = "/config.html";
var APP_TITLE = "Configuration Editor";
var SAMPLE_PATH = "/content/contextaware-config-sample/en";
var DEBUG_QUERY = "?debugClientLibs=true";

describe("App", function() {
  it("should have a title and title should be visible", function() {
    browser.get(SAMPLE_PATH + APP_URL + DEBUG_QUERY);
    expect(browser.getTitle()).toEqual(APP_TITLE);
    expect(browser.findElement(by.css(".caconfig-title")).getText()).toEqual(APP_TITLE);
    expect(browser.findElement(by.css(".caconfig-contextPath")).getText()).toContain(SAMPLE_PATH);
  });

  it("should display the context path", function() {
    browser.get(SAMPLE_PATH + APP_URL + DEBUG_QUERY);
    expect(browser.findElement(by.css(".caconfig-contextPath")).getText()).toContain(SAMPLE_PATH);
  });
});