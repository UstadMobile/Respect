import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

compose.resources {
    publicResClass = true
    packageOfResClass = "world.respect.shared.generated.resources"
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
            api(projects.respectDatalayer)
            api(projects.respectCredentials)
            api(projects.respectLibUtil)
            api(projects.respectDatalayerDb)
            api(projects.respectDatalayerHttp)

            implementation(projects.respectLibCache)
            implementation(projects.respectLibXxhash)
            implementation(projects.respectLibPrimarykeygen)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
            implementation(libs.navigation.compose)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.argparse4j)
            api(libs.uri.kmp)
            implementation(libs.kotlinx.date.time)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(libs.multiplatformsettings)
            implementation(libs.napier)
            implementation(libs.qrose)

        }

        androidMain.dependencies {
            implementation(libs.androidx.preference)
            implementation(libs.androidx.preference.ktx)
            implementation(libs.acra.core)
            implementation(libs.libphonenumber.android)
            implementation(libs.androidx.biometric.ktx)
            implementation(libs.installreferrer)

            implementation(libs.androidx.browser)
        }

        jvmMain.dependencies {
            implementation(projects.respectDatalayerDb)
            implementation(libs.androidx.room.runtime)
            implementation(libs.json.schema.validator)
            implementation(libs.jsoup)
            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.webauthn4j.core)
            implementation(libs.libphonenumber.google)
        }

        jvmTest.dependencies {
            implementation(projects.respectLibTest)
            implementation(projects.respectLibSharedSe)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.server.conditional.headers)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.test)
            implementation(libs.mockito.kotlin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "world.respect.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
dependencies {
    implementation(project(":respect-datalayer-repository"))
    implementation(project(":respect-datalayer-repository"))
}
