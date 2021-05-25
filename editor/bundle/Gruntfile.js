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
        fix: true,
        configFile: ".eslintrc"
      },
      target: ["*.js", "src/main/webapp/app-root/clientlibs/io.wcm.caconfig.editor/**/*.js"]
    },
    watch: {
      html: {
        files: ["src/main/resources/angularjs-partials/*.html"],
        tasks: ["html2js:templates"]
      }
    }
  });
  grunt.registerTask("lint:js", ["eslint"]);
  grunt.registerTask("build", ["html2js:templates"]);
  grunt.registerTask("default", ["build"]);
};
