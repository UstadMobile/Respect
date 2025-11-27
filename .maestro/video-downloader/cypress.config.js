const { defineConfig } = require('cypress');
const https = require('https');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
    },

    baseUrl: 'https://signin.maestro.dev',
    chromeWebSecurity: false,
    video: true, // Cypress recording of the test run
    defaultCommandTimeout: 30000,
    viewportWidth: 1920,
    viewportHeight: 1080,
  },
});