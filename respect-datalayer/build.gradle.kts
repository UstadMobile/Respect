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
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.datalayer"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.respectLibDataloadstate)
            api(projects.respectLibIhttpCore)
            api(projects.respectLibXxhash)
            api(projects.respectCredentials)
            api(projects.respectLibOpdsModel)
            api(projects.respectLibXapiCore)
            api(projects.respectLibSerializers)
            api(projects.respectLibUtil)
            implementation(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(libs.androidx.paging.common)
            implementation(libs.atomicfu)
            implementation(libs.napier)

        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(projects.respectLibTestResources)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
