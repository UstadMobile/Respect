plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    id("maven-publish")
}

kotlin {
    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "world.respect.lib.cache"

        compilerOptions {

        }

        kotlin {
            jvmToolchain(17)
        }
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
