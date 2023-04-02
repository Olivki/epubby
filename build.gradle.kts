plugins {
    kotlin("jvm") version "1.8.20-RC"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    tasks {
        compileKotlin {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=kotlin.contracts.ExperimentalContracts")
            }
        }

        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
}