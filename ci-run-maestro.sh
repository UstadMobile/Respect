#!/bin/bash

# Script used on CI (Continuous Integration - eg Jenkins) to run Maestro end to end tests (see 
# .maestro for test flows

SCRIPTDIR=$(realpath $(dirname $BASH_SOURCE))

# Root directory for TestServerController to use (each server will get its own sub directory)
# TestServerController will create the directory automatically.
TESTSERVERCONTROLLER_BASEDIR="$SCRIPTDIR/build/testservercontroller/workspace"

if [ "$TESTCONTROLLER_PORT" == "" ]; then
    TESTCONTROLLER_PORT=8094
fi

if [ "$TESTCONTROLLER_URL" == "" ]; then
    TESTCONTROLLER_URL="http://$(hostname -I | xargs):$TESTCONTROLLER_PORT/"
    echo "ci-run-maestro: ATTN: TESTCONTROLLER_URL not set. Setting to $TESTCONTROLLER_URL (this might not be correct)"
fi

if [ "$TEST_LEARNINGSPACE_PORTRANGE" == "" ]; then
    TEST_LEARNINGSPACE_PORTRANGE="8000-9000"
fi

function cleanup() {
    if [ "$TESTCONTROLLER_PID" != "" ]; then
        echo "ci-run-maestro: note 'No instance for key AttributeKey: KOIN_SCOPE' can be safely ignored"
        echo "ci-run-maestro: Stopping TestServerController"
        wget -qO- "${TESTCONTROLLER_URL}shutdown"
        sleep 10
        if [ -d "/proc/$PID" ]; then
            echo "ci-run-maestro: calling kill just in case (no such process error can be ignored)"
            kill $TESTCONTROLLER_PID
        fi
    fi
}

trap cleanup EXIT

TESTCONTROLLER_BIN=/home/mike/tmp/testservercontroller-0.0.2/bin/testservercontroller

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
    -P:testservercontroller.basedir=$TESTSERVERCONTROLLER_BASEDIR \
    -P:testservercontroller.env.DIR_ADMIN_AUTH=$DIR_ADMIN_AUTH_PASS \
    -P:ktor.deployment.shutdown.url=/shutdown \
    -P:testservercontroller.shutdown.url=/shutdown \
    -P:testservercontroller.cmd="$SCRIPTDIR/ci-run-test-server.sh" &

TESTCONTROLLER_PID=$!

wait-port $TESTCONTROLLER_PORT
echo "ci-run-maestro: TestServerController now running on port $TESTCONTROLLER_PORT (pid $TESTCONTROLLER_PID)"

# Can now run maestro - the TESTSERVERCONTROLLER url is known and we also know the admin auth to create a new school etc.

echo "Run Maestro using $TESTSERVERCONTROLLER_URL and $DIR_ADMIN_AUTH_PASS"

if [ ! -e build/results ]; then
    mkdir -p build/results
fi

if [ ! -e build/maestro/results ]; then
    mkdir -p build/maestro/output
fi

maestro test \
  --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
  --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
  --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
  --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
  --env SCHOOL_NAME=TestSchool \
  --format=junit \
  --test-output-dir=build/maestro/output \
  --output=build/maestro/report.xml \
  .maestro/flows/000_000_hello_world.yaml
MAESTRO_STATUS=$?

echo "ci-run-maestro: Maestro test completed. Workspaces are in $TESTSERVERCONTROLLER_BASEDIR"

exit $MAESTRO_STATUS
