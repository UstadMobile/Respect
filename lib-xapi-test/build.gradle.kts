
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
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.lib.xapi.test"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()


    sourceSets {
        commonMain.dependencies {
            api(projects.libXapiCore)

            api(projects.respectLibUtil)
            api(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(libs.xmlutil.serialization)

            implementation(kotlin("test"))
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation(projects.respectLibTestResources)
        }

        commonTest.dependencies {

        }
    }
}
