# xAPI identified groups for classes

```
{
	"id":"fd41c918-b88b-4b20-a0a5-a4c32391aaa0",
	"timestamp": "2015-11-18T12:17:00+00:00",
	"actor":{
		"objectType": "Agent",
		"name":"Firstname Lastname",
		"account": {
		    name: "loggedinuser",
		    homePage: "https://schoolname.example.org/"
		}
	},
	"verb":{
		"id":"http://activitystrea.ms/schema/1.0/saved",
		"display":{ 
			"en-US":"Saved" 
		}
	},
	"object":{
		"objectType": "Group",
		"name": "Group Name",
		"account": {
		    name: "uuid",
		    homePage: "https://schoolname.example.org/"
		},
		member: [
		    {
                "objectType": "Agent",
                "name":"Member Name1",
                "account": {
                    "name": "member1",
                    "homePage": "https://schoolname.example.org/"
                }
            },
            {
                "objectType": "Agent",
                "name":"Member Name2",
                "account": {
                    "name": "member2",
                    "homePage": "https://schoolname.example.org/"
                }
            }
		]
	},
	"context": {
        "contextActivities": {
            "parent": [
                {
                    "id": "https://schoolname.example.org/class/class-uid",
                    "objectType": "Activity"
                }
            ]
        }
	}
}
```

To retrieve groups for a given class: Use the statement resource with the query parameters:

* verbid=http://activitystrea.ms/schema/1.0/saved
* activity_id=https://schoolname.example.org/class/class-uid
* related_activities=true

To retrieve the latest version of a specific group:
* verbid=http://activitystrea.ms/schema/1.0/saved
* actor=group identity (without member field set)

Scratch:

For class itself: team = students, instructor = teachers.

