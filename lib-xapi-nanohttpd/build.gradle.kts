
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.lib.xapi.nanohttpd"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.nanohttpd)
            api(projects.libXapiCore)
            api(projects.libSerializers)
            api(projects.libDataloadstate)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.urlencoder)

            implementation(libs.nanohttpd.nanolets)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            implementation(libs.napier)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(projects.libDatalayerDb)
            implementation(projects.libTest)
            implementation(projects.libTestResources)
            implementation(projects.libXapiTest)
            implementation(projects.libDatalayerHttpClient)

            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)

            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
