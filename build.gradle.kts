plugins {
    kotlin("jvm").version("1.7.10")
    kotlin("kapt").version("1.7.10")
}

group = "dev.epubby"
description = "Framework for working with the EPUB file format for Kotlin."
version = "0.0.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https//maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-html-jvm", version = "0.7.3")
    api(group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable-jvm", version = "0.3.3")

    implementation(group = "moe.kanon.krautils", name = "krautils-core", version = "0.0.6")
    implementation(group = "moe.kanon.krautils", name = "krautils-scalr", version = "1.0.0")

    // XML
    implementation(group = "org.jdom", name = "jdom2", version = "2.0.6")

    // Apache
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "org.apache.commons", name = "commons-collections4", version = "4.4")
    implementation(group = "commons-io", name = "commons-io", version = "2.7")

    // logging
    implementation(group = "com.michael-bull.kotlin-inline-logger", name = "kotlin-inline-logger", version = "1.0.2")
    implementation("io.arrow-kt:arrow-core:1.1.2")

    // css handler
    api(group = "com.helger", name = "ph-css", version = "6.2.0")
    //implementation(group = "net.sf.cssbox", name = "jstyleparser", version = "3.5")

    // JSoup
    api(group = "org.jsoup", name = "jsoup", version = "1.12.1")

    // css dsl
    // replace this with https://github.com/JetBrains/kotlin-wrappers/tree/master/kotlin-css ?
    //implementation(group = "azadev.kotlin", name = "aza-kotlin-css", version = "1.0")

    // Google
    api(group = "com.google.guava", name = "guava", version = "28.1-jre")
    compileOnly(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")
    kapt(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")

    // scalr
    api(group = "org.imgscalr", name = "imgscalr-lib", version = "4.2")

    // Test Dependencies
    testImplementation(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.11")
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.12.0")
    testImplementation(group = "org.fusesource.jansi", name = "jansi", version = "1.18")
    testImplementation(group = "org.slf4j", name = "slf4j-simple", version = "1.8.0-beta2")

    testCompileOnly(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")
    kaptTest(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-Xjvm-default=all",
            )
        }
    }
}