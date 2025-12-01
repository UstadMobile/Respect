const { defineConfig } = require('cypress');
const https = require('https');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
    },

    baseUrl: 'https://signin.maestro.dev',
    defaultCommandTimeout: 30000,
  },
});