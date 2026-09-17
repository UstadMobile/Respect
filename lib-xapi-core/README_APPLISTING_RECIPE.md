# App listings
Recipe id: https://id.openeel.org/recipes/applisting

Used to provide a list of launchable apps that are intended to be made available to users (e.g. 
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
        "id": "https://id.openeel.org/verb/listed-app"
    },
    object: {
        "id": "https://example.app/ns/app-id",
        "definition": {
            "name":{ 
                "en-US" : "App name" 
            },
            "description":{ 
                "en-US" : "App description" 
            },
            "type": "http://activitystrea.ms/schema/1.0/application",
            "moreInfo": "https://example.app/about",
            "extensions": {
                "https://id.openeel.org/extensions/activity/webpub-manifest-link": "https://example.app/app-publication-opds.json"
            }
        }
    },
    context: {
        "contextActivities": {
          "category": [
             {
               "id": "https://id.openeel.org/recipes/applisting"
             }
          ]
        }
    }
}
```

* The [webpub-manifest extension](README_EXTENSION_PUBLICATION.md) links to a
[launchable app OPDS publication](../respect-lib-opds-model/README_LAUNCHABLE_APP.md).
* An app is removed by [voiding](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#232-voiding) 
  the statement that added it.
* A listing of available apps can be retrieved using a get statements query with the following 
  parameters:
  * verb=```https://id.openeel.org/verb/listed-app```
  * activity=```https://id.openeel.org/recipes/applisting```
  * related_activities=```true```
