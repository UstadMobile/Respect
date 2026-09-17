# OPDS Collections Activity Profile

Profile ID: https://id.openeel.org/profile/activity/opds-collection

Profile ID used to store and retrieve an OPDS collection using the 
[xAPI Activity Profile Resource](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#27-activity-profile-resource).

The Activity ID should generally be the identifier of the OPDS collection itself.

e.g.
```json
{
  "metadata": {
    "title": "Default Lesson collection"
  },

  "links": [
    {
      "rel": "self", 
      "href": "https://school.example.org/xapi/activities/profile?activityId=[https://school.example.org/collections/uuid]&profileId=[https://id.openeel.org/profile/activity/opds-collection]", 
      "type": "application/opds+json"
    }
  ],

  "publications": [
    {
      "metadata": {
        "@type": "http://schema.org/Game",
        "title": "Native Demo 001",
        "author": "Mullah Nasruddin",
        "identifier": "https://demo.openeel.org/en-US/grade/1/learningunits/1/",
        "language": "en",
        "modified": "2015-09-29T17:00:00Z",
        "subject": [
          {
            "name": "Mathematics",
            "scheme": "https://www.bisg.org/#bisac",
            "code": "MAT000000"
          }
        ]
      },
      "links": [
        {"rel": "self", "href": "Lesson-manifest.json", "type": "application/opds-publication+json"},
        {
          "rel": "https://id.openeel.org/rel/tincanxml",
          "href": "https://demo.openeel.org/en-US/grade/1/learningunits/1/tincan.xml",
          "type": "application/xml"
        },
        {
          "rel": "https://id.openeel.org/rel/launchable-app",
          "href": "https://demo.openeel.org/en-US/launchable-app-manifest.json",
          "type": "application/opds-publication+json"
        }
      ],
      "images": [
        {"href": "https://demo.openeel.org/en-US/grade/1/learningunits/1/Lesson-cover.png", "type": "image/png"  }
      ]
    }
  ]
}
```

Note: activityId and profileId are shown without URL encoding for readability. In actual usage they
must be URL-encoded.

Storage MUST be done using PUT (not POST), otherwise the xAPI document storage merge behavior could 
lead to invalid OPDS.
