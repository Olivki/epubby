group = "dev.epubby"
description = "Framework for working with the EPUB file format for Kotlin."
version = "0.0.1"

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))

    // XML
    implementation("org.jdom:jdom2:2.0.6.1")

    // Apache
    implementation("commons-validator:commons-validator:1.7")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("commons-io:commons-io:2.11.0")

    // logging
    implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger:1.0.4")
    implementation("io.arrow-kt:arrow-core:1.1.2")

    // css handler
    api("com.helger:ph-css:7.0.0")

    // JSoup
    api("org.jsoup:jsoup:1.15.4")

    // Google
    api("com.google.guava:guava:31.1-jre")
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}