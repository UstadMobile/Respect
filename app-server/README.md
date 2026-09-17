# app-server

This is a [KTOR](https://ktor.io/) RESPECT server application module.

Run from source:
```
./gradlew app-server:run
```

KTOR configuration parameters:
- ktor.extrastaticfiles.dir : path to a directory that will be served on /static-extra. Can be used to host 
  test assets.

OpenAPI (swagger) docs detailing the API endpoints are in [openapi.yaml](src/main/resources/openapi/openapi.yaml).

They can be viewed:
* Main branch: [Ustad Devserver](https://devserver3.ustadmobile.com/respect-openapi/)
* From source: run the server, then use /swagger (e.g. http://localhost:8098/swagger )

