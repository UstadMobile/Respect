# RESPECT App Maestro tests

These tests let developers of RESPECT-compatible apps verify that their app works with RESPECT.
Use the latest release from [GitHub Releases](https://github.com/UstadMobile/Respect/releases/latest).

## Development environment setup:
* Install [Maestro CLI](https://github.com/mobile-dev-inc/Maestro/releases).
* Install [adb](https://developer.android.com/tools/adb) (part of Android platform-tools), and have an
  Android emulator or device available.
* Download the following from the [latest RESPECT release](https://github.com/UstadMobile/Respect/releases/latest):
  * The RESPECT Android APK
  * The RESPECT app-server package
* Get the test flows (the `app-cli/src/main/resources/flows` folder) by cloning the repository or downloading it as a zip.

## Quick run an individual test:

* Install the RESPECT APK (downloaded from the latest release) on the Android Emulator or device being used to run tests
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

* Set up the learning unit flow (temporary manual step)

The tests open a learning unit in your app using the flow file `gotolearningunit.yaml`
(in `app-cli/src/main/resources/flows`).

> **Note:** In a future release this file will be generated automatically by the RESPECT app code
> (Kotlin) based on the lesson you select. Until that is implemented, you need to edit it manually.

**Before running the tests, edit `gotolearningunit.yaml` so it navigates to the lesson you want to
test in your own app.** The version in the repository navigates through the **Leap Learning Universe**
app as an example.

Replace the `tapOn` steps with the taps needed to reach your lesson, starting from the RESPECT app
home screen.


* Run test using Maestro CLI :
```
cd app-cli/src/main/resources/flows

maestro test \
    -e SCHOOL_URL=http://192.168.1.2:8094/ \
    -e SCHOOL_ADMIN_PASSWORD=adminpassword \
    -e SCHOOL_NAME=TestSchool \
    -e TEST_APP_URL=https://demo.openeel.org/en-US/launchable-app-manifest.json \
    -e TEST_APP_NAME="Demo Launchable App" \
    -e TEST_APP_MODE=native|webview \
    -e OFFLINE_STATUS=true|false \
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