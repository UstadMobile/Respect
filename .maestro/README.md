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
### 001_001a_invite_flow_admin_teacher_approval_on_off_test

1. Admin Generates New Person Invites:
2. Admin generates invitation codes for:
   • System Administrator role
   • Teacher role
   Both Approval Required ON and Approval Required OFF flows are tested.
3. Admin Creates Class & Teacher Class Invite:
4. Admin creates "TestClass" and generates Teacher class invitation codes for:
   • Approval Required ON
   • Approval Required OFF
5. Admin & Teacher Onboarding (Approval Required ON):
   • AdminA joins using System Administrator invite and enters "Waiting for approval" state.
   • TeacherA joins using Teacher QR/link invite and enters "Waiting for approval" state.
   • TeacherC joins "TestClass" using Teacher class invite and enters "Waiting for approval" state.
6. Admin Approval Flow:
7. Admin logs into the People section and validates pending join requests for:
   • AdminA User
   • TeacherA User
   • TeacherC User
8. Admin approves all pending requests.
9. Admin & Teacher Onboarding (Approval Required OFF):
   • AdminB joins using System Administrator invite and gains immediate access.
   • TeacherB joins using Teacher QR/link invite and gains immediate access.
   • TeacherD joins "TestClass" using Teacher class invite and gains immediate access.
10. Immediate Access Verification:
    AdminB and TeacherB successfully reach the Apps dashboard without requiring approval.
    Final Verification:
11. Admin logs in and validates:
   • People section contains AdminA, AdminB, TeacherB, TeacherC, and TeacherD users.
   • No pending requests remain.
   • "TestClass" contains TeacherC User and TeacherD User.
---
### 001_001b_invite_flow_student_parent_approval_enabled_test

1. Admin Creates Class & Teacher Account:
   Admin creates "TestClass" and manually adds TeacherA User as a Teacher account with login credentials.
2. Admin Generates New Person Invites (Approval Required ON):
   Admin generates invitation codes for:
   • Student role (New Person invite)
   • Parent role (New Person invite)
   Both invites are configured with "Approval Required" ON.
3. Admin Generates Class Invites (Approval Required ON):
   Inside "TestClass", Admin generates:
   • Direct Student class invite
   • Parent-to-Class invite
   Both class invites are configured with "Approval Required" ON.
4. Student & Parent New Person Onboarding:
   • StudentA joins the school using the Student New Person invite and enters "Waiting for approval" state.
   • ParentA joins the school using the Parent New Person invite and enters "Waiting for approval" state.
5. Student Joins Class Directly:
   • StudentC joins "TestClass" using the direct Student class invite and enters "Waiting for approval" state.
6. Parent Joins Class with Child:
   • ParentC joins using the Parent class invite and registers ChildA User during onboarding.
   • ParentC and ChildA User both enter "Waiting for approval" state.
7. Teacher Approval Flow:
   TeacherA logs in and validates that all Approval Required ON users appear as pending requests in the People section.
   TeacherA approves all pending requests from the People section.
8. Final Verification:
   TeacherA opens "TestClass" and validates:
   • Student User is visible in the class.
   • Child User is visible in the class.
   • No pending approval requests remain.
---
### 001_001c_invite_flow_student_parent_approval_disabled_test

1. Admin Creates Class & Teacher Account:
   Admin creates "TestClass" and manually adds TeacherA User as a Teacher account with login credentials.
2. Admin Generates New Person Invites (Approval Required OFF):
   Admin generates invitation codes for:
   • Student role (New Person invite)
   • Parent role (New Person invite)
   Both invites are configured with "Approval Required" OFF.
3. Admin Generates Class Invites (Approval Required OFF):
   Inside "TestClass", Admin generates:
   • Direct Student class invite
   • Parent-to-Class invite
   Both class invites are configured with "Approval Required" OFF.
4. Student & Parent New Person Onboarding:
   • StudentA joins the school using the Student New Person invite and gains immediate access.
   • ParentA joins the school using the Parent New Person invite and gains immediate access.
5. Student Joins Class Directly:
   • StudentB joins "TestClass" using the direct Student class invite and gains immediate access to the class.
6. Parent Joins Class with Child:
   • ParentB joins using the Parent class invite, registers ChildA User during onboarding, and gains immediate access.
   • ChildA User is automatically added to "TestClass".
7. Teacher Verification:
   TeacherA logs in and validates:
   • All onboarded users are visible in the People section.
   • No pending approval requests are displayed.
   • "TestClass" contains StudentB User and ChildA User.
   • ParentB User is not listed inside the class roster.
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

### 003_admin_user_assigns_assignment_to_a_class_test

1. Admin Adds Learning App:
   Admin logs into the school and adds a learning application to the school environment.
2. Admin Creates Teacher Account:
   Admin creates TeacherA User with Teacher role credentials and enables account access.
3. Admin Creates Class:
   Admin creates a class named "TestClass".
4. Teacher Login & Navigation:
   TeacherA logs into the school and navigates to the Assignments section.
5. Assignment Creation Validation:
   Teacher attempts to save a new assignment without entering required details and validates the required field error message.
6. Teacher Creates Assignment:
   Teacher creates an assignment named "Homework 1" with:
   • Assigned class: TestClass
   • Future due date and time
   • Linked lesson content from the learning app
7. Assignment Verification:
   Teacher successfully saves the assignment and verifies:
   • Assignment detail page displays "Homework 1"
   • Linked lesson "Lesson 001" is visible
   • Assignment appears in the Assignments list.

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
