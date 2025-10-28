#!/bin/bash

# Script used on CI (Continuous Integration - eg Jenkins) to run a test server process for a specific
# test. This script will be run by TestServerController
# (see https://github.com/UstadMobile/TestServerController ). You SHOULD NOT run it manually unless
# required to debug the script itself.

# CI (Jenkins Scenario)
# a) ci-run-maestro.sh starts TestServerController (where run command is set to this script).
#    ci-run-maestro.sh will save the admin password to dir-admin.txt in advance (allowing the test
#    itself to add a new school)
# b) Maestro test calls TESTSERVERCONTROLLER_URL/start to start server process and get the allocated
#     port. ci-run-test-server.sh also sets a shutdown url
# c) Maestro test uses the server process that was started. This can be using the created URL directly
#    or by string substitution (e.g. https://portnum.example.org/ where a reverse proxy is setup).
# d) Maestro test calls /stop?port=(portnum) to stop the server.

# TESTSERVER_WORKSPACE and TESSTSERVER_PORT is set by TestServerController.
echo "ci-run-test-server.sh: Workspace = $TESTSERVER_WORKSPACE Port=$TESTSERVER_PORT"

ROOTDIR=$(realpath $(dirname $BASH_SOURCE))

echo $ROOTDIR

unzip -q -d $TESTSERVER_WORKSPACE $ROOTDIR/respect-server/build/distributions/respect-server-1.0.0.zip

DATADIR=$TESTSERVER_WORKSPACE/data
mkdir -p $DATADIR

# Set the directory server admin authentication (passed from the TestServerController)
echo $DIR_ADMIN_AUTH > $DATADIR/dir-admin.txt

echo "ci-run-test-server.sh: saved admin auth to $DATADIR/dir-admin.txt"

export JAVA_OPTS="-Dlogs_dir=$TESTSERVER_WORKSPACE/logs/"
echo "ci-run-test-server.sh starting server :"
echo $TESTSERVER_WORKSPACE/respect-server-1.0.0/bin/respect-server runserver \
          -P:ktor.deployment.port=$TESTSERVER_PORT \
          -P:ktor.deployment.shutdown.url=/api/shutdown \
          -P:ktor.respect.datadir=$TESTSERVER_WORKSPACE/data \

# Could set the credentials required to create a new instance here.
$TESTSERVER_WORKSPACE/respect-server-1.0.0/bin/respect-server runserver \
     -P:ktor.deployment.port=$TESTSERVER_PORT \
     -P:ktor.deployment.shutdown.url=/api/shutdown \
     -P:ktor.respect.datadir=$TESTSERVER_WORKSPACE/data \

