const { defineConfig } = require('cypress');
const https = require('https');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
    },

    baseUrl: 'https://signin.maestro.dev',
    chromeWebSecurity: false,
    video: true, // Cypress recording of the test run
    videoCompression: false,
    defaultCommandTimeout: 30000,
    viewportWidth: 1536,  // https://docs.cypress.io/api/commands/viewport#Arguments
    viewportHeight: 960,
  },
});