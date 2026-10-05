# Handling permissions on xAPI

The Experience API spec defines [oAUTH scopes](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#42-oauth-10-authorization-scope)
by endpoint (e.g. statements/write, read, etc). The launcher app uses Experience API to store 
assignments, school pinned apps/collections, etc. More granular control over who can make what
statement is needed.

The launcher app will follow a JSON (specified in .well-known/xapi-write-permission.json):

```json
{
  "assignment-write": {
    "type" : "require-role",
    "role": "teacher"
  },
  "pinned-apps-write": {
    "type" : "require-role",
    "role": "admin"
  }
}
```
Note: this could be made more flexible by using SQL LRS reaction JSON. If something is just not
allowed, then the reaction could result in a void statement. If something requires a particular role
then the reaction can result in a requires-role statement.

The launcher app will abide by the JSON (as per the role specified by the oAUTH token or builtin
auth).

Read permission is expected to include any statement where the actor is related to the statement
(e.g. as per the [statements resource](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements) 
behavior when the agent parameter is set to the logged in actor and related_agents=true). This covers
most use cases:
* Assignments: students can see because they are assigned to a group for their class.
* Launchable app statements: students can read/write their own statements, but not others.
* Bookmarks: students can read/write their own statements, but not others.

Some cases need an allow list such that statements can be viewed by anyone e.g.
* List apps and collections pinned for the whole school

**Enforcing permissions**

Permissions are enforced 'natively' in the launcher app itself and when using builtin server 
app-server. Permissions can be enforced on other platforms:

* **SQL LRS**: Use the reactions JSON to write a void statement when a statement is not allowed. 
  Read permissions can be enforced using a simple proxy (that sets the agent parameter in requests)
  where needed.
* **Google Drive**: The configuration needs to include which folder a given statement would be saved
  in. 

**Permissions for launchable apps**

* An extra filter: based on the domain of the app. Access is subject to the same rules as browser 
  local storage e.g. an app that launched from the URL app.example.org can only access local storage 
  for that domain and its subdomains.
