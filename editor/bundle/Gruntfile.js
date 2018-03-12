var loadGruntTasks = require("load-grunt-tasks");

module.exports = function (grunt) {
  // load all grunt prefixed tasks
  loadGruntTasks(grunt);

  grunt.initConfig({
    pkg: grunt.file.readJSON("package.json"),
    src: {
      js: ["<%= pkg.config.jsPath %>*.js"],
      html: ["src/main/resources/angularjs-partials/*.html"]
    },
    min: {
      dist: {
        src: ["<%= pkg.config.jsPath %>**/*.js"],
        dest: "target/yui-compression-test/io.wcm.caconfig.editor.min.js"
      },
      lib: {
        src: ["<%= pkg.config.angularPath %>**/*.js"],
        dest: "target/yui-compression-test/io.wcm.caconfig.editor.angularjs.min.js"
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
      }
    },
    eslint: {
      options: {
        configFile: ".eslintrc"
      },
      target: ["*.js", "src/main/webapp/clientlibs-root/io.wcm.caconfig.editor/**/*.js"]
    },
    watch: {
      html: {
        files: ["src/main/resources/angularjs-partials/*.html"],
        tasks: ["html2js:templates"]
      },
      js: {
        files: ["<%= pkg.config.jsPath %>**/*.js"],
        tasks: ["min"]
      }
    }
  });
  grunt.registerTask("lint:js", ["eslint"]);
  grunt.registerTask("build", ["html2js:templates", "min"]);
  grunt.registerTask("default", ["build"]);
};
