'use strict';

module.exports = function(grunt) {

  grunt.initConfig({
    meta: {
      'transpiledDir': 'target/transpiled',
      'transpiledJs': ['<%=meta.transpiledDir%>/**/*.amd.js'],
      'dependencies': ['vendor/amdloader/loader.js',
        'vendor/react/react.js',
        'vendor/es6-promises/promise.js',
        'vendor/mori/mori.min.js'],
      'dependenciesMinified': ['vendor/amdloader/loader.js',
        'vendor/react/react.min.js',
        'vendor/es6-promises/promise.min.js',
        'vendor/mori/mori.min.js']
    },

    clean: ['target/', 'app.js'],

    concat: {
      options: {
        separator: ';\n\n',
      },
      main: {
        src: ['<%= meta.dependencies %>', '<%= meta.transpiledJs %>'],
        dest: 'target/app.js'
      }
    },

    copy: {
      devJs: {
        src: ['target/app.js'],
        dest: 'app.js'
      },

      releaseJs: {
        src: ['target/app.min.js'],
        dest: 'app.js'
      }
    },

    uglify: {
      options: {
        report: 'gzip',
        preserveComments: false
      },
      main: {
        files: {
          'target/app.min.js': [
            '<%= meta.dependenciesMinified %>',
            '<%= meta.transpiledJs %>'
          ]
        }
      }
    },

    transpile: {
      main: {
        type: 'amd', // or "amd" or "yui"
        files: [{
          expand: true,
          cwd: 'js',
          src: ['**/*.js'],
          dest: '<%= meta.transpiledDir %>',
          ext: '.amd.js'
        }]
      }
    },

    watch: {
      js: {
        files: 'js/**/*.js',
        tasks: ['compile', 'copy:devJs']
      },
    },
  });

  [
    'grunt-contrib-clean',
    'grunt-contrib-concat',
    'grunt-contrib-copy',
    'grunt-contrib-uglify',
    'grunt-contrib-watch',
    'grunt-es6-module-transpiler'
  ].forEach(grunt.loadNpmTasks.bind(grunt));


  grunt.registerTask('compile', ['transpile', 'concat']);
  grunt.registerTask('dev', ['clean',
    'compile',
    'copy:devJs',
    'watch'
  ]);
  grunt.registerTask('release', ['clean',
    'compile',
    'uglify',
    'copy:releaseJs'
  ]);
  grunt.registerTask('default', ['release']);

};
