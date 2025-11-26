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

npx cypress run --env maestroEmail=<emailid>,projectUrl=<maestro_Url>,recivoApiKey=<recivo_Api_Key>,recivoOrgId=<recivo_Org_Id>

```
---

## 6. Notes

* `cy.origin()` is required because Maestro uses different domains:

    * `https://signin.maestro.dev`
    * `https://app.maestro.dev`
* Ensure page loads fully before video extraction.
* Video download may take time depending on network speed.
