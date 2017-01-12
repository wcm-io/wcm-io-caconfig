module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: "",

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ["jasmine"],

    // list of files / patterns to load in the browser
    files: [
      "node_modules/angular/angular.js",
      "node_modules/angular-route/angular-route.js",
      "node_modules/angular-mocks/angular-mocks.js",
      "node_modules/jquery/dist/jquery.min.js",
      "node_modules/jasmine-jquery/lib/jasmine-jquery.js",
      "node_modules/lodash/lodash.min.js",
      "src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/js/**/*.module.js",
      "src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/js/**/!(*.spec).js",
      "src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/js/**/*.spec.js",
      {
        pattern: "src/test/webapp/fixtures/*.json",
        watched: true,
        served: true,
        included: false
      }
    ],

    preprocessors: {
      "./src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/js/**/*.js": "coverage"
    },

    // list of files / patterns to exclude
    exclude: [],

    // web server port
    port: 9876,

    // cli runner port
    runnerPort: 9100,

    reporters: ["dots", "coverage"],

    coverageReporter: {
      type: "html",
      dir: "./target/coverage"
    },
    junitReporter: {
      outputFile: "./target/surefire-reports/karma-results.xml"
    },

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ["Chrome"],


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true
  });
};