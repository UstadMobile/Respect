#!/bin/bash

# Script that will be run by TestServerController to run a server process

# CI (Jenkins Scenario)
# a) run-maestro-ci.sh starts TestServerController (where run command is set to this run-server.sh script).
#    This script will write the basic auth server admin password somewhere that run-runserver.sh can get it
# b) Maestro test calls TESTSERVERCONTROLLER_URL/start to start server process and get the allocated
#     port. run-server.sh also sets a shutdown url
# c) Maestro test uses the server process that was started. This can be using the created URL directly
#    or by string substitution (e.g. https://portnum.example.org/ where a reverse proxy is setup).
# d) Maestro test calls /stop?port=(portnum) to stop the server.

echo $(realpath $BASH_SOURCE)
echo "Workspace = $TESTSERVER_WORKSPACE"

SCRIPTDIR=$(realpath $(dirname $BASH_SOURCE))

ROOTDIR=$(realpath $SCRIPTDIR/../..)

echo $ROOTDIR

unzip -d $TESTSERVER_WORKSPACE $ROOTDIR/respect-server/build/distributions/respect-server-1.0.0.zip

# Could set the credentials required to create a new instance here.
$TESTSERVER_WORKSPACE/respect-server-1.0.0/bin/respect-server runserver \
     -P:ktor.deployment.port=$TESTSERVER_PORT \
     -P:ktor.deployment.shutdown.url=/shutdown
#    -serverauth foo \
#    -datadir $TESTSERVER_WORKSPACE/data



