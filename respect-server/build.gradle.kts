import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    application
    alias(libs.plugins.swagger.generator)
}

group = "world.respect.app"
version = "1.0.0"

application {
    mainClass.set("world.respect.server.ServerAppMainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}


// As per https://swagger.io/docs/open-source-tools/swagger-codegen/codegen-v3/workflow-integration/
swaggerSources {
    create("respect") {
        setInputFile(project.file("src/main/resources/openapi/openapi.yaml"))
        code.language = "html2"
    }
}

dependencies {
    implementation(projects.respectLibShared)
    implementation(projects.respectDatalayer)
    implementation(projects.respectDatalayerDb)
    implementation(projects.respectLibXxhash)
    implementation(projects.respectLibPrimarykeygen)

    implementation(projects.respectLibIhttpCore)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)

    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.argparse4j)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.napier)

    testImplementation(libs.kotlin.test.junit)

    swaggerUI(libs.swagger.ui)
    swaggerCodegen(libs.swagger.codegen.cli)
}

tasks.named("build").dependsOn("generateSwaggerUI")

tasks.withType<Tar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
