
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
            api(projects.respectLibXapiCore)
            api(projects.respectLibSerializers)
            api(projects.respectLibDataloadstate)

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

        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
