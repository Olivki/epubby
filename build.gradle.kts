plugins {
    kotlin("jvm") version "1.8.20-RC"
}

group = "dev.epubby"
description = "Framework for working with the EPUB file format for Kotlin."
version = "0.0.1"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))

    // XML
    implementation("org.jdom:jdom2:2.0.6")

    // Apache
    implementation("commons-validator:commons-validator:1.6")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("commons-io:commons-io:2.7")

    // logging
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger:1.0.2")
    implementation("io.arrow-kt:arrow-core:1.1.2")

    // css handler
    api("com.helger:ph-css:6.2.0")

    // JSoup
    api("org.jsoup:jsoup:1.12.1")

    // Google
    api("com.google.guava:guava:28.1-jre")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=kotlin.contracts.ExperimentalContracts")
        }
    }
}