# RESPECT App Maestro tests

## Development environment setup:
* Complete development environment setup as per main [README](../../../../README.md)
* Install [Maestro CLI](https://github.com/mobile-dev-inc/Maestro/releases).

## Quick run an individual test:

* Build the project as per the main [README](../../../../README.md)
* Start app-server and add a school as per the main project README.
* Install the RESPECT APK on the Android Emulator or device being used to run tests
  e.g. run project using Android Studio, drag/drop file onto Android emulator, or install using adb command:
```
adb install ./app-android/build/outputs/apk/debug/app-android-debug.apk
```
* Install the latest version of NATIVE APK on the Android Emulator or device being used to run tests (If native app is supported and not installed yet to the device).

* Run the server

```
./gradlew app-server:run
```

* Set the school
```
./gradlew app-server:run --args='addschool --url <School_Url> --name <School_Name> --adminpassword <Admin_password>'
```

* Run test using Maestro CLI (specify the school URL and admin password):
```
cd app-cli/src/main/resources/flows

maestro test \
    -e SCHOOL_URL=http://192.168.1.2:8094/ \
    -e SCHOOL_ADMIN_PASSWORD=adminpassword \
    -e SCHOOL_NAME=TestSchool \
    -e TEST_APP_URL=https://app_manifest_url \
    -e TEST_APP_NAME="App Name" \
    -e TEST_APP_MODE=native|webView \
    -e OFFLINE_STATUS=true|False \
      flow_name.yaml
```

Where:
* ```SCHOOL_URL``` is the URL for the school as used with the addschool command as  as per the main
  [README](../../../../README.md)
* ```SCHOOL_ADMIN_PASSWORD``` is the password for the admin user for the school (also as per addschool command)
* ```SCHOOL_NAME``` is the name of the school (also as per addschool command)
* ```TEST_APP_URL``` is the app manifest URL - setup as per [README_ADD_YOUR_APP.md](../../../../README_ADD_YOUR_APP.md)
* ```TEST_APP_NAME``` is the app name (also as per README_ADD_YOUR_APP.md)
* ```OFFLINE_STATUS``` is the offline status (true/false) , If true test will run in offline mode
* ```TEST_APP_MODE``` is the app mode (native/webView) , If native test will run in native app and if webView test verify Web-view is visible
* ```flow_name.yaml``` is the test flow name 