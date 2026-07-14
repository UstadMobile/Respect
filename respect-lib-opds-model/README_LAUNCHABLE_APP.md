# Launchable App Publication

Launchable app publication:
```json

```

Learning unit (e.g. lesson or assessment) publication:

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
      "href": "lesson.json",
      "type": "application/opds-publication+json"
    },
    {
      "rel": "https://id.openeel.org/rel/tincanxml",
      "href": "tincan.xml",
      "type": "application/xml"
    },
    {
      "rel": "https://id.openeel.org/rel/launchable-app",
      "href": "https://demo.openeel.org/appmanifest.json",
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
* The ```readingOrder``` property can be used to provide a fallback when the app used to browse publications
  does not support xAPI launch so the user can still open the publication without support for xAPI.
* The link with rel ```https://id.openeel.org/rel/launchable-app``` links to the launchable app that
  the learning unit is related to.
