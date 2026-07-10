# Activity publication manifest extension

```https://id.openeel.org/extensions/activity/webpub-manifest-link```

A [WebPub Manifest](https://readium.org/webpub-manifest/) can represent how a collection of 
resources can be grouped together to represent a publication (see also [W3C Web Publications](https://w3c.github.io/dpub-pwp-ucr/)).

A publication manifest can also include a manifest listing all the resources required, enabling such
resources to be cached for future use offline.

An activity can use this extension as follows:
```
{
    "id":"https://example.com/xapi/activity",
    "definition":{
        "name":{ 
            "en-US":"An activity" 
        },
        "extensions": {
            "https://id.openeel.org/extensions/activity/webpub-manifest-link": "https://example.com/xapi/activity/manifest.json"
        }
    }
}
```

There are different ways to launch xAPI (e.g. CMI5, Rustici Launch method, etc) that then resolve
to a URL, but there _isn't_ a single way to go from a given activity ID to a launch URL.

Mapping rules:
* The OpdsPublication Metadata identifier _SHOULD_ match the Xapi Activity Id whenever possible.

