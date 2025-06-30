import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}
compose.resources {
    publicResClass = true
}
kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        val commonMain by getting {
            resources.srcDir("src/commonMain/resources")
        }
        val androidMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
        }

        commonMain.dependencies {
            implementation(projects.shared)
            api(projects.respectDatasource)
            implementation(projects.respectDatasourceRepository)
            implementation(projects.respectDatasourceHttp)
            implementation(projects.respectDatasourceSqldelight)

            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(libs.runtime)
            implementation(libs.multiplatformsettings)
            implementation(libs.androidx.compose.material3.window.size)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.icons.extended)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.compose.bom)
            implementation(libs.kodein.compose)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.date.time)
            implementation(libs.coil3.coil.compose)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "world.respect.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "world.respect.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "world.respect.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "world.respect.app"
            packageVersion = "1.0.0"
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}