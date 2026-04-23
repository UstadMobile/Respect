# XAPI

The [xAPI](https://www.xapi.com/) is a key widely adopted open standard to track learning experiences.

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
		        "id": "https://schoolname.example.org/ns/assignment/uuid"
		    ]
		}
	}
}
```


