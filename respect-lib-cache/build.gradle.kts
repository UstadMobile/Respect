import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.atomicfu)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {

    }

    sourceSets {
        commonMain.dependencies  {
            implementation(kotlin("stdlib-common"))
            api(projects.respectLibIhttpCore)
            implementation(projects.respectLibOpdsModel)
            implementation(projects.respectLibIhttpIostreams)
            implementation(projects.respectLibXxhash)
            implementation(projects.respectLibUtil)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.atomicfu)
            implementation(libs.kotlinx.io.core)
            implementation(libs.ktor.client.core)
            implementation(libs.napier)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.nanohttpd)
            implementation(libs.okhttp)
            implementation(projects.respectLibIhttpOkhttp)
            implementation(libs.androidx.room.runtime)
            implementation(libs.kotlinx.date.time)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
        }

        jvmMain.dependencies {
            implementation(libs.okhttp)
        }

        jvmTest.dependencies {
            implementation(libs.mockwebserver)
            implementation(libs.mockito.kotlin)
            implementation(libs.turbine)
            implementation(libs.androidx.sqlite.bundled)
            implementation(projects.respectLibIhttpNanohttpd)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.conditional.headers)
            implementation(libs.ktor.server.auto.head.response)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.room.ktx)
            implementation(libs.androidx.lifecycle.common.java8)
            implementation(libs.androidx.work.runtime)
            implementation(libs.koin.android)
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


android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "world.respect.lib.cache"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}
