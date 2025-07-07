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

include(":composeApp")
include(":server")
include(":shared")
include(":respect-cli")
include(":respect-datasource")
include(":respect-datasource-http")
include(":respect-datasource-repository")
include(":respect-datasource-db")
include(":respect-lib-util")
include(":respect-lib-xxhash")
include(":respect-lib-primarykeygen")
