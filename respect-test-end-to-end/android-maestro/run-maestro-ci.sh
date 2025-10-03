#!/bin/bash

# Environment variables:
# TESTCONTROLLER_PORT - port that the testcontroller will run on

SCRIPTDIR=$(realpath $(dirname $BASH_SOURCE))

if [ "$TESTCONTROLLER_PORT" == "" ]; then
    TESTCONTROLLER_PORT=8094
fi

if [ "$TESTCONTROLLER_URL" == "" ]; then
    TESTCONTROLLER_URL="http://$(hostname -I | xargs):$TESTCONTROLLER_PORT/"
    echo "run-maestro-ci: ATTN: TESTCONTROLLER_URL not set. Setting to $TESTCONTROLLER_URL (this might not be correct)"
fi

if [ "$TEST_LEARNINGSPACE_PORTRANGE" == "" ]; then
    TEST_LEARNINGSPACE_PORTRANGE="8000-9000"
fi

function cleanup() {
    if [ "$TESTCONTROLLER_PID" != "" ]; then
        echo "run-maestro-ci: Stopping TestServerController"
        wget -qO- "${TESTCONTROLLER_URL}shutdown"
        sleep 10
        echo "run-maestro-ci: calling kill just in case (no such process error can be ignored)"
        kill $TESTCONTROLLER_PID
    fi
}

trap cleanup EXIT

TESTCONTROLLER_BIN=/home/mike/tmp/testservercontroller-0.0.1/bin/testservercontroller

DIR_ADMIN_AUTH_PASS=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 13)

$TESTCONTROLLER_BIN -P:ktor.deployment.port=$TESTCONTROLLER_PORT \
    -P:testservercontroller.portRange=$TEST_LEARNINGSPACE_PORTRANGE \
    -P:testservercontroller.env.DIR_ADMIN_AUTH=$DIR_ADMIN_AUTH_PASS \
    -P:ktor.deployment.shutdown.url=/shutdown \
    -P:testservercontroller.shutdown.url=/shutdown \
    -P:testservercontroller.cmd="$SCRIPTDIR/run-server.sh" &

TESTCONTROLLER_PID=$!

wait-port $TESTCONTROLLER_PORT
echo "run-maestro-ci: TestServerController now running on port $TESTCONTROLLER_PORT (pid $TESTCONTROLLER_PID)"

# Can now run maestro - the TESTSERVERCONTROLLER url is known and we also know the admin auth to create a new school etc.

echo "Run Maestro using $TESTSERVERCONTROLLER_URL and $DIR_ADMIN_AUTH_PASS"

sleep 10


