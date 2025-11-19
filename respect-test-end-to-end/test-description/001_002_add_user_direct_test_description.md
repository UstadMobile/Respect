#  Admin create a person and validate user login and credentials

## Description:

Admin create a person, add details ,add family members and create account. The user then verifies the credentials by logging into the application.

## Step-by-step procedure:

### A) Admin add new Person

1. Launch the application.
2. Click **Get started** button on On-boarding screen.
3. On "Let's get started" screen, Tap on the **School name** field.
4. Enter the **School name**.
5. Select on School name that will appear below the School name field.
6. Verify the user is in **Login** screen.
7. Tap on the **Username** field and enter the admin username.
8. Tap on the **Password** field and enter the admin password.
9. Tap on the **Login** button.
10. Verify the user is in the **Apps** screen.
11. Click on the "People" tab.
12. Click on "+ Person" button.
13. On "Add person" screen, click on Save button.
14. Verify that user getting error saying First name, Last name and Gender fields are required fields.
15. Click on "First name" field and enter the user's First name as ParentA.
16. Click on "Last name" field and enter the user's Last name as User. 
17. Click on "Gender" and select user's gender.
18. Click on "role" dropdown selector and choose "Parent" option.
19. Click on the Save button.
20. Verify the ParentA user is saved.
21. Click on Edit button
22. Edit person screen appears.
23. Click on "+ Family member" button.
24. Click on "Add person" button.
25. Click on "First name" field and enter the user's First name as Child.
26. Click on "Last name" field and enter the user's Last name as User.
27. Click on "Gender" and select user's gender.
28. Click on the Save button.
29. Verify child user is added as family member to the ParentA user
30. Click on Child user
31. Verify user able to see ParentA user as Family member and the role of Child user is set as "Student" by default.
32. Click on the "People" tab.
33. Click on "+ Person" button.
34. Click on "First name" field and enter the user's First name as Test.
35. Click on "Last name" field and enter the user's Last name as User.
36. Click on "Gender" and select user's gender.
37. Click on "role" dropdown selector and choose "Teacher" option.
38. Verify user not able to see "Family members" field on the Add person screen.
39. Click on "role" dropdown selector and choose "Student" option.
40. Verify user able to see "Family members" field on the Add person screen.
41. Click on "+ Family member" button.
42. Verify user is on "Select person" screen.
43. Click on "ParentA user".
44. Click on the Save button.
45. Verify ParentA user is added as family member to the Test user
46. Click on Edit button.
47. Click on "Date of birth" button.
48. Enter today's date and click on save button.
49. Verify user is getting error saying- Date of birth can't be a future date.
50. Click on "Date of birth" field and enter user's date of birth.
51. Click on "Country code" button and choose "+1" as country code.
52. Click on Phone number field and enter 2 digit phone number and click on "Save" button.
53. Verify user getting error saying "Invalid".
54. Click on Phone number field and enter 11 digit phone number and click on "Save" button.
55. Verify user getting error saying "Invalid".
56. Click on Phone number field and enter 10 digit phone number.
57. Click on email field and enter "test@gmail" and click on "Save" button
58. Verify user getting error saying "Enter valid email address."
59. User enter valid email id.
60. Click on the Save button
61. Verify person details are saved.
62. Verify ParentA user is visible as family member to the Test user.
63. Click on "Create account" button.
64. Click on "Save" button.
65. Verify user is getting error saying Username and Password are mandatory fields.
66. Click on Username and enter a name starting with a letter.
67. Verify user is getting error, saying Username cannot start with a number.
68. Click on Username and enter a name with 2 characters like - "AB".
69. Verify user is getting error saying "User name should be at least 3 characters"
70. Click on Username and enter a valid username.
71. Click on Password and enter a 2 digit password.
72. Verify user is getting error saying "User password should be at least 6 characters"
73. Click on Password and enter a 7 digit/letters password.
74. Click on "SAVE" button.
75. Verify the user able to see username in person detail screen.
76. 

### B) New person login to the app with credentials shared by admin

1. Launch the application.
2. Click **Get started** button on On-boarding screen.
3. On "Let's get started" screen, Tap on the **School name** field.
4. Enter the **School name**.
5. Select on School name that will appear below the School name field.
6. Verify the user is in **Login** screen.
7. Tap on the **Username** field and enter the invalid username.
8. Tap on the **Password** field and enter the valid password.
9. Tap on the **Login** button.
10. Verify the user is getting error saying - Invalid Username/Password, try again.
11. Tap on the **Username** field and enter the valid username.
12. Tap on the **Password** field and enter the invalid password.
13. Tap on the **Login** button.
14. Verify the user is getting error saying - Invalid Username/Password, try again.
15. Tap on the **Username** field and enter the valid username.
16. Tap on the **Password** field and enter the valid password.
17. Tap on the **Login** button.
18. Verify the user is in the **Apps** screen.
19. Click on the "People" tab.
20. Click on user's name 
21. Verify user is on Person details screen.
22. Click on "Manage account" button.
23. Verify user able to see username and password change button is visible.
24. Click on "Change" button next to the password button.
25. Click on "Old password" and enter old password.
26. Click on "New password" and enter new password.
27. Click on "Save" button.
28. Verify new password got saved.

### C) New person login to the app with new password

1. Launch the application.
2. Click **Get started** button on On-boarding screen.
3. On "Let's get started" screen, Tap on the **School name** field.
4. Enter the **School name**.
5. Select on School name that will appear below the School name field.
6. Verify the user is in **Login** screen.
7. Tap on the **Username** field and enter the valid username.
8. Tap on the **Password** field and enter the updated new password.
9. Tap on the **Login** button.
10. Verify the user is in the **Apps** screen.
