
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.libxxhash"
        minSdk = libs.versions.android.minSdk.get().toInt()

    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            //Works because this module is currently JVM and Android. Can be moved into another
            //module as/when iOS/JS target is added
            implementation(libs.lz4.pure.java)
            implementation(libs.atomicfu)
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

