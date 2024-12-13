plugins {
    application
    kotlin("multiplatform") version "2.1.0"
    kotlin("plugin.serialization") version "2.0.20"
}

repositories {
    mavenCentral()
}

kotlin {
    mingwX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            }
        }
    }
}

