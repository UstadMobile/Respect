# RESPECT App Maestro tests

## Development environment setup:
* Complete development environment setup as per main [README](../README.md)
* Install [Maestro CLI](https://github.com/mobile-dev-inc/Maestro/releases).

## Run an individual test:

* Build the project as per the main [README](../README.md)
* Start respect-server and add a school as per the main project README.
* Install the APK on the Android Emulator or device being used to run tests
  e.g. run project using Android Studio, drag/drop file onto Android emulator, or install using adb command:
```
adb install ./respect-app-compose/build/outputs/apk/debug/respect-app-compose-debug.apk
```

* Run test using Maestro CLI (specify the school URL and admin password):
```
maestro test \
    -e SCHOOL_URL=http://192.168.1.2:8094/ \
    -e SCHOOL_ADMIN_PASSWORD=adminpassword \
    -e SCHOOL_NAME=TestSchool \
    .maestro/flows/flow_name.yaml
```

Where:
* ```SCHOOL_URL``` is the URL for the school as used with the addschool command as  as per the main
  [README](../README.md)
* ```SCHOOL_ADMIN_PASSWORD``` is the password for the admin user for the school (also as per addschool command)
* ```SCHOOL_NAME``` is the name of the school (also as per addschool command)


## Available test flows
---
### 001_001_invite_users_using_qr_code_or_link_test
- Admin generates invite link (QR/link) for teacher
- Teacher joins using QR/link → creates account
- Teacher creates class and generates invite code for student
- Student joins using invite code → waits for approval
- Teacher approves student → student joins class
- Teacher generates parent invite link to join class
- Parent joins using link → adds child to class
---
### 001_002_add_user_direct_test
- Admin adds new user directly (Parent/Teacher/Student)
- Validates mandatory fields and input constraints
- Creates parent with child (family member)
- Creates accounts for users
- Assigns and manages QR code badges
- Validates login, password change, and child mode access
- Verifies QR-based login for student
---
### 001_003_login_using_school_link_test
- User opens app via school link
- Validates empty and invalid link inputs
- Enters valid school URL
- Logs in using credentials
- Accesses app successfully
---
### 001_005_add_school_self_registration_test
- User adds a new school from login screen
- Selects host and registers school
- Creates system administrator account
- Logs into newly created school
- Verifies profile and logout/login flow
---
### 002_browse_lessons_test
- Admin logs in to the App
- Adds app using external manifest link
- Verifies app is added successfully
- Opens app and browses lessons
- Opens and validates a lesson content
---
### 003_admin_user_assigns_assignment_to_a_class_test
- Admin setup includes app, class and teacher creation
- Teacher logs in and accesses class
- Teacher creates a new assignment
- Assignment is linked with lesson content
- Assignment is saved and verified in class
---

## Testing using HTTPS


## Run multiple tests (suite)

Running multiple tests with Maestro requires a blank server installation for each test.
[TestServerController](https://github.com/UstadMobile/TestServerController) is used to start/stop a new blank server instance on a free port as
required.

```
export TESTSERVER_CONTROLLER=http://192.168.1.2:8094/
./ci-run-maestro.sh 
```

Where:
* 192.168.1.2 is the local IP of the developer's laptop
