module.exports = function (grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    src: {
      js: ['<%= pkg.config.jsPath %>*.js'],
      html: ['partials/*.html']
    },
    html2js: {
      templates: {
        options: {
          base: 'partials',
          module: 'io.wcm.caconfig.templates'
        },
        src: [ '<%= src.html %>' ],
        dest: '<%= pkg.config.jsPath %>Template.js'
      },
      test_templates: {
        options: {
          base: 'test/fixtures',
          module: 'io.wcm.caconfig.templates'
        },
        base: 'test/fixtures',
        module: 'io.wcm.caconfig.test.templates',
        src: [ 'test/fixtures/*.html' ],
        dest: 'test/test-Template.js'
      }
    },
    karma: {
      options: {
        configFile: "karma.conf.js"
      },
      unit: {
        autoWatch: true,
        singleRun: false
      },
      maven: {
        singleRun: true,
        autoWatch: false,
        browsers: ["PhantomJS"],
        reporters: ["dots", "junit", "coverage"],
        coverageReporter: {
          type: "cobertura",
          dir: "../target/cobertura"
        },
        junitReporter: {
          outputFile: "../target/surefire-reports/karma-results.xml"
        }
      }
    },
    watch: {
      html: {
        files: ['partials/*.html'],
        tasks: ['html2js:templates']
      }
    }
  });
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks("grunt-karma");
  grunt.loadNpmTasks('grunt-html2js');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.registerTask('build', function() {
    grunt.task.run('html2js:templates');
    grunt.task.run('html2js:test_templates');
    // disabled until tests are fixed/migrated
    /*grunt.task.run('karma:maven');*/
  });

  grunt.registerTask("test", ["html2js:test_templates", "karma:unit"]);

};
