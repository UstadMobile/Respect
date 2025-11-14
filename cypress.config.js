const { defineConfig } = require('cypress');
const fs = require('fs');
const path = require('path');
const https = require('https');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {

      on('task', {
        // ---- Save any text file ----
        saveFile({ filePath, content }) {
          try {
            const folder = path.dirname(filePath);
            if (!fs.existsSync(folder)) fs.mkdirSync(folder, { recursive: true });
            fs.writeFileSync(filePath, content, 'utf8');
            console.log(`✅ Saved: ${filePath}`);
            return null;
          } catch (err) {
            console.error('❌ Error saving file:', err);
            throw err;
          }
        },

        // ---- Read any text file ----
        readFile(filePath) {
          try {
            if (!fs.existsSync(filePath)) throw new Error(`File not found: ${filePath}`);
            const content = fs.readFileSync(filePath, 'utf8');
            return content;
          } catch (err) {
            console.error('❌ Error reading file:', err);
            throw err;
          }
        },

        // ---- Download any video ----
        downloadVideo({ url, fileName }) {
          return new Promise((resolve, reject) => {
            if (!url || !fileName) return reject(new Error('Missing url or fileName'));

            const safeName = fileName.replace(/[<>:"/\\|?*]+/g, '_');
            const downloadsDir = path.join(__dirname, 'cypress', 'downloads');
            if (!fs.existsSync(downloadsDir)) fs.mkdirSync(downloadsDir, { recursive: true });

            const filePath = path.join(downloadsDir, `${safeName}.mp4`);
            const file = fs.createWriteStream(filePath);

            https.get(url, (res) => {
              if (res.statusCode !== 200) return reject(new Error(`Failed to download: ${res.statusCode}`));
              res.pipe(file);
              file.on('finish', () => file.close(() => {
                console.log(`Downloaded: ${filePath}`);
                resolve(filePath);
              }));
            }).on('error', (err) => {
              fs.unlink(filePath, () => reject(err));
            });
          });
        },
      });

      return config;
    },

    // Base settings
    baseUrl: 'https://signin.maestro.dev',
    video: true,
    videoCompression: 32,
    videoUploadOnPasses: false,
    defaultCommandTimeout: 30000,
    taskTimeout: 600000, // 10 minutes for big video downloads
  },
});
