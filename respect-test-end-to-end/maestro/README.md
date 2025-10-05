# RESPECT App Maestro tests

## Run an individual test:

* Install Maestro CLI
* Run any test using Maestro CLI:
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


