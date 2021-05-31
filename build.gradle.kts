import name.remal.gradle_plugins.dsl.extensions.convention
import name.remal.gradle_plugins.dsl.extensions.get
import name.remal.gradle_plugins.plugins.publish.ossrh.RepositoryHandlerOssrhExtension

plugins {
    kotlin("jvm").version("1.5.10")
    kotlin("kapt").version("1.5.10")
    id("name.remal.maven-publish-ossrh") version "1.3.1"
    id("name.remal.check-dependency-updates") version "1.3.1"
    `maven-publish`
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
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-html-jvm", version = "0.6.12")
    api(group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable-jvm", version = "0.3.3")

    // Kanon
    implementation(group = "moe.kanon.kommons", name = "kommons.func", version = "2.0.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.reflection", version = "0.6.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.io", version = "1.5.1")
    implementation(group = "moe.kanon.kommons", name = "kommons.lang", version = "0.6.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.collections", version = "0.14.1")

    // kassava
    implementation(group = "au.com.console", name = "kassava", version = "2.1.0-rc.1")

    // XML
    implementation(group = "org.jdom", name = "jdom2", version = "2.0.6")

    // Apache
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "org.apache.commons", name = "commons-collections4", version = "4.4")
    implementation(group = "commons-io", name = "commons-io", version = "2.7")

    // logging
    implementation(group = "com.michael-bull.kotlin-inline-logger", name = "kotlin-inline-logger", version = "1.0.2")

    // css handler
    api(group = "com.helger", name = "ph-css", version = "6.2.0")
    //implementation(group = "net.sf.cssbox", name = "jstyleparser", version = "3.5")

    // JSoup
    api(group = "org.jsoup", name = "jsoup", version = "1.12.1")

    // css dsl
    implementation(group = "azadev.kotlin", name = "aza-kotlin-css", version = "1.0")

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
            jvmTarget = "1.8"
            freeCompilerArgs = freeCompilerArgs  + listOf("-Xuse-experimental=kotlin.Experimental")
        }
    }
}

project.afterEvaluate {
    publishing.publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set(project.description)
            url.set("https://github.com/Olivki/epubby")

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("Olivki")
                    name.set("Oliver Berg")
                    email.set("oliver@berg.moe")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/Olivki/epubby")
                developerConnection.set("scm:git:ssh://github.com/Olivki/epubby")
                url.set("https://github.com/Olivki/epubby")
            }
        }
    }

    publishing.repositories.convention[RepositoryHandlerOssrhExtension::class.java].ossrh()
}