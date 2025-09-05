import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            api(libs.uri.kmp)
            api(libs.ktor.client.core)
            implementation(projects.respectLibUtil)
            implementation(projects.respectLibPrimarykeygen)
            implementation(projects.respectDatalayerDb)
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(libs.kotlinx.serialization.json)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(projects.respectAppCompose)
        }

    }
    sourceSets.androidInstrumentedTest.dependencies {
        implementation(kotlin("test"))
        implementation(projects.respectAppCompose)

    }
}

android {
    namespace = "world.respect.credentials"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
