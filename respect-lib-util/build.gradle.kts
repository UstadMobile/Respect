import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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

android {
    namespace = "world.respect.libutil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
