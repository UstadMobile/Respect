rootProject.name = "Respect"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(":app-android")
include(":respect-app-compose")
include(":respect-server")
include(":respect-lib-shared")
include(":respect-lib-shared-se")
include(":respect-cli")
include(":lib-datalayer")
include(":lib-datalayer-http-client")
include(":lib-datalayer-repository")
include(":lib-datalayer-db")
include(":respect-lib-util")
include(":respect-lib-xxhash")
include(":respect-lib-primarykeygen")
include(":respect-credentials")

include(":respect-lib-ihttp-core")
include(":respect-lib-ihttp-iostreams")
include(":respect-lib-ihttp-okhttp")
include(":respect-lib-ihttp-nanohttpd")
include(":respect-lib-cache")
include(":respect-lib-serializers")
include(":respect-lib-opds-model")
include(":respect-lib-test")
include(":respect-lib-test-resources")
include(":lib-xapi-core")
include(":lib-dataloadstate")
include(":lib-xapi-nanohttpd")
include(":lib-xapi-ipc-shared")
include(":lib-xapi-ipc-server")
include(":lib-xapi-ipc-client")
include(":lib-ipc-messagebridge")

include(":lib-http-ipc-shared")
include(":lib-http-ipc-client")
include(":lib-http-ipc-server")
include(":lib-xapi-test")
include(":lib-datalayer-http-server")
include(":lib-dataloadstate-ktor-server")
