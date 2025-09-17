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
            implementation(projects.respectLibUtil)
            api(libs.uri.kmp)
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.date.time)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(kotlin("test"))
            implementation(libs.kotlin.test.junit)


            implementation(projects.respectLibPrimarykeygen)
            implementation(projects.respectLibXxhash)

            implementation(libs.turbine)
            implementation(projects.respectDatalayerHttp)
            implementation(projects.respectDatalayerDb)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)


            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)



            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.mockito.kotlin)


        }

        jvmMain.dependencies {
            implementation(projects.respectServer)
            implementation(projects.respectLibShared)
            implementation(projects.respectDatalayerRepository)
            implementation(projects.respectDatalayerDb)
            implementation(projects.respectDatalayerHttp)
            implementation(libs.koin.ktor)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.conditional.headers)
            implementation(libs.ktor.server.call.logging)
            implementation(libs.logback)
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
    namespace = "world.respect.lib.test"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
