var BASE_URL = "http://localhost:4502";

exports.config = {
  baseUrl: BASE_URL,
  capabilities: {
    browserName: "chrome",
    chromeOptions: {
      args: ["--test-type"]
    }
  },
  framework: "jasmine",
  // seleniumServerJar: "",
  // seleniumAddress: "http://localhost:4444/wd/hub",
  specs: ["*.js"],
  onPrepare: function() {
    var dvr = browser.driver;
    dvr.get(BASE_URL + "/libs/granite/core/content/login.html");
    dvr.findElement(by.id("username")).sendKeys("admin");
    dvr.findElement(by.id("password")).sendKeys("admin");
    dvr.findElement(by.id("submit-button")).click();
  }
};