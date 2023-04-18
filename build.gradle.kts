plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

group = "dev.epubby"
description = "Framework for working with the EPUB file format for Kotlin."
version = "0.0.1"

val kotestVersion: String by project

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")

    // XML
    implementation("org.jdom:jdom2:2.0.6.1")

    // IRI/URI
    implementation("org.xbib:net:3.0.1")

    // Zip
    api("net.lingala.zip4j:zip4j:2.11.5")

    // better-parse
    implementation("cc.ekblad.konbini:konbini:0.1.2")

    // Apache
    implementation("commons-validator:commons-validator:1.7")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("commons-io:commons-io:2.11.0")

    // bull
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger:1.0.4")
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.16")

    // css handler
    api("com.helger:ph-css:7.0.0")

    // JSoup
    api("org.jsoup:jsoup:1.15.4")

    // Google
    api("com.google.guava:guava:31.1-jre")
    implementation("com.google.jimfs:jimfs:1.2")

    // misc
    implementation("net.pearx.kasechange:kasechange:1.3.0")

    // test
    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")

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