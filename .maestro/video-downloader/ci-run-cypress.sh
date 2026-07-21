#!/bin/bash
set -u
set -o pipefail

# ---- Sanity checks ---------------------------------------------------------

if [ -z "${MAESTRO_CLOUD_URL:-}" ]; then
    echo "Error: Could not find Maestro Cloud URL from previous Maestro run."
    exit 1
fi

echo "Detected Maestro Cloud URL: $MAESTRO_CLOUD_URL"

DOWNLOAD_DIR="cypress/downloads"
URL_FILE="$DOWNLOAD_DIR/video_urls.txt"
mkdir -p "$DOWNLOAD_DIR"
rm -f "$URL_FILE"   # fresh run every time

# ---- Run Cypress ------------------
export NVM_DIR="/home/jenkins/.nvm"
source "$NVM_DIR/nvm.sh"

nvm use 20.20.2

echo "Node version after switch:"
which node
node -v

echo "Installing dependencies..."
npm install

echo "Starting Cypress to extract video URLs..."
set +e
npx cypress run --browser chrome --env \
maestroEmail="${MAESTRO_EMAIL}",\
projectUrl="$MAESTRO_CLOUD_URL",\
recivoApiKey="${RECIVO_API_KEY}",\
recivoOrgId="${RECIVO_ORG_ID}"
EXIT_CODE=$?
set -e

if [ $EXIT_CODE -ne 0 ]; then
    echo "Cypress exited with code $EXIT_CODE. Checking for partially captured URLs..."
else
    echo "Cypress finished successfully."
fi

# ---- Download videos via wget ----------------------------------------------

if [ -s "$URL_FILE" ]; then
    echo "------------------------------------------------"
    echo "Video URL file found. Starting downloads..."
    echo "------------------------------------------------"

    while IFS= read -r line; do
        [ -z "$line" ] && continue

        url=$(echo "$line" | grep -o 'http.*' || true)
        clean_name=$(echo "$line" | sed "s/: http.*//")

        if [ -z "$url" ]; then
            echo "Warning: could not parse URL from line: $line"
            continue
        fi

        # Sanitize filename (strip characters that break the filesystem)
        safe_name=$(echo "$clean_name" | tr -c 'A-Za-z0-9._-' '_')
        outfile="$DOWNLOAD_DIR/${safe_name}.mp4"

        echo "Downloading: $clean_name -> $outfile"
        wget -q -O "$outfile" "$url" || echo "Warning: Failed to download $clean_name"

    done < "$URL_FILE"

    echo "------------------------------------------------"
    echo "Downloads completed in $DOWNLOAD_DIR"
    ls -lh "$DOWNLOAD_DIR"/*.mp4 2>/dev/null || echo "No mp4 files found."
else
    echo "Warning: $URL_FILE was not generated (or is empty). No videos to download."
fi

if [ $EXIT_CODE -ne 0 ]; then
    echo "Video downloader script encountered an error (Cypress exit $EXIT_CODE)."
    exit $EXIT_CODE
fi

echo "Process completed successfully."