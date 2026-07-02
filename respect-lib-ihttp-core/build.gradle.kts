plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("maven-publish")
}

kotlin {
    androidTarget {

    }

    jvm {

    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("stdlib-common"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            api(libs.kotlinx.io.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
        }

        jvmMain.dependencies {
            implementation(libs.okhttp)
            implementation(projects.respectLibIhttpIostreams)
        }

        androidMain.dependencies {
            implementation(projects.respectLibIhttpIostreams)
        }

    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "world.respect.lib.ihttp.core"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}