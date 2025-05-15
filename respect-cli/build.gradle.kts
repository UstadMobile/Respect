plugins {
    id("java-library")
    id("application")
    alias(libs.plugins.kotlinJvm)
}

application {
    mainClass = "world.respect.clitools.RespectCLI"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(libs.argparse4j)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":shared"))
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
