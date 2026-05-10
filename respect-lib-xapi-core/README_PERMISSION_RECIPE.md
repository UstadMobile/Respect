# xAPI Statement Storage Folders Recipe

**Recipe ID**: ```https://openeel.org/xapi-ns/recipes/statement-folders```

DRAFT

Purpose: the general xAPI oAUTH scopes consider permission to update canonical activity and actor 
information as binary. There are many cases where granular permissions need to be understood 
e.g. where a teacher can update an activity that represents their own class, but not others.

## Statement folders

A "statement folder" is a URI similar to a shared drive/folder (e.g. Google Drive). It is up to any
given LRS as to how this is implemented (can be done through SQL queries, NextCloud client, 
Google Drive client, etc).

By default, each statement is stored in a folder for the authority of the statement (e.g. 
actor_id/statements/) which is owned by the given authority. The authority MUST have read-write 
access to this folder.

Others _MAY_ be granted read or write access to this folder (e.g. the parent should have read access
to all of their child's records, a teacher may have write access to all of their students records).
This can be done using a grant statement (where the authority grants a permission to the actor) e.g.

Grant permission to a folder:
```
{
  "actor": {
    "account" : {
      "name": "student",
      "homePage": "http://school.example.org/"
    }
  },
  "authority": {
    "account" : {
      "name": "student",
      "homePage": "http://school.example.org/"
    }
  },
  "verb": {
    "id": "http://id.openeel.org/verb/grant-read-permission"
  },
  "object": {
    "objectType": "Agent",
    "account" : {
      "name": "teacher",
      "homePage": "http://school.example.org/"
    }
  },
  "context": {
    "contextActivities": {
       "parent": [
          {
            "id": "statement-storage://agents/student@school.example.org/"
          }
       ],
       "category": [
          {
            "id": "https://openeel.org/xapi-ns/recipes/granular-permission-management"
          }
       ]
    }
  }
}
```
A grant statement MUST only be made by an authority with write permission for the given URI (as per
contextActivities.parent.id). The LRS (or proxy) implementing this recipe MUST reject any grant 
statement by any actor that does not have write permission for the given URI. The Uri scheme is 
arbitrary and can be determined by the LRS or proxy. 

If a folder does not yet exist, it should be created, if the authority making the grant statement
has write permission to create it. If the authority does not have that permission, the statement
must be rejected with a forbidden error.

Certain statements may need to be shared selectively with certain groups e.g. when a teacher creates
a class the statement should probably be shared with those in the class, but not others.

The LRS can use a rule set that requires statements matching certain parameters to be stored in a
particular URI : e.g. where a verb is saved and an activity id starts with 
```https://example.school.org/classes/.*``` then it must be saved in the statement folder 
```"statement-storage://classes/:1"```.

Potential flows:

**User gets created by admin**.

* Admin grants permission to folder (folder created) e.g. agents/students/id
* Admin saves new user (user management profile) to folder e.g. writes agents/students/id/statements/uuid.json
* Anyone with an applicable grant (e.g. to agents/students) can see it.

**Add a teacher, class, etc.**

* System initializes where admin grants write permission to teacher role
* Admin grants write permission to folder e.g. agents/teachers/id
* Admin saves new user (user management profile) to folder e.g. writes agents/teachers/id/statements/uuid.json
* Admin grants teacher role to new teacher.
* Teacher saves class. Rules set (e.g. because verb is saved and activity id starts with classes/) 
  that class should be saved to classes/class-uid. Teacher role has a grant on  folder classes so 
  can write.

When student added to class:
* Teacher grants read permission to student (agent) on the class statement folder.
* Teacher updates the group. The group save statement is saved in classes/class-uid.

Now student can read, but not write, statements related to the class.

