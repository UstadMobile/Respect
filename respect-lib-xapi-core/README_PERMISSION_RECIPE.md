# xAPI granular permission recipe

Purpose: the general xAPI oAUTH scopes consider permission to update canonical activity and actor 
information as binary. Most systems require more granular permission e.g. where a teacher can
update an activity that represents their own class, but not others.

## Core

* The system admin (e.g. when a system is first setup) makes a protect statement to protect 
  specific activity id(s). Such statements are expected to be allowed only from the system admin.
* Actors (agents and groups) are considered protected by default.
* Once an activity id is protected, its canonical definition can only be read or written _if_ 
  there is an applicable permission grant statement.
* Someone with a given permission can grant it (or a more restricted version of it) to someone else
  e.g. a teacher can grant students read permission.
* OpenID role claims are mapped to an identified group.
* Grant can be to:
  * A specific actor (identified group or agent)
  * A role (eg. as specified by an openid claim)
* All grant and protect statements MUST have the profile id in the category activities

* E.g.

* The system admin protects ```https://school.example.org/classes/*```.
* The system admin grants write permission on activity ids https://school.example.org/classes/* to
  users with the role 'teacher'.

## Protect statements


## Grant statements


Rough scratch notes to be tidied up later:

* The identified group for a class could be in the form of 
  ```account: { name: "teachers", homePage: "https://school.example.org/classes/id }"```

* Use a save verb. 

* If a path is protected, then to create a new activity one must have write permission for the 
  directory path e.g. to create a new class, you need to have an applicable grant write permission 
  statement.

