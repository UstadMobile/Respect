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
### 001_001_invite_users_test

1. Admin Generates New Person Invites: 
    Admin creates invitation codes for "System Administrator" and "Teacher" roles, testing both "Approval Required" (ON) and "Approval Required" (OFF) states.
2. Admin and Teacher Onboarding (New Person):
  • AdminA (Approve ON) and TeacherA (Approve ON) join via QR/Link and must wait for approval.
  • AdminB (Approve OFF) and TeacherB (Approve OFF) join via QR/Link and gain immediate access.
3. Class Creation & Teacher Invites: Admin creates "TestClass" and generates specific invitation codes for Teachers to join that class. 
  • TeacherC (Approve ON) waits for approval, while TeacherD (Approve OFF) joins immediately.
4. Admin Approval Logic:
  • Admin logs in to the "People" and "Classes" sections to approve the pending requests for AdminA, TeacherA, and TeacherC.
5. Teacher Generates New Person & Class Invites:
  • TeacherC generates New Person invites for Students/Parents and class-specific invites for "TestClass" (testing both Approval ON/OFF states).
6. Student & Parent Onboarding:
  • StudentA/ParentA (New Person - Approve ON) wait for approval.
  • StudentB/ParentB (New Person - Approve OFF) join immediately.
  • StudentC (Class - Approve ON) waits for approval.
  • StudentD (Class - Approve OFF) joins immediately.
7. Parent Joins with Child:
  • ParentC (Class - Approve ON) joins and registers ChildA, then waits for approval.
  • ParentD (Class - Approve OFF) joins and registers ChildB, gaining immediate access.
8. Teacher Approval & Verification: TeacherC approves all pending New Person and class requests. 
9. ParentD logs in to verify that all approved students and children are visible within "TestClass".

---
### 001_002_add_user_direct_test

1. Admin logs into the app
2. Admin adds a new user directly (Parent user)
3. Verify "Family member" field is not visible when role is changed to Teacher
4. Validate "Edit" button functionality
5. Validate mandatory fields and input constraints
6. Add child user via Family member → Add person screen
7. Create account for Parent user (username, password)
8. Create account for Child user (username, assign/manage QR code badge, set password)
9. Create a student user for QR code validation
10. Validate login, password change, and child mode access
11. Verify QR-based login for student
---
### 001_003_login_using_school_link_test

1. User opens app via school link
2. Validate empty and invalid link scenarios
3. Enter valid school URL
4. Perform login with credentials
5. Verify successful access to the app
---
### 001_005_add_school_self_registration_test

1. User adds a new school from login screen
2. Selects host and registers school
3. Creates system administrator account
4. Logs into newly created school
5. Verifies profile and logout/login flow
---
### 002_browse_lessons_test

1. Admin logs in to the App
2. Adds app using external manifest link
3. Verifies app is added successfully
4. Opens app and browses lessons
5. Opens and validates a lesson content
---
### 003_admin_user_assigns_assignment_to_a_class_test

1. Admin setup includes app, class and teacher creation
2. Teacher logs in and accesses class
3. Teacher creates a new assignment
4. Assignment is linked with lesson content
5. Assignment is saved and verified in class
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
