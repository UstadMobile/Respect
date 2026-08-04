# Architecture

RESPECT is an open source digital library for EdTech apps. It makes it easier for educators to
discover and use high-quality apps with a single account in all settings, while giving developers
the platform they need to grow their impact globally.

RESPECT's architecture is designed to:
* **Use existing open standards**: [OAuth](https://oauth.net/2/), [xAPI](https://www.xapi.com/), 
  and [Web Publication](https://github.com/readium/webpub-manifest).
* **Support digital sovereignty**: ensure users, organizations, and governments can decide where 
  they store their personal information.
* **Support offline usage and reducing data usage**: maximize support for offline usage (though
  some login mechanisms may require Internet access) and support a variety of techniques to reduce
  data usage (including peer-to-peer caching, ISP/mobile network content delivery networks, etc).
* **Be scalable**: allow the use of various different strategies for scalability (federation, 
  clustering, microservices, etc).
* **Be resource efficient on mobile devices and servers**: RESPECT is intended to serve as many 
   users as possible, including those who have older devices and budget smartphones. It is also
   intended to work for organizations (such as education ministries) with limited resources: it must 
   support scale without generating unsustainable server bills.

RESPECT's architecture includes:

## School API endpoint

Each school can specify its own xAPI LRS endpoint and RESPECT app endpoints (which can handle user
management, invitations, RESPECT app native support for passkeys, etc). A school is represented by 
a json file e.g.

GET https://school.example.org/respect-shool.json
```
{
    name: "School Name",
    self: "https://school.example.org/",
    xapi: "https://school.example.org/api/xapi/",
    respectExt: "https://school.example.org/api/respect/" 
}
```

Grouping school-level data together enables each school to have its own database (e.g.
[horizontal partitioning](https://en.wikipedia.org/wiki/Partition_(database)#Partitioning_methods)).
This dramatically reduces the work required to perform queries (e.g. searching a users table that
contains only a thousand or so users in a particular school instead of a table that contains
all users in a district/country). Reports above school level are almost always aggregate data which
can be handled using a REST API.

## School Directory API endpoint
**Directories** (typically country or regional level): a directory server lists schools. It is used
by the RESPECT app to find info for a given school name.

Using a directory is recommended, but not required.

A user can manually enter a link for the school in the RESPECT app.

## RESPECT Compatible Apps

A RESPECT Compatible app is an edtech app (such as a math app, language learning app, or attendance
tracker app). It can be a web based app or native (Android) app. RESPECT Compatible apps can:
* **Publish lists of learning units/lessons**: allowing users of the RESPECT app to browse available
   lessons and create playlists or assignments containing lessons from multiple different apps.
* **Send user progress information to RESPECT using open standards**: RESPECT Compatible apps can 
  send the RESPECT app user progress information (even when offline), which is then relayed by the
  RESPECT app to the school's own LRS endpoint (requires permission from school admin/users).
* **Retrieve user profile information**: retrieve basic rostering and profile information such as
  class lists, enrollments, name, and grade level (requires permission from school admin/users).


