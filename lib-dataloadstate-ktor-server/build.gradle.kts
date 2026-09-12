
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

kotlin {
    compilerOptions {
        jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.libDataloadstate)
            api(projects.respectLibUtil)
            api(libs.ktor.server.core)
        }

        jvmMain.dependencies {

        }

        jvmTest.dependencies {

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
