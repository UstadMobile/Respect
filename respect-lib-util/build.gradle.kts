import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("maven-publish")
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.lib.util"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    /*
     * This module MUST NOT depend on any other module within this project to avoid circular
     * dependencies. It provides utility functions that may be used in any other module.
     */
    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            api(libs.uri.kmp)
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.date.time)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.urlencoder)
        }

        androidMain.dependencies {
            implementation(libs.acra.core)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {

        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
