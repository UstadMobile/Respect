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

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.respectLibShared)
            api(projects.respectDatalayer)
            api(projects.respectCredentials)
            api(projects.respectLibUtil)
            api(projects.respectDatalayerDb)

            implementation(libs.androidx.room.runtime)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(libs.multiplatformsettings)
            implementation(libs.napier)
        }

        androidMain.dependencies {

        }

        jvmMain.dependencies {
            implementation(projects.respectDatalayerDb)
        }

        jvmTest.dependencies {
            implementation(projects.respectLibTest)
            implementation(libs.androidx.sqlite.bundled)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "world.respect.sharedse"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
