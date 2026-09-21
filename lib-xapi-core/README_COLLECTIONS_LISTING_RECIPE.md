# Collections listings
Recipe id: https://id.openeel.org/recipes/applisting

Used to keep a list of OPDS collections that are intended to be made available to users (e.g.
teachers, students, etc). 

```
{
    actor: {
        "account" : {
            "name": "admin",
            "homePage": "https://school.example.org/"
        }
    },
    verb: {
        "id": "https://id.openeel.org/verb/pin-collection"
    },
    object: {
        "id": "https://example.app/ns/app-id",
        "definition": {
            "name":{ 
                "en-US" : "Collection name" 
            },
            "description":{ 
                "en-US" : "Collection subtitle" 
            },
            "type": "http://activitystrea.ms/schema/1.0/application",
            "extensions": {
                "https://id.openeel.org/extensions/activity/opds-collection-link": "https://school.example.org/xapi/activities/profile?activityId=[https://school.example.org/collections/uuid]&profileId=[https://id.openeel.org/profile/activity/opds-collection]"
            }
        }
    },
    context: {
        "contextActivities": {
          "category": [
             {
               "id": "https://id.openeel.org/recipes/collection-listing"
             }
          ]
        }
    }
}
```

* The opds-collection-link links to an OPDS collection.
* A collection is unpinned by [voiding](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#232-voiding)
  the statement that added it.
* A listing of pinned collections can be retrieved using a get statements query with the following
  parameters:
    * verb=`https://id.openeel.org/verb/pin-collection`
    * activity=`https://id.openeel.org/recipes/collection-listing`
    * related_activities=`true`