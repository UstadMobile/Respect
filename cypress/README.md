# Maestro Test Video Automation – Cypress Suite

This repository contains two Cypress tests that automate:
1. Extracting Maestro test names.
2. Downloading screen-recording videos for each test.

All videos and test names are saved locally inside `cypress/downloads/`.

---

## 📌 1. Purpose

### **A. saveTestNames.cy.js**
Scrapes all Maestro test/flow names from the project page and stores them in:

```

cypress/downloads/testnames.txt

```

### **B. downloadTestVideos.cy.js**
Reads `testnames.txt` and downloads the corresponding video for every test into:

```

cypress/downloads/videos/

```

---

## 2. Prerequisites

- Node.js (>= 16)
- Cypress (>= 13)
- Maestro account + login email
- Valid Maestro project URL (usually from Jenkins)

---

## 3. Passing Project URL (Manual or Jenkins)

You must pass the **project URL** when running Cypress:

### Manual:

```bash
cypress open --env projectUrl="https://app.maestro.dev/project/..."
````

### Jenkins:

```bash
--env projectUrl=${MAESTRO_PROJECT_URL}
```

Inside tests, Cypress reads it as:

```js
const projectUrl = Cypress.env('projectUrl');
```

---

## 4. How the Tests Work

### **saveTestNames.cy.js**

1. Login to Maestro.
2. Navigate to the project page.
3. Collect all visible test names.
4. Save them into `testnames.txt` using `cy.task('saveFile')`.

---

### **downloadTestVideos.cy.js**

1. Login to Maestro.
2. Load test names using `cy.task('readFile')`.
3. Use `cy.origin()` for cross-domain navigation.
4. For each test name:

    * Open the test details page.
    * Get `<video src="...">`
    * Call `cy.task('downloadVideo')` to download the mp4.
5. Save files into `cypress/downloads/videos/`.

---

## 5. Cypress Tasks in `cypress.config.js`

* **readFile** → Reads `testnames.txt`
* **saveFile** → Writes text files
* **downloadVideo** → Downloads mp4 using HTTPS stream

These tasks must be defined inside `setupNodeEvents`.

---

## 6. Running the Tests

### **Step 1 – Save test names**

```bash
cypress open --env projectUrl="YOUR_URL"
```

### **Step 2 – Download videos**

```bash
cypress open --env projectUrl="YOUR_URL"
```

---

## 7. Notes

* `cy.origin()` is required because Maestro uses different domains:

    * `https://signin.maestro.dev`
    * `https://app.maestro.dev`
* Ensure page loads fully before video extraction.
* Video download may take time depending on network speed.
