#!/bin/bash
set -e  # Exit immediately if any command fails

# 1️⃣ Capture the Maestro Cloud URL from previous Maestro run logs
export MAESTRO_CLOUD_CONSOLE_URL=$(grep -o 'https://app\.robintest\.com/[^ ]*' $WORKSPACE/build/testservercontroller/workspace/lastMaestroRun.log | tail -1)

# Fail if URL is not found
if [ -z "$MAESTRO_CLOUD_CONSOLE_URL" ]; then
    echo "Error: Could not find Maestro Cloud URL from previous Maestro run."
    exit 1
fi

echo "Detected Maestro Cloud URL: $MAESTRO_CLOUD_CONSOLE_URL"

# 2️⃣ Jenkins environment variables already set
# Example if not set in Jenkins, you can temporarily define here
# RECIVO_API_KEY and RECIVO_ORG_ID can also come from Jenkins secrets
RECIVO_API_KEY=${RECIVO_API_KEY:-rcv_JjijkepFCRgVcYJchzwTMMCVWduBuAmmpWtETyEpnznBKZrXvcirDxgxaRvpuUHT}
RECIVO_ORG_ID=${RECIVO_ORG_ID:-88bu4IyUYf1LTtr1igZTeFT0s3Q4F2p7}

# Optional: show some variables for debugging (mask API key in logs if needed)
echo "Project ID: $MAESTRO_CLOUD_PROJECTID"
echo "Org ID: $RECIVO_ORG_ID"

# 3️⃣ Run Cypress with all environment variables
npx --prefix cypress cypress run --env \
projectUrl="$MAESTRO_CLOUD_CONSOLE_URL",\
recivoApiKey="$RECIVO_API_KEY",\
recivoOrgId="$RECIVO_ORG_ID"

# 4️⃣ Exit with Cypress exit code
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo "Cypress tests failed with exit code $EXIT_CODE"
    exit $EXIT_CODE
fi

echo "Cypress tests completed successfully."
