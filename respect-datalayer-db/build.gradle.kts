import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "${rootProject.group}.datalayer.db"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm()

    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.respectDatalayer)
            implementation(projects.respectLibPrimarykeygen)
            implementation(projects.respectLibXxhash)
            implementation(projects.respectLibUtil)
            implementation(libs.kotlinx.serialization.json)
            api(libs.uri.kmp)
            api(libs.kotlinx.date.time)
            api(libs.ktor.client.core)
            api(libs.androidx.room.runtime)
            api(libs.androidx.paging.common)
            api(libs.androidx.room.paging)
            implementation(libs.napier)

        }

        androidMain.dependencies {
            api(libs.androidx.room.ktx)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {
            implementation(libs.androidx.sqlite.bundled)
            implementation(projects.respectLibTestResources)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}


room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspJvm", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
}

