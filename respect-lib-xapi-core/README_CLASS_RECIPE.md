# Classes

Recipe id: https://id.openeel.org/xapi/recipes/class-management

Used to provide a list of classes consisting of teachers and students. Depending on the permission
configuration this may be managed directly by the launcher application or could be managed upstream
(e.g. by a roster server, where changes on the Roster server trigger LRS statements).

Each class is represented by a unique activity id. The statement saving the class references two
identified groups: the students (context team) and the teachers (context instructor).

Example save class statement:
```
{
    actor: {
        "account" : {
            "name": "admin",
            "homePage": "https://schoolname.example.org/"
        }
    },
    verb: {
        "id": "http://activitystrea.ms/schema/1.0/saved"
    },
    object: {
        "id": "https://school.example.edu/classes/uuid",
        "definition": {
            "name":{ 
                "en-US" : "Class name" 
            },
            "description":{ 
                "en-US" : "Class description" 
            },
            "type": "http://id.openeel.org/xapi/activity-type/class"
        }
    },
    context: {
        "team": {
            "objectType": "Group",
            "name": "Class name students",
            "account": {
                name: "students",
                homePage: "https://school.example.edu/classes/uuid"
            },
        },
        "instructor": {
            "objectType": "Group",
            "name": "Class name teachers",
            "account": {
                name: "teachers",
                homePage: "https://school.example.edu/classes/uuid"
            },
        },
        "contextActivities": {
          "category": [
             {
               "id": "https://id.openeel.org/xapi/recipes/class-management"
             }
          ]
        }
    }
}
```
* The statement saving the class references two identified groups: a teachers group and a students 
  group
* The list of classes can be retrieved using the following get statement query paraemters:
  * ```activity```=https://id.openeel.org/xapi/recipes/class-management
  * ```related_activities```=true

Example save identified group statement:
```
{
	"actor":{
		"objectType": "Agent",
		"name":"Firstname Lastname",
		"account": {
		    name: "loggedinuser",
		    homePage: "https://schoolname.example.org/"
		}
	},
	"verb":{
		"id":"http://activitystrea.ms/schema/1.0/saved"
	},
	"object":{
		"objectType": "Group",
        "name": "Class name students",
        "account": {
            name: "students",
            homePage: "https://school.example.edu/classes/uuid"
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
                    "id": "https://school.example.edu/classes/uuid"
                }
            ],
            "category": [
                {
                   "id": "https://id.openeel.org/xapi/recipes/class-management"
                }
            ]
        }
	}
}
```


