#!/bin/bash
set -e  # Exit immediately if any command fails

# Capture the Maestro Cloud URL from previous Maestro run logs
export MAESTRO_CLOUD_URL=$(grep -o 'https://app\.robintest\.com/[^ ]*' $WORKSPACE/build/testservercontroller/workspace/lastMaestroRun.log | tail -1)

# Fail if URL is not found
if [ -z "$MAESTRO_CLOUD_URL" ]; then
    echo "Error: Could not find Maestro Cloud URL from previous Maestro run."
    exit 1
fi

echo "Detected Maestro Cloud URL: $MAESTRO_CLOUD_URL"

maestroEmail="${maestroEmail:-respecttester@recivo.email}"
RECIVO_API_KEY="${RECIVO_API_KEY:-rcv_JjijkepFCRgVcYJchzwTMMCVWduBuAmmpWtETyEpnznBKZrXvcirDxgxaRvpuUHT}"
RECIVO_ORG_ID="${RECIVO_ORG_ID:-88bu4IyUYf1LTtr1igZTeFT0s3Q4F2p7}"


# Run Cypress with all environment variables
npx cypress run --env \
maestroEmail=$maestroEmail,\
projectUrl="$MAESTRO_CLOUD_URL",\
recivoApiKey="$RECIVO_API_KEY",\
recivoOrgId="$RECIVO_ORG_ID"

# Exit with Cypress exit code
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo "Cypress tests failed with exit code $EXIT_CODE"
    exit $EXIT_CODE
fi

echo "Cypress tests completed successfully."
