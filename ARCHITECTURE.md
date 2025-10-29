# Architecture

RESPECT's network architecture is designed to:
* **Use existing open standards**: [OAuth](https://oauth.net/2/), [xAPI](https://www.xapi.com/), 
  [OneRoster](https://www.1edtech.org/standards/oneroster), and [Web Publication](https://github.com/readium/webpub-manifest).
* **Support digital sovereignty**: ensure users, organizations, and governments can decide where 
  they store their personal information.
* **Support offline usage and reducing data usage**: maximize support for offline usage (though
  some login mechanisms may require Internet access) and support a variety of techniques to reduce
  data usage (including peer-to-peer caching, ISP/mobile network content delivery networks, etc).
* **Be language and infrastructure agnostic**: APIs can be implemented using federated monoliths,
  clustering, microservices, or any combination thereof.
* **Be scalable**: allow the use of various different strategies for scalability (federation, 
  clustering, microservices, etc).

RESPECT's architecture includes:

## Schools
Each school can specify its own xAPI LRS endpoint and OneRoster endpoint. It may also implement some 
RESPECT extension endpoints (e.g. for user sign-up via invitations etc). A school is represented by 
a json file e.g.

GET https://school.example.org/respect-shool.json
```
{
    name: "School Name",
    self: "https://school.example.org/",
    xapi: "https://school.example.org/api/xapi/",
    oneRoster: "https://school.example.org/api/oneroster/",
    respectExt: "https://school.example.org/api/respect/" 
}
```
Grouping school-level data together enables each school to have its own database (e.g. 
[horizontal partitioning](https://en.wikipedia.org/wiki/Partition_(database)#Partitioning_methods)).
This dramatically reduces the work required to perform queries (e.g. searching a users table that
contains only a thousand or so users in a particular school instead of a table that contains
all users in a district/country). Reports above school level are almost always aggregate data which
can be handled using a REST API.

## Directories
**Directories** (typically country or regional level): a directory server lists schools. It is used
by the RESPECT app to find info for a given school name. Directory servers are also used when 
handling sign-up invitations.

Using a directory is recommended, but not required. Directories allow the user to:
* Find their school using the school name
* Join using a numerical invite code.

A user can manually enter a link for the school in the RESPECT app.

