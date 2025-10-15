rootProject.name = "Respect"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

    }
}

include(":respect-app-compose")
include(":respect-server")
include(":respect-lib-shared")
include(":respect-lib-shared-se")
include(":respect-cli")
include(":respect-datalayer")
include(":respect-datalayer-http")
include(":respect-datalayer-repository")
include(":respect-datalayer-db")
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
