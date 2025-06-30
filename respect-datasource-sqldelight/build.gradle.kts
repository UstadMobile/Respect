import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(projects.respectDatasource)
            api(libs.sqldelight.coroutine.extensions)
        }

        jvmMain.dependencies {
            implementation(libs.sqldelight.jvm.driver)
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

sqldelight {
    databases {
        create("RespectDb") {
            srcDirs("src/main/sqldelight")
            packageName.set("world.respect.datasource.sqldelight")
        }
    }
}

android {
    namespace = "world.respect.datasource.sqldelight"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
