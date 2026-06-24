#!/bin/bash

# Script used to download file with test variables
# 1. Remove old build
  rm -rf build

# 2. Create directory - build
  mkdir -p build

  JENKINS_VAR_PATH=$1
  VAR_FILE_PATH=build/test_variables.txt

  echo "Saving Variable file from Jenkins to local"
  scp $JENKINS_VAR_PATH $VAR_FILE_PATH
  source $VAR_FILE_PATH

  echo "Saving APK file from Jenkins to local"
  scp $APP_PATH build

  adb uninstall world.respect.app
  adb install build/respect-app-compose-release.apk

#exit 0

   maestro test \
      --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
      --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
      --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
      --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
      --env SCHOOL_NAME=TestSchool \
      .maestro/flows/flow-passkey/*.yaml
#      $TEST_APP_URL_ARG \
#      --format=junit \
#      --output=build/report.xml \


