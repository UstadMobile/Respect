# Admin enables shared school device mode, manages devices, and student login via QR badge

## Description:
This test covers the end-to-end workflow of setting up "Shared School Device" mode. It involves an admin creating classes and users, configuring the device into shared mode (kiosk), a teacher unlocking the device to approve it, and finally, adding a second device via a link where a student logs in using a QR code badge.

## Step-by-step procedure:

### A) Admin creates class and users

1. Run the school admin login flow.
2. Run the subflow to add a new class named "New Class".
3. Run the subflow to add a student ("StudentA User") to "New Class" and generate their QR badge link.
4. Run the subflow to add a teacher ("TeacherA User") to "New Class".

### B) Admin configures shared device PIN and enables mode locally

1. Navigate to Apps > Settings > School > Shared school device.
2. Click on Set PIN.
3. Attempt to save an invalid PIN (less than 4 digits or alphanumeric) to verify error handling.
4. Enter a valid 4-digit PIN ("1234") and click Save.
5. Click Add device and select This device.
6. Enter "Test Device 1" as the device name and click Enable.
7. Verify the app enters shared mode (displays "Select class" and "Scan QR code badge").

### C) Teacher unlocks device and approves pending request

1. Click Teacher/admin login.
2. Enter the device PIN (verify validation for incorrect PIN first, then enter "1234").
3. Proceed to the standard login screen.
4. Login using the "TeacherA" credentials created in step A.
5. Navigate to Apps > Settings > School > Shared school device.
6. Toggle the "Student can self-select their class and name" switch.
7. Observe the "Pending device request" for "Test Device 1".
8. Click Approve to authorize the device.


### D) Adding a second device via invite link and Student QR login

1. Click Add device and select Another device.
2. Toggle Approval required to OFF.
3. Copy the invite link.
4. Open the invite link (simulating a new device flow).
5. Click Get Started.
6. Enter "Test Device 2" as the device name and click Enable.
7. Click Scan QR code badge.
8. Select More Options > Paste URL.
9. Paste the Student QR badge link (generated in section A) and click OK.
10. Verify the student is successfully logged in and directed to the Apps screen.