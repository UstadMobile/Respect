# Add user directly and manage accounts

## Summary
- Admin adds new user directly (Parent/Teacher/Student)
- Validates mandatory fields and input constraints
- Creates parent with child (family member)
- Creates accounts for users
- Assigns and manages QR code badges
- Validates login, password change, and child mode access
- Verifies QR-based login for student

---

## Flow A: Admin adds new user
- Admin logs in
- Navigates to **People**
- Clicks **Add new person**
- Validates required fields (First name, Last name, Gender)
- Selects role (Teacher / Parent / Student)
- Saves user

---

## Flow B: Validate user details
- Validates **Date of Birth** (no future date)
- Validates **Phone number** (invalid formats rejected)
- Validates **Email format**
- Saves valid details successfully

---

## Flow C: Add family member (child)
- Opens parent profile → Edit
- Adds **Family member**
- Enters child details
- Saves child under parent
- Verifies relationship (Parent ↔ Student)

---

## Flow D: Create accounts
- Creates account for parent
- Validates password rules
- Creates account for child
- Verifies username generation

---

## Flow E: Assign and manage QR badge
- Assigns QR badge to child
- Validates:
    - Duplicate QR not allowed
    - Unique QR assignment works
- Verifies:
    - Replace badge option
    - Revoke badge option
    - Reassign badge

---

## Flow F: Create and validate student user
- Admin creates student user
- Creates account with password
- Assigns QR badge
- Validates duplicate badge restriction
- Verifies badge management actions

---

## Flow G: Parent login and account management
- Parent logs in
- Navigates to **Manage account**
- Changes password (validates rules)
- Views profile and family members

---

## Flow H: Child (student) mode validation
- Parent switches to child profile
- Verifies limited access:
    - Can see **Assignments, Apps**
    - Cannot see **Classes, People**

---

## Flow I: QR login for student
- Launch app → Scan QR code
- Pastes QR URL
- Logs in successfully
- Verifies access to Apps, Assignments, Classes, People  