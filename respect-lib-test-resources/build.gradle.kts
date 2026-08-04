import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.test.resources"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    /*
     * This module MUST NOT depend on any other module within this project to avoid circular
     * dependencies. It provides utility functions that may be used in any other module.
     */
    sourceSets {
        commonMain.dependencies {
            implementation(projects.respectLibUtil)
            api(libs.uri.kmp)
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.date.time)
            implementation(libs.kotlinx.serialization.json)
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
