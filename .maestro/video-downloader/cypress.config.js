const { defineConfig } = require('cypress');
const https = require('https');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      on('task', {
            log(message) {
              console.log(message);
              return null; // tasks must return null or a value, never undefined
            },
          });

          return config;
        },
    baseUrl: 'https://signin.maestro.dev',
    defaultCommandTimeout: 30000,
  },
});