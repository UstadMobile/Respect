# xAPI SQL Query Recipe

Recipe ID: https://id.openeel.org/xapi/recipes/xapi-sql-query

[xAPI HTTP endpoints](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md) provide 
access to specific statements. Some users (e.g. managers) often need 
aggregate rather than granular data (eg the total number of active users not individual names). 
Sending the entire set of statements over a network may result in poor performance and excessive 
bandwidth usage. There are also scenarios where publishing aggregate data may be useful for 
accountability purposes however granular data needs protected to maintain the privacy of individual
users.

This recipe operates as follows:

* **Query request** a statement requesting a specific SQL query to run (see example queries). This is
  normally made by a user (eg a manager).
* **Query response** a statement containing the results of the SQL query that was just run. This is 
  normally made by the server.

Running the query and generating a query response relies on the statement data being available using
a given schema, e.g. the [SQL LRS](https://github.com/yetanalytics/lrsql) schema. This can be done
by using SQL LRS itself, using another LRS that uses the same database schema, or  

Query results can be used to draw charts etc. This does have some parallels to the [ADL xAPI Dashboard](https://github.com/adlnet/xAPI-Dashboard).
The xAPI Dashboard is no longer maintained. It also required access to each statement that was going
to be used in a report (in memory via JavaScript), whereas this recipe makes it possible to use SQL 
to aggregate data more efficiently.

## Verbs

https://id.openeel.org/xapi/verb/query-request

https://id.openeel.org/xapi/verb/query-response

## Permission management and security

Like other elements of xAPI, the implementation of security rules is at the discretion of the 
LRS/implementer. Potential appraoches include:

* Only allowing admin users to request a query (similar to how must business intelligence tools are
  used).
* Using a CTE or SQL View to limit the statements that can be accessed by the query according to user
  permissions.

### Appendix A: Example query request statement

```json
{
  "id": "6690e6c9-3ef0-4ed3-8b37-7f3964730bee",
  "actor": {
    "name": "uid",
    "mbox": "mailto:uid@example.com",
    "objectType": "Agent"
  },
  "verb": {
    "id": "https://id.openeel.org/xapi/verb/query-request"
  },
  "version": "1.0.0",
  "object": {
    "id": "https://school.example.org/xapi/ns/report-uuid",
    "definition": {
      "name": {
        "en-US": "Report title"
      },
      "description": {
        "en-US": "Total number of active users per day"
      },
      "type": "https://id.openeel.org/xapi/activity-type/query-request",
      "extensions": {
        "https://id.openeel.org/xapi/extension/query": "SELECT MAX(jsonb(xapi_statement.payload, '$.result.score')))\nFROM xapi_statement\n     JOIN actor ON actor.ifi = \n          (SELECT statement_to_actor.actor_ifi\n             FROM statement_to_actor\n            WHERE statement_to_actor.statement_id = xapi_statement.id\n              AND statement_to_actor.usage = 'Actor') \nWHERE EXISTS(\n      SELECT 1\n        FROM statement_to_activity\n       WHERE statement_to_activity.statement_id = xapi_statement.id\n         AND statement_to_activity.activity_iri = 'http://example.org/activity-id')  \nGROUP BY actor.actor_ifi\n",
        "https://id.openeel.org/xapi/extension/report-options": { }
      }
    },
    "objectType": "Activity"
  },
  "context": {
    "contextActivities": {
      "category": [
         {
           "id": "https://id.openeel.org/xapi/recipes/xapi-sql-query",
           "objectType": "Activity"
         }
      ]
    }
  }
}
```

### Appendix B: Example query response statement

```json
{
  "id": "3aeb4b68-3a59-43f3-a406-b399a6ea33d9",
  "actor": {
    "name": "uid",
    "mbox": "mailto:uid@example.com",
    "objectType": "Agent"
  },
  "verb": {
    "id": "https://id.openeel.org/xapi/verb/query-response"
  },
  "version": "1.0.0",
  "object": {
    "id": "6690e6c9-3ef0-4ed3-8b37-7f3964730bee",
    "objectType": "StatementRef"
  },
  "result": {
    "extensions": {
      "https://id.openeel.org/xapi/extension/query-result": {
        "columnNames": ["maxScore", "actorIfi"],
        "rows": [
          [0.9, "ifi1"],
          [0.1, "ifi2"]
        ]
      }
    }
  }
}
```

### Appendix C: Example queries

The server generating the response statement needs to enforce security rules. This is likely to
include:

* Running the query in a read-only transaction block
* Using a CTE to limit access to statements according to security rules for the user who requested
  the query.

Maximum score per user for a specific activity
```sql
SELECT MAX(jsonb(xapi_statement.payload, '$.result.score'))) AS maxScore,
       actor.actor_ifi AS actorIfi
FROM xapi_statement
     JOIN actor ON actor.ifi = 
          (SELECT statement_to_actor.actor_ifi
             FROM statement_to_actor
            WHERE statement_to_actor.statement_id = xapi_statement.id
              AND statement_to_actor.usage = 'Actor') 
WHERE EXISTS(
      SELECT 1
        FROM statement_to_activity
       WHERE statement_to_activity.statement_id = xapi_statement.id
         AND statement_to_activity.activity_iri = 'http://example.org/activity-id')  
GROUP BY actor.actor_ifi
```

Number of active users by day in a given time range:
```sql
SELECT COUNT(DISTINCT actor.actor_ifi) AS num_users,
       DATE(xapi_statement.timestamp) AS timestamp_day
FROM xapi_statement
WHERE xapi_statement.timestamp BETWEEN '2025-01-01' AND '2025-01-31'
GROUP BY DATE(xapi_statement.timestamp)
```
