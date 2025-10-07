#!/bin/bash

# Script used in CI environment (Continuous Integration - eg Jenkins) to run Maestro end to end tests (see
# .maestro for test flows

ROOTDIR=$(realpath $(dirname $BASH_SOURCE))

# Root directory for TestServerController to use (each server will get its own sub directory)
# TestServerController will create the directory automatically.
TESTSERVERCONTROLLER_BASEDIR="$ROOTDIR/build/testservercontroller/workspace"


TESTSERVERCONTROLLER_DOWNLOAD_URL="https://devserver3.ustadmobile.com/jenkins/job/TestServerController/4/artifact/build/distributions/testservercontroller-0.0.4.zip"
TESTSERVERCONTROLLER_BASENAME="testservercontroller-0.0.4"

echo "ROOTDIR=$ROOTDIR BASH_SOURCE=$BASH_SOURCE"

if [ ! -e $ROOTDIR/build/testservercontroller/$TESTSERVERCONTROLLER_BASENAME ]; then
    if [ ! -e $ROOTDIR/build/testservercontroller ]; then
        mkdir -p $ROOTDIR/build/testservercontroller
    fi

    wget --output-document=$ROOTDIR/build/testservercontroller/$TESTSERVERCONTROLLER_BASENAME.zip $TESTSERVERCONTROLLER_DOWNLOAD_URL
    unzip -d $ROOTDIR/build/testservercontroller/ \
          $ROOTDIR/build/testservercontroller/$TESTSERVERCONTROLLER_BASENAME.zip
fi

TESTCONTROLLER_BIN=$ROOTDIR/build/testservercontroller/$TESTSERVERCONTROLLER_BASENAME/bin/testservercontroller

if [ "$TESTCONTROLLER_PORT" == "" ]; then
    TESTCONTROLLER_PORT=8094
fi

if [ "$TESTCONTROLLER_URL" == "" ]; then
    if [ "$URL_SUBSTITUTION" != "" ]; then
        echo "ci-run-maestro: no TESTCONTROLLER_URL set: using hostname - this might not be correct"
        TESTCONTROLLER_URL=$(echo $URL_SUBSTITUTION | sed s/_PORT_/$TESTCONTROLLER_PORT/g)
    else
        TESTCONTROLLER_URL="http://$(hostname -I | xargs):$TESTCONTROLLER_PORT/"
    fi
fi

echo "ci-run-maestro: TESTCONTROLLER_URL is $TESTCONTROLLER_URL"

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


DIR_ADMIN_AUTH_PASS=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 13)
if [ "$SCHOOL_ADMIN_PASSWORD" == "" ]; then
    SCHOOL_ADMIN_PASSWORD=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 13)
fi

# The Maestro test needs to use basic auth (which is base64 encoded) to authenticate to request the
# creation of the school, that is encoded here and passed to Maestro to avoid using Maestro's
# Javascript (which does not have the btoa function)
DIR_ADMIN_TO_ENCODE="admin:$DIR_ADMIN_AUTH_PASS"
DIR_ADMIN_AUTH_HEADER="Basic $(printf '%s' $DIR_ADMIN_TO_ENCODE | base64)"

export JAVA_OPTS="-Dlogs_dir=$TESTSERVERCONTROLLER_BASEDIR/logs/"
$TESTCONTROLLER_BIN  \
    -P:ktor.deployment.port=$TESTCONTROLLER_PORT \
    -P:testservercontroller.portRange=$TEST_LEARNINGSPACE_PORTRANGE \
    -P:testservercontroller.urlsubstitution=$URL_SUBSTITUTION \
    -P:testservercontroller.basedir=$TESTSERVERCONTROLLER_BASEDIR \
    -P:testservercontroller.env.DIR_ADMIN_AUTH=$DIR_ADMIN_AUTH_PASS \
    -P:ktor.deployment.shutdown.url=/shutdown \
    -P:testservercontroller.shutdown.url=/shutdown \
    -P:testservercontroller.cmd="$ROOTDIR/ci-run-test-server.sh" &

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

if [ "$1" == "cloud" ]; then
    if [ "$MAESTRO_CLOUD_PROJECTID" == "" ]; then
      echo "Must set Maestro cloud project id as MAESTRO_CLOUD_PROJECTID environment var"
      exit 1
    fi

    if [ "$MAESTRO_CLOUD_APIKEY" == "" ]; then
      echo "Must set Maestro cloud API key as MAESTRO_CLOUD_APIKEY environment var"
      exit 1
    fi

    BRANCH_ARG=""
    PULLREQUEST_ARG=""
    NAME_ARG=""
    COMMIT_ARG=""
    BRANCH_ARG=""

    if [ "$BUILD_TAG" != "" ]; then
        NAME_ARG="--name=$BUILD_TAG"
    fi

    if [ "$GIT_BRANCH" != "" ]; then
        BRANCH_ARG="--branch=$BRANCH"
    fi

    if [ "$GIT_COMMIT" != "" ]; then
        COMMIT_ARG="--commit-sha=$GIT_COMMIT"
    fi

    if [ "$PULLREQUEST" != "" ]; then
        PULLREQUEST_ARG="--pull-request-id=$PULLREQUEST"
    fi

    maestro cloud \
        --api-key=$MAESTRO_CLOUD_APIKEY \
        --project-id=$MAESTRO_CLOUD_PROJECTID \
        --app-file=./respect-app-compose/build/outputs/apk/release/respect-app-compose-release.apk \
        --flows=.maestro/flows \
        --format=junit \
        --output=build/maestro/report.xml \
        --timeout=300 \
        $NAME_ARG \
        --repo-name=Respect \
        --repo-owner=UstadMobile \
        $COMMIT_ARG \
        $BRANCH_ARG \
        $PULLREQUEST_ARG \
        --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
        --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
        --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
        --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
        --env SCHOOL_NAME=TestSchool
    MAESTRO_STATUS=$?
else
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
fi


echo "ci-run-maestro: Maestro test completed. Workspaces are in $TESTSERVERCONTROLLER_BASEDIR"

exit $MAESTRO_STATUS
