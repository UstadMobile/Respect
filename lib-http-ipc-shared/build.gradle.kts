plugins {
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
}

android {
    namespace = "org.openeel.httpipc.core"
    compileSdk {
        version = release(libs.versions.android.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    api(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
