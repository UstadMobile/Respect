
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
        namespace = "${rootProject.group}.datalayer.http"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.respectLibUtil)
            implementation(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(projects.libDatalayer)
            implementation(libs.napier)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(projects.libDatalayerHttpServer)
            implementation(projects.libDatalayerDb)
            implementation(projects.respectLibTest)
            implementation(projects.respectLibTestResources)
            implementation(projects.libXapiTest)

            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)

            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
