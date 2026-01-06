import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import java.util.Properties
import java.io.FileInputStream

//As per: https://developer.android.com/studio/publish/app-signing.html#kts
// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = System.getenv("KEYSTORE")?.let {
    File(it)
} ?: rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.takeIf { keystorePropertiesFile.exists() }
    ?.load(FileInputStream(keystorePropertiesFile))

val acraProperties = Properties()
val acraPropertiesFile = System.getenv("ACRA")?.let {
    File(it)
} ?: rootProject.file("acra.properties")

acraProperties.takeIf { acraPropertiesFile.exists() }
    ?.load(FileInputStream(acraPropertiesFile))

// The applist list - see main README
val defaultAppList = System.getenv("RESPECT_DEFAULT_APPLIST") ?: "https://respect.world/respect-ds/manifestlist.json"

val ACRA_PROP_NAMES = listOf("uri", "basicAuthLogin", "basicAuthPassword")

ACRA_PROP_NAMES.forEach { propName ->
    System.getenv("ACRA_${propName.uppercase()}")?.also {
        acraProperties.setProperty(propName, it)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildconfigPlugin)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

compose.resources {
    publicResClass = true
    packageOfResClass = "world.respect.app.generated.resources"
}

buildConfig {
    packageName("world.respect.app.config")
    className("RespectBuildConfig")

    buildConfigField<String>("RESPECT_DEFAULT_APPLIST", defaultAppList)
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

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
            api(projects.respectCredentials)
            implementation(projects.respectLibSharedSe)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.service.auth)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.koin.android)
            implementation(libs.okhttp)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.material3.window.size.clazz)
            implementation(projects.respectDatalayerDb)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.webkit)
            implementation(libs.material)
            implementation(libs.androidx.appcompat)
            implementation(libs.coil3.coil.svg)
            implementation(libs.acra.http)
            implementation(libs.acra.core)
            implementation(libs.libphonenumber.android)
        }

        commonMain.dependencies {
            implementation(projects.respectLibShared)
            api(projects.respectDatalayer)
            api(projects.respectLibXxhash)
            implementation(projects.respectDatalayerRepository)
            implementation(projects.respectDatalayerHttp)
            implementation(projects.respectLibPrimarykeygen)
            implementation(projects.respectLibCache)

            implementation(libs.napier)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.foundation)
            implementation(compose.runtime)
            implementation(libs.multiplatformsettings)
            implementation(compose.materialIconsExtended)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.date.time)
            implementation(libs.coil3.coil.compose)
            implementation(libs.coil.network.okhttp)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.date.time)
            implementation(libs.koalaplot)
            implementation(libs.kotlinx.io.core)
            implementation(libs.androidx.paging.compose)
            implementation(libs.reorderable)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(projects.respectLibSharedSe)
        }
    }
}

android {
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        println("Keystore exists: ${keystorePropertiesFile.exists()}")
        //See https://developer.android.com/build/building-cmdline#gradle_signing
        if(keystorePropertiesFile.exists()) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    namespace = "world.respect.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "world.respect.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 16
        versionName = "1.0.16"

        for(propName in ACRA_PROP_NAMES) {
            buildConfigField(
                type = "String",
                name = "ACRA_${propName.uppercase()}",
                value = "\"${acraProperties.getProperty(propName) ?: ""}\"   "
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            if(keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }


            proguardFiles(
                // Default file with automatically generated optimization rules.
                getDefaultProguardFile("proguard-android-optimize.txt"),
                project.file("proguard-rules.pro")
            )

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