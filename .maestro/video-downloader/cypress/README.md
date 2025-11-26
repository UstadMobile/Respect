# Maestro Test Video Automation – Cypress Suite

This cypress folder contains Cypress test that automate: Downloading screen-recording videos for each test.

All videos and test names are saved locally inside `.maestro/video-downloader/cypress/downloads/`.

---

## 1. Purpose


### **downloadTestVideos.cy.js**
Reads `testnames.txt` and downloads the corresponding video for every test into:

```

.maestro/video-downloader/cypress/downloads/

```

---

## 2. Prerequisites

- Node.js (>= 16)

Check if installed:

```bash
node -v
npm -v
````
If not installed:

```bash
sudo apt install nodejs npm
````
- Cypress (>= 13)

Install Cypress:

```bash
npm install cypress --save-dev
````
- Maestro account + login email
- Valid Maestro project URL (from Jenkins)

---

## 3. How the Tests Work

### **downloadTestVideos.cy.js**

1. Login to Maestro.
2. Navigate to the project page.
3. Collect all visible test names.
4. Save them into `testnames.txt` using `cy.task('saveFile')`. 
5. Load test names using `cy.task('readFile')`. 
6. Use `cy.origin()` for cross-domain navigation. 
7. For each test name:

    * Open the test details page.
    * Get `<video src="...">`
    * Call `cy.task('downloadVideo')` to download the mp4.
8. Save files into `cypress/downloads/`.

---

## 4. Cypress Tasks in `cypress.config.js`
* **getMaestroOtp** → Fetches the latest OTP for Maestro login.
* **readFile** → Reads `testnames.txt`
* **saveFile** → Writes text files
* **downloadVideo** → Downloads mp4 using HTTPS stream

These tasks must be defined inside `setupNodeEvents`.

---

## 5. Running the Tests

### **On Jenkins**

```bash
cd .maestro/video-downloader

./ci-run-cypress.sh

```
### **Manual - headless mode**

```bash
cd .maestro/video-downloader

npx cypress run --env maestroEmail=respecttester@recivo.email,projectUrl="https://app.robintest.com/project/proj_01k6wzp0pwf75vpcw8vw1gh0y8/maestro-test/app/app_01k6x3hyf9exztk0022nehadkk/upload/mupload_01kas5dx97eq7b4bc0d16w47z0",recivoApiKey="rcv_JjijkepFCRgVcYJchzwTMMCVWduBuAmmpWtETyEpnznBKZrXvcirDxgxaRvpuUHT",recivoOrgId="88bu4IyUYf1LTtr1igZTeFT0s3Q4F2p7"

```
---

## 6. Notes

* `cy.origin()` is required because Maestro uses different domains:

    * `https://signin.maestro.dev`
    * `https://app.maestro.dev`
* Ensure page loads fully before video extraction.
* Video download may take time depending on network speed.
