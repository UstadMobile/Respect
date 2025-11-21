const { defineConfig } = require('cypress');
const fs = require('fs');
const path = require('path');
const https = require('https');

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {

      on('task', {

    // ---- Fetch Maestro OTP via Recivo Mail API ----
     getMaestroOtp() {
       const orgId = process.env.RECIVO_ORG_ID || config.env.recivoOrgId;
       const apiKey = process.env.RECIVO_API_KEY || config.env.recivoApiKey;

       const fetchEmails = () => {
         return new Promise((resolve, reject) => {
           const url = `https://recivo.email/api/v1/organizations/${orgId}/inbox`;

           https.get(url, {
             headers: { Authorization: `Bearer ${apiKey}` }
           }, (res) => {
             let data = "";
             res.on("data", chunk => (data += chunk));
             res.on("end", () => {
               try {
                 resolve(JSON.parse(data));
               } catch (err) {
                 reject(err);
               }
             });
           }).on("error", reject);
         });
       };

       return new Promise(async (resolve, reject) => {
         const timeout = Date.now() + 30000; // retry up to 30 seconds

         while (Date.now() < timeout) {
           const now = Date.now();
           const emails = await fetchEmails();

           // Ensure emails is an array
           const emailArray = Array.isArray(emails) ? emails : [];

           const freshEmails = emailArray
             .filter(m => m.subject && m.subject.includes("Sign in to Maestro Cloud"))
             .filter(m => m.createdAt && (now - new Date(m.createdAt).getTime() <= 10000)) // <= 10 sec old
             .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

           if (freshEmails.length) {
             const latest = freshEmails[0];

             // Make sure latest.text exists and has OTP
             const match = latest.text?.match(/\b\d{6}\b/);
             if (match) {
               const otp = match[0];
               return resolve(otp);
             }
           }

           // small delay to avoid busy loop
           await new Promise(r => setTimeout(r, 500));
         }
         return reject(new Error("No fresh OTP found within timeout"));
       });
     },


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
