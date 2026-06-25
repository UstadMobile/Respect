plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    id("maven-publish")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}


android {
    namespace = "world.respect.app.xapi.ipc.server"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "world.respect.xapi.ipc.server.InstrumentationTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    api(projects.respectLibXapiIpcShared)
    implementation(projects.respectLibUtil)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.rules)
    implementation(libs.urlencoder)


    testImplementation(libs.junit)
    androidTestImplementation(projects.respectLibXapiIpcClient)
    androidTestImplementation(projects.respectDatalayerDb)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(projects.respectLibTestResources)
    androidTestImplementation(kotlin("test"))
}