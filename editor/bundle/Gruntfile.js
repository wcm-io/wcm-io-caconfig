module.exports = function (grunt) {
  require("load-grunt-tasks")(grunt);

  grunt.initConfig({
    pkg: grunt.file.readJSON("package.json"),
    src: {
      js: ["<%= pkg.config.jsPath %>*.js"],
      html: ["src/main/resources/angularjs-partials/*.html"]
    },
    min: {
      dist: {
        src: ["<%= pkg.config.jsPath %>**/!(*.spec).js"],
        dest: "target/yui-compression-test/io.wcm.caconfig.editor.js"
      }
    },
    html2js: {
      templates: {
        options: {
          base: "src/main/resources/angularjs-partials",
          module: "io.wcm.caconfig.templates"
        },
        src: ["<%= src.html %>"],
        dest: "<%= pkg.config.jsPath %>templates.module.js"
      },
      test_templates: {
        options: {
          base: "test/webapp/fixtures",
          module: "io.wcm.caconfig.templates"
        },
        src: ["test/webapp/fixtures/*.html"],
        dest: "test/webapp/test-templates.module.js"
      }
    },
    eslint: {
      options: {
        configFile: ".eslintrc"
      },
      target: ["src/**/*.js"]
    },
    stylelint: {
      options: {
        configFile: ".stylelintrc"
      },
      src: ["src/**/*.css"]
    },
    karma: {
      options: {
        configFile: "karma.conf.js"
      },
      single: {
        singleRun: true
      },
      watch: {
        autoWatch: true,
        singleRun: false
      }
    },
    shell: {
      installSelenium: {
        command: "./node_modules/grunt-protractor-runner/node_modules/protractor/bin/webdriver-manager update"
      }
    },
    protractor: {
      options: {
        configFile: "src/test/webapp/e2e-tests/protractor.conf.js",
        keepAlive: true
      },
      e2e: {}
    },
    watch: {
      html: {
        files: ["src/main/resources/angularjs-partials/*.html"],
        tasks: ["html2js:templates"]
      },
      js: {
        files: ["<%= pkg.config.jsPath %>**/!(*.spec).js"],
        tasks: ["min"]
      }
    }
  });
  grunt.registerTask("test:unit", ["karma:single"]);
  grunt.registerTask("test:e2e", ["shell:installSelenium", "protractor:e2e"]);
  grunt.registerTask("test", ["test:unit", "test:e2e"]);
  grunt.registerTask("lint:js", ["eslint"]);
  grunt.registerTask("lint:css", ["stylelint"]);
  grunt.registerTask("build", ["html2js:templates", "min"]);
  grunt.registerTask("default", ["build"]);
};
