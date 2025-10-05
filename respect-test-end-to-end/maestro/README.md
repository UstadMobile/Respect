# RESPECT App Maestro tests

## Development environment setup:
* Complete development environment setup as per main [README](../../README.md)
* Install [Maestro CLI](https://github.com/mobile-dev-inc/Maestro/releases).

## Run an individual test:

* Build the project as per the main [README](../../README.md)
* Start respect-server and add a school as per the main project README.
* Install the APK on the Android Emulator or device being used to run tests
e.g.
```
adb install ./respect-app-compose/build/outputs/apk/debug/respect-app-compose-debug.apk
```

* Run test using Maestro CLI (specify the school URL and admin password):
```
maestro test \
    -e SCHOOL_URL=http://192.168.1.2:8094/ \
    -e SCHOOL_ADMIN_PASSWORD=adminpassword \
    e2e-tests/flow_name.yaml
```

## Run multiple tests (suite)

Running multiple tests with Maestro requires a blank server installation for each test. 
[TestServerController](https://github.com/UstadMobile/TestServerController) is used to start/stop a 
new blank server instance on a free port as required. 


