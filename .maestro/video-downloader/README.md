# Video Downloader

This utility automates the extraction and downloading of video run artifacts from **Maestro Cloud** using **Cypress** and a supporting shell script (`ci-run-cypress.sh`).

---

## What It Does

1. **Reads the Maestro Cloud run URL**.
2. Launches a **Cypress script** that:
    - Logs into Maestro Cloud using OTP email flow.
    - Opens the project run page.
    - Extracts all test run **video URLs**.
    - Saves them to `cypress/downloads/video_urls.txt`.
3. The script then **downloads all videos** locally in `.mp4` format via `wget`.

## Prerequisites

- Node.js + npm installed
- Cypress installed
- bash + wget
- Access to:
    - Maestro Cloud
    - A working Cypress test script
    - A email address to use for Maestro Cloud login.

---

## How to Run

### CLI

```bash
cd .maestro/video-downloader

./ci-run-cypress.sh --env \
  maestroEmail=<emailId>,\
  projectUrl=<Project_Url>,\
  recivoApiKey=<Recivo_Api_Key>,\
  recivoOrgId=<Recivo_Org_Id>
```
---

## Outputs

Successfull execution generates video files in:
./cypress/downloads/

---