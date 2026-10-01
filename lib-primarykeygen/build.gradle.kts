
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.libprimarykey"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.date.time)

            //Needs to be added explicitly to avoid crash on Android
            // See https://github.com/Kotlin/kotlinx-atomicfu/issues/145
            implementation(libs.atomicfu)
        }

        androidMain.dependencies {

        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }

    }
}
