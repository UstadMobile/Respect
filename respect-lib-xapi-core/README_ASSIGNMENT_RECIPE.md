# Assignment xAPI Recipe

Use this recipe when an assignment is being set by one actor (e.g. a teacher) for another actor
(e.g. a group of students). An assignment can contain one or more activities to be completed by
the assignee(s).

## Core

* Assignment statements include the Recipe ID ( https://id.ustadmobile.com/xapi/activities/assignment-recipe ) 
  in the 'category' context activity list (this applies to the assignment statements, it does not
  apply to statements about the assignees completion of assigned activities).
* Statements about the assignees completion of assigned activities MUST include the activity id of 
  the assignment in the contextActivities grouping property.
* The agent of the assigned statement MUST be assignee (can be an Agent or Group)
* Statements generate an Activity ID that is unique for a given an assignment. As normal, the 
  activity id should use a domain that the creator is authorized to use for this purpose.
* If an assignment is changed then a new assigned statement is issued with the updated assignment. 
  The previous assignment for the same activity id SHOULD be voided.

## Verb

Use the following verb:
* http://activitystrea.ms/schema/1.0/assign

## Context

* The context instructor should be the actor who set the assignment (e.g. the teacher)
* The activities assigned should be included in the __grouping__ property of contextActivities.
* If set, the deadline of the assignment should be set in the extension https://id.ustadmobile.com/xapi/extension/deadline
* The original creation date of the assignment should be set in the extension https://id.ustadmobile.com/xapi/extension/created

### Appendix A: Example assignment statement

```
{
  "id": "6690e6c9-3ef0-4ed3-8b37-7f3964730bee",
  "actor": {
    "name": "Grade 1",
    "mbox": "mailto:grade1@example.com",
    "member": [
      {
        "name": "Andrew Downes",
        "account": {
          "homePage": "http://www.example.com",
          "name": "13936749"
        },
        "objectType": "Agent"
      },
      {
        "name": "Toby Nichols",
        "openid": "http://toby.openid.example.org/",
        "objectType": "Agent"
      },
      {
        "name": "Ena Hills",
        "mbox_sha1sum": "ebd31e95054c018b10727ccffd2ef2ec3a016ee9",
        "objectType": "Agent"
      }
    ],
    "objectType": "Group"
  },
  "verb": {
    "id": "http://activitystrea.ms/schema/1.0/assign"
  },
  "version": "1.0.0",
  "object": {
    "id": "https://school.example.org/xapi/ns/assignment-uuid",
    "definition": {
      "extensions": {
        "https://id.ustadmobile.com/xapi/extension/deadline": "2013-05-18T05:32:34.804+00:00",
        "https://id.ustadmobile.com/xapi/extension/created": "2013-05-20T05:32:34.804+00:00" 
      },
      "name": {
        "en-US": "Example assignment"
      },
      "description": {
        "en-US": "Students should complete a/b/c",
      },
      "type": "http://id.tincanapi.com/activitytype/school-assignment",
    },
    "objectType": "Activity"
  },
  "context": {
    "instructor": {
      "account" : {
        "name": "teacher",
        "homePage": "https://school.example.org/"
      }
    },
    "contextActivities": {
      "category": [
         {
           "id": "https://id.ustadmobile.com/xapi/activities/assignment-recipe",
           "objectType": "Activity"
         }
      ]
      "grouping": [
        {
          "id": "https://app.provider.com/activities/math/algebra1",
          "objectType": "Activity"
        },
        {
          "id": "https://app.anotherprovider.com/activities/physics/gravity",
          "objectType": "Activity"
        }
      ]
    }
  }
}
```


