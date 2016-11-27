module.exports = function (grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    src: {
      js: ['<%= pkg.config.jsPath %>*.js'],
      html: ['src/main/resources/angularjs-partials/*.html']
    },
    html2js: {
      templates: {
        options: {
          base: 'src/main/resources/angularjs-partials',
          module: 'io.wcm.caconfig.templates'
        },
        src: [ '<%= src.html %>' ],
        dest: '<%= pkg.config.jsPath %>Template.js'
      }
    },
    watch: {
      html: {
        files: ['src/main/resources/angularjs-partials/*.html'],
        tasks: ['html2js:templates']
      }
    }
  });
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-html2js');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.registerTask('build', function() {
    grunt.task.run('html2js:templates');
  });
};
