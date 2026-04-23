# XAPI

The [xAPI](https://www.xapi.com/) is a key widely adopted open standard to track learning experiences.

## General notes:

* [Activity IDs](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#activity-id-requirements) 
  that match the URL of the LRS itself will be controlled by permission models on the LRS. As per the 
  xAPI spec ```An Activity id SHOULD use a domain that the creator is authorized to use for this purpose.```.
* Canonical updates to actors which are identified by an [Account object](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2423-inverse-functional-identifier) 
  will only be performed according to the permission models on the LRS.
* When making a statement the authorized session must have permission to write learning records for
  the given actor e.g. it is for the actor themselves or someone authorized to write records on their
  behalf as per the permission model e.g. their teacher.

Verbs and extensions are sourced from the [xAPI registry](https://registry.tincanapi.com/) wherever
possible.

## Example statements

Largely derived from [xAPI base standard examples](https://opensource.ieee.org/xapi/xapi-base-standard-examples/-/blob/main/9274.1.1%20xAPI%20Base%20Standard%20Examples.md)

### Example completed activity

```
{
	"id":"7ccd3322-e1a5-411a-a67d-6a735c76f119",
	"timestamp": "2015-12-18T12:17:00+00:00",
	"actor":{
        "objectType": "Agent",
		"name":"Example Learner",
		"mbox":"mailto:example.learner@adlnet.gov"
	},
	"verb":{
		"id":"http://adlnet.gov/expapi/verbs/attempted",
		"display":{
			"en-US":"attempted"
		}
	},
	"object":{
		"id":"https://example.app/lesson/identifier",
		"definition":{
			"name":{
				"en-US":"Assigned course"
			},
			"description":{
				"en-US":"A fictitious example CBT course."
			}
		}
	},
	"result":{
		"score":{
			"scaled":0.95
		},
		"success":true,
		"completion":true,
		"duration": "PT1234S"
	}
}
```
Statement must include result.completion = true. If a learning unit is used as part of an assignment
by RESPECT, then the RESPECT app will automatically add the Activity Id of the assignment if not 
already present to the contextActivities e.g.:

```
{
	"id":"7ccd3322-e1a5-411a-a67d-6a735c76f119",
	"timestamp": "2015-12-18T12:17:00+00:00",
	"actor":{
        "objectType": "Agent",
		"name":"Example Learner",
		"mbox":"mailto:example.learner@adlnet.gov"
	},
	"verb":{
		"id":"http://adlnet.gov/expapi/verbs/attempted",
		"display":{
			"en-US":"attempted"
		}
	},
	"object":{
		"id":"https://example.app/lesson/identifier",
		"definition":{
			"name":{
				"en-US":"Assigned course"
			},
			"description":{
				"en-US":"A fictitious example CBT course."
			}
		}
	},
	"result":{
		"score":{
			"scaled":0.95
		},
		"success":true,
		"completion":true,
		"duration": "PT1234S",
		"contextActivities": {
		    "grouping" : [
		        {
		            "id": "https://schoolname.example.org/ns/assignment/uuid",
		            "objectType": "Activity"
		        }
		    ]
		}
	}
}
```

### Create a group

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
		"id":"http://activitystrea.ms/schema/1.0/create",
		"display":{ 
			"en-US":"Created" 
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
                    name: "member1",
                    homePage: "https://schoolname.example.org/"
                }
            },
            {
                "objectType": "Agent",
                "name":"Member Name2",
                "account": {
                    name: "member2",
                    homePage: "https://schoolname.example.org/"
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

If an existing group is updated: simply change the verb from created to [updated](https://registry.tincanapi.com/#uri/verb/160).
