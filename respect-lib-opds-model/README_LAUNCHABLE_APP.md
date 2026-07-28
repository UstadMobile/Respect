# Launchable App Manifest

The launchable app publication describes a specific app e.g. a math app, assessment app, etc. It is
a [Readium Manifest](https://readium.org/webpub-manifest/) with a set of specific link relations.

Example:
```json
{
  "metadata": {
    "@type": "https://id.openeel.org/schema/launchable-app",
    "title": "Demo Launchable App",
    "author": {
      "name": "Ustad Mobile FZ-LLC",
      "links": {
        "href": "https://www.ustadmobile.com/"
      }
    },
    "identifier": "https://demo.openeel.org/app",
    "language": "en",
    "modified": "2025-09-29T17:00:00Z"
  },
  "links": [
    {
      "rel": "self",
      "href": "https://demo.openeel.org/launchable-app.json",
      "type": "application/opds-publication+json"
    },
    {
      "rel": "collection",
      "href": "https://demo.openeel.org/default-catalog.json",
      "type": "application/opds+json"
    },
    {
      "rel": "https://id.openeel.org/rel/app-launch-uri",
      "href": "https://demo.openeel.org/"
    },
    {
      "rel": "https://id.openeel.org/rel/appstore-android",
      "href": "https://play.google.com/store/apps/details?id=demo.openeel.org",
      "title": "Get it on Google Play",
      "alternate": [
        {
          "href": "https://f-droid.org/en/packages/demo.openeel.org/",
          "title": "Get it from F-Droid"
        }
      ]
    },
    {
      "rel": "https://id.openeel.org/rel/app-highlight-card",
      "title": "Case Study",
      "href": "https://demo.openeel.org/casestudy.html"
    },
    {
      "rel": "https://id.openeel.org/rel/app-highlight-card",
      "title": "Case Study 2",
      "href": "https://demo.openeel.org/casestudy2.html"
    },
    {
      "rel": "terms-of-service",
      "href": "https://demo.openeel.org/terms-privacy.html"
    },
    {
      "rel": "license",
      "href": "http://opensource.org/licenses/MIT"
    }
  ],
  "images": [
    {
      "href": "https://demo.openeel.org/app-icon.png",
      "type": "image/png"
    }
  ]
}
```
Notes:
* The ```rel=collection``` link SHOULD link to a default collection of learning units (e.g. lessons) 
  - see below.
* The ```rel=https://id.openeel.org/rel/app-launch-uri``` href the link that will be opened when the
  user opens an app directly from the app detail screen.
* The ```rel=https://id.openeel.org/rel/appstore-android``` href MUST be a link to an Android app
  store _if_ an app has a native app version (normally Google Play). The ```alternate``` subsection 
  of links can provide links to other app stores if desired.
* The ```rel=terms-of-service``` href MUST be a link to the terms and conditions of the app (
  e.g. including privacy policy)
* The ```rel=https://id.openeel.org/rel/app-badge``` MAY be used to provide links to evidence, case
  studies, or endorsements of an app.

# Collections: 

Collections are listings that can link to other feeds or to publications (learning units) themselves. 
They are simply [OPDS2.0 collections](https://specs.opds.io/opds-2.0.html#2-collections)

e.g.
```json
{
  "metadata": {
    "title": "Default Lesson collection"
  },

  "links": [
    {"rel": "self", "href": "https://demo.openeel.org/default-catalog.json", "type": "application/opds+json"}
  ],

  "publications": [
    {
      "metadata": {
        "@type": "http://schema.org/Game",
        "title": "Native Demo 001",
        "author": "Mullah Nasruddin",
        "identifier": "https://demo.openeel.org/Lesson",
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
          "href": "tincan.xml",
          "type": "application/xml"
        },
        {
          "rel": "https://id.openeel.org/rel/launchable-app",
          "href": "https://demo.openeel.org/launchable-app.json",
          "type": "application/opds-publication+json"
        }
      ],
      "images": [
        {"href": "Lesson-cover.png", "type": "image/png"  }
      ]
    }
  ]
}
```

# Publications

Publications represent a specific learning unit (e.g. a lesson, game, or assessment) that can be
launched.

```json
{
  "metadata": {
    "@type": "http://schema.org/Game",
    "title": "Lesson 001",
    "author": "Mullah Nasruddin",
    "identifier": "https://example.app/id/lesson001",
    "language": "en",
    "modified": "2015-09-29T17:00:00Z"
  },
  "links": [
    {
      "rel": "self",
      "href": "https://demo.openeel.org/Lesson-manifest.json",
      "type": "application/opds-publication+json"
    },
    {
      "rel": "https://id.openeel.org/rel/tincanxml",
      "href": "tincan.xml",
      "type": "application/xml"
    },
    {
      "rel": "https://id.openeel.org/rel/launchable-app",
      "href": "https://demo.openeel.org/launchable-app.json",
      "type": "application/opds-publication+json"
    }
  ],
  "readingOrder": [
    {
      "href": "https://demo.openeel.org/Lesson",
      "type": "text/html"
    }
  ],
  "images": [
    {
      "href": "cover.png",
      "type": "image/png"
    }
  ],
  "resources": [
    {
      "href": "audio.ogg",
      "type": "audio/ogg"
    },
    {
      "href": "video.mp4",
      "type": "video/mp4"
    }
  ]
}
```
Notes:
* The ```rel=https://id.openeel.org/rel/tincanxml``` links to the tincan.xml file that provides the
  launchable link and xAPI activity ID. See [README_XAPI_OPDS.md](../respect-lib-xapi-core/README_XAPI_OPDS.md).
* The ```readingOrder``` property can be used to provide a fallback when the app used to browse publications
  does not support xAPI launch so the user can still open the publication without support for xAPI.
* The link with rel ```https://id.openeel.org/rel/launchable-app``` links to the launchable app that
  the learning unit is related to.
