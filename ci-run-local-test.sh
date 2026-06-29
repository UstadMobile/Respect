#!/bin/bash

# Remove old build
  rm -rf build

# Create directory - build
  mkdir -p build

  touch build/test_vedio.mp4
  touch build/report.xml
  touch build/maestro-uploaded.txt

  JENKINS_VAR_PATH=$1
  VAR_FILE_PATH=build/test_variables.txt
  TEST_VIDEO_PATH=build/test_vedio.mp4
  TEST_REPORT_PATH=build/report.xml
  DONE_FLAG_FILE_PATH=build/maestro-uploaded.txt

  echo "Saving Variable file from Jenkins to local"
  scp $JENKINS_VAR_PATH $VAR_FILE_PATH
  source $VAR_FILE_PATH

  echo "Saving APK file from Jenkins to local"
  scp $APP_PATH build

  adb uninstall world.respect.app
  adb install build/respect-app-compose-release.apk


   maestro test \
      --env DIR_ADMIN_AUTH_PASS=$DIR_ADMIN_AUTH_PASS \
      --env TESTCONTROLLER_URL=$TESTCONTROLLER_URL \
      --env SCHOOL_ADMIN_PASSWORD=$SCHOOL_ADMIN_PASSWORD \
      --env DIR_ADMIN_AUTH_HEADER="$DIR_ADMIN_AUTH_HEADER" \
      --env SCHOOL_NAME=TestSchool \
      --format=junit \
      --output=build/maestro/output/report.xml \
      .maestro/flows/flow-passkey/*.yaml
      MAESTRO_STATUS=$?

      TEST_VIDEO_PATH=build/user_signup_using_passkey_test.mp4

# Copy the video file and report file to Jenkins
      echo "Saving Test files from local to Jenkins"
      scp $TEST_VIDEO_PATH $JENKINS_TEST_PATH
      scp $TEST_REPORT_PATH $JENKINS_TEST_PATH

# Write the status to the file
      echo $MAESTRO_STATUS > $DONE_FLAG_FILE_PATH

      echo "Saving done flag to Jenkins"
      scp $DONE_FLAG_FILE_PATH $DONE_FLAG_TEST_PATH

      exit $MAESTRO_STATUS



