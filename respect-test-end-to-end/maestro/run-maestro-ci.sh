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
if [ "$SCHOOL_ADMIN_PASSWORD" == "" ]; then
    SCHOOL_ADMIN_PASSWORD=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 13)
fi

# The Maestro test needs to use basic auth (which is base64 encoded) to authenticate to request the
# creation of the school, that is encoded here and passed to Maestro to avoid using Maestro's
# Javascript (which does not have the btoa function)
DIR_ADMIN_TO_ENCODE="admin:$DIR_ADMIN_AUTH_PASS"
DIR_ADMIN_AUTH_HEADER="Basic $(printf '%s' $DIR_ADMIN_TO_ENCODE | base64)"

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

if [ ! -e build/results ]; then
    mkdir -p build/results
fi

maestro test \
  --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
  --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
  --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
  --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
  e2e-tests/000_000_hello_world.yaml



