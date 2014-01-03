'use strict';

module.exports = function(grunt) {

  grunt.initConfig({
    meta: {
      'transpiledDir': 'target/transpiled',
      'transpiledJs': ['<%=meta.transpiledDir%>/**/*.js'],
      'dependencies': ['vendor/amdloader/loader.js',
        'vendor/react/react.js',
        'vendor/es6-promises/promise.js',
        'vendor/mori/mori.min.js',
        'vendor/underscore/underscore.js',
        'vendor/js-signals/dist/signals.js',
        'vendor/hasher/dist/js/hasher.js',
        'vendor/crossroads/dist/crossroads.js'],
      'dependenciesMinified': ['vendor/amdloader/loader.js',
        'vendor/react/react.min.js',
        'vendor/es6-promises/promise.min.js',
        'vendor/mori/mori.min.js',
        'vendor/underscore/underscore-min.js',
        'vendor/js-signals/dist/signals.min.js',
        'vendor/hasher/dist/js/hasher.min.js',
        'vendor/crossroads/dist/crossroads.min.js']
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
      },

      es6Sources: {
        expand: true,
        cwd: 'js',
        src: ['**/*.js'],
        dest: 'target/es6-sources',
        ext: '.js'
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

    react: {
      files: {
        expand: true,
        cwd: 'js',
        src: ['**/*.jsx'],
        dest: 'target/es6-sources',
        ext: '.js'
      }
    },

    sass: {
      dev: {
        options: {
          style: 'expanded',
          debugInfo: true,
          lineNumbers: true,
          compass: true,
          require: ['sass-css-importer']
        },
        files: {
          'app.css': 'scss/app.scss'
        }
      }
    },

    transpile: {
      main: {
        type: 'amd', // or "amd" or "yui"
        files: [{
          expand: true,
          cwd: 'target/es6-sources',
          src: ['**/*.js'],
          dest: 'target/transpiled',
          ext: '.js'
        }]
      }
    },

    watch: {
      js: {
        files: 'js/**/*',
        tasks: ['compile', 'copy:devJs']
      },
      css: {
        files: 'scss/**/*',
        tasks: ['sass:dev']
      }
    }
  });

  [
    'grunt-contrib-clean',
    'grunt-contrib-concat',
    'grunt-contrib-copy',
    'grunt-contrib-sass',
    'grunt-contrib-uglify',
    'grunt-contrib-watch',
    'grunt-es6-module-transpiler',
    'grunt-react'
  ].forEach(grunt.loadNpmTasks.bind(grunt));


  grunt.registerTask('compile', [
    'react',
    'copy:es6Sources',
    'transpile',
    'concat'
  ]);
  grunt.registerTask('dev', ['clean',
    'compile',
    'copy:devJs',
    'sass:dev',
    'watch'
  ]);
  grunt.registerTask('release', ['clean',
    'compile',
    'uglify',
    'copy:releaseJs'
  ]);
  grunt.registerTask('default', ['release']);

};
