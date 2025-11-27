# Maestro Video Test Automation

## Overview

Automates Maestro platform login, test execution collection, and video playback with timestamp logging.

## Prerequisites

- Node.js v16+
- npm/yarn
- Cypress v14+

## Installation

```bash
git clone [repository]
cd [project]
npm install
npx cypress install
```

## Execution

```bash
npx cypress run --browser chrome \
--env maestroEmail=your@email.com \
--env projectUrl="your_project_url" \
--env recivoApiKey="your_key" \
--env recivoOrgId="your_org_id"
```
## Outputs:

- test_start_times.txt (timestamps in MM:SS format)
- Video recordings in cypress/videos/
- Screenshots on failure in cypress/screenshots/