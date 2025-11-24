#!/bin/bash

# Check if the first argument (Maestro job URL) is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <MAESTRO_CLOUD_CONSOLE_URL>"
    exit 1
fi

MAESTRO_CLOUD_CONSOLE_URL=$1
API_KEY=rcv_JjijkepFCRgVcYJchzwTMMCVWduBuAmmpWtETyEpnznBKZrXvcirDxgxaRvpuUHT
RECIVO_ORG_ID=88bu4IyUYf1LTtr1igZTeFT0s3Q4F2p7

echo "Running Cypress with Maestro Cloud URL: $MAESTRO_CLOUD_CONSOLE_URL"

# Clean and install dependencies
rm -rf node_modules package-lock.json
npm install

# Clear old Cypress downloads and videos
rm -rf cypress/cypress/downloads/* cypress/cypress/videos/*

# Run Cypress
npx --prefix cypress cypress run --env \
projectUrl=$MAESTRO_CLOUD_CONSOLE_URL,\
recivoApiKey=$API_KEY,\
recivoOrgId=$RECIVO_ORG_ID
