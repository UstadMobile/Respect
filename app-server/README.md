# app-server

This is a [KTOR](https://ktor.io/) RESPECT server application module.

Run from source:
```
./gradlew app-server:run
```

KTOR configuration parameters:
- ktor.extrastaticfiles.dir : path to a directory that will be served on /static-extra. Can be used to host 
  test assets.
