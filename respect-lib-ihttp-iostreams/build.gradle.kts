plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("maven-publish")
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    }

    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.libihttpiostreams"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm{

    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.io.okio)
        }
    }
}
