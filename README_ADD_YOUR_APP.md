# Add a new Launchable Education App

Intended audience: this guide is for app developers who want to add their app to an Open 
Educational Experience Launcher. The launcher supports web technology based apps (HTML/Javascript)
and Android native apps.

TODO: define terms

## Step 1: Create an app manifest and OPDS app catalog for your app
An Open Educational Experience Launcher gives students and teachers a way to browse a different
apps, launch them, browse lessons within them, and make/share collections of lessons.

**A) Create a launchable app manifest:** 

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
      "title": "Get it on Google Play"
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
For a more options see [README_LAUNCHABLE_APP.md](../respect-lib-opds-model/README_LAUNCHABLE_APP.md).

**B) Create a default collection of publications:**

Example (default-catalog.json):
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

**C) Create/generate a manifest for each learning unit**

Example (Lesson-manifest.json)
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

**D) Create a tincan.xml file for each learning unit**

Example:
```xml
<tincan xmlns="http://projecttincan.com/tincan.xsd">
    <activities>
        <activity id="https://demo.openeel.org/Lesson" type="http://activitystrea.ms/schema/1.0/game">
            <name>Lesson 1</name>
            <description lang="en-US">A demo lesson</description>
            <launch lang="en-us">https://demo.openeel.org/Lesson</launch>
        </activity>
    </activities>
</tincan>
```
The tincan.xml file is used as per the [Rustici Launch Method](https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md)
so learning units can send xAPI usage data back to the launcher.

## Step 2: Send xAPI usage data back to the launcher (HTML/Javascript apps)

Update your HTML/Javascript to check for the presence of the xAPI launch parameters and send data
back to the specified endpoint. This can be done using the [xAPI.js library](https://www.xapijs.dev/). 
This step can be skipped if your app is used exclusively as a native app.

e.g.

```javascript
//Downloaded as per https://github.com/xapijs/xapi/blob/develop/example/browser/module/index.html
import XAPI from "./xapi_module.js";
const { endpoint, auth } = XAPI.getTinCanLaunchData();

//As per https://www.xapijs.dev/xapi-wrapper-library/helpers
const queryParamsObject = XAPI.getSearchQueryParamsAsObject(location.search);

const searchParams = new URLSearchParams(location.search);

const xapi = new XAPI({
    endpoint: endpoint,
    auth: auth
});

function onLessonPassed() {
    const myStatement = {
        actor: queryParamsObject.actor,
        verb: {
            id: "http://adlnet.gov/expapi/verbs/passed",
        },
        object: {
            id: searchParams.get("activity_id")
        },
        result: {
            completion: true,
            success: true,
            score: {
                scaled: 1.0
            }
        }
    };

    console.log("XapiAuJs: Assignable unit: sending statement ");

    xapi.sendStatement({
        statement: myStatement
    }).then((result) => {
        console.log(result);
    }).catch((error) => {
        const errorStr = JSON.stringify(error.toJSON(), null, 2);
        console.log(errorStr);
    });
}
```

# Step 3: Send xAPI usage data back to the launcher (Native Android apps)

Update your app to use the xAPI-IPC library to send xAPI usage data back to the launcher:

```kotlin
@Composable
fun LessonScreen(
    modifier: Modifier = Modifier,
    lesson: LessonDestination
) {
    val context = LocalContext.current.getActivityContext()
    val json = remember {
        Json {
            encodeDefaults = false
        }
    }

    val ipcPackage = lesson.xapiIpcPackage
    val endpointUrl = lesson.endpoint?.let { Url(it) }
    val auth = lesson.auth

    val client = remember(ipcPackage, endpointUrl, auth) {
        if (ipcPackage != null && endpointUrl != null && auth != null) {
            XapiIpcClientBuilder(context, endpointUrl.toString())
                .setAuth(auth)
                .setJson(json)
                .setIpcServicePackageName(ipcPackage)
                .build()
        } else {
            null
        }
    }

    val actorObject = remember(lesson.actor) {
        lesson.actor?.let { json.decodeFromString(XapiAgent.serializer(), it) }
    }

    val activityIdVal = lesson.activity_id
    
    fun onLessonPassed() {
        if(actorObject != null && activityIdVal != null && scoreFloat != null && client != null) {
            val result = client.statements.post(
                listOf(
                    XapiStatement(
                        actor = actorObject,
                        verb = XapiVerb(id = "http://adlnet.gov/expapi/verbs/passed"),
                        `object` = XapiActivity(id = activityIdVal),
                        result = XapiResult(
                            completion = true,
                            success = true,
                            score = XapiResult.Score(
                                scaled = 1.0f
                            )
                        )
                    )
                )
            )

            resultStmtText = result.prettyResultString()
        }else {
            resultStmtText = "Could not send stmt: missing params"
        }
    }
}
```
See the full native demo app [on GitHub](https://www.github.com/UstadMobile/DemoLaunchableApp/).

# Step 4: Try adding your app on the launcher

Login to the RESPECT launcher as an admin, go the Apps list, click add, then enter the URL for your
launchable app manifest from step 1. You should now be able to browse lessons from the apps 
collections, launch learning units, and receive usage data using xAPI.

