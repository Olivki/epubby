import name.remal.gradle_plugins.dsl.extensions.convention
import name.remal.gradle_plugins.dsl.extensions.get
import name.remal.gradle_plugins.dsl.extensions.implementation
import name.remal.gradle_plugins.plugins.publish.bintray.RepositoryHandlerBintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("name.remal:gradle-plugins:1.0.129")
    }
}

plugins {
    kotlin("jvm").version("1.3.41")
    kotlin("kapt").version("1.3.41")

    id("com.github.ben-manes.versions").version("0.21.0")
    
    `maven-publish`
}

apply(plugin = "name.remal.maven-publish-bintray")

group = "moe.kanon.epubby"
description = "Framework for working with the EPUB file format for Kotlin and Java."
version = "0.1.0"
val gitUrl = "https://gitlab.com/Olivki/epubby"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-html-jvm", version = "0.6.12")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable-jvm", version = "0.3")

    // Kanon
    implementation(group = "moe.kanon.kommons", name = "kommons.func", version = "1.4.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.reflection", version = "0.5.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.io", version = "1.4.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.lang", version = "0.2.0")
    implementation(group = "moe.kanon.kommons", name = "kommons.collections", version = "0.9.0")

    // XML
    implementation(group = "org.jdom", name = "jdom2", version = "2.0.6")

    // Apache
    implementation(group = "commons-validator", name = "commons-validator", version = "1.6")
    implementation(group = "org.apache.logging.log4j", name = "log4j-api-kotlin", version = "1.0.0")

    // css handler
    implementation(group = "com.helger", name = "ph-css", version = "6.2.0")

    // JSoup
    implementation(group = "org.jsoup", name = "jsoup", version = "1.12.1")

    // css dsl
    implementation(group = "azadev.kotlin", name = "aza-kotlin-css", version = "1.0")

    // Google
    implementation(group = "com.google.guava", name = "guava", version = "28.1-jre")
    compileOnly(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")
    kapt(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")

    // Test Dependencies
    testImplementation(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.11")
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.12.0")
    testImplementation(group = "org.fusesource.jansi", name = "jansi", version = "1.18")
    testImplementation(group = "org.slf4j", name = "slf4j-simple", version = "1.8.0-beta2")

    testCompileOnly(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")
    kaptTest(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

project.afterEvaluate {
    publishing.publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set(project.description)
            url.set(gitUrl)

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    email.set("oliver@berg.moe")
                    id.set("Olivki")
                    name.set("Oliver Berg")
                }
            }

            scm {
                url.set(gitUrl)
            }
        }
    }

    publishing.repositories.convention[RepositoryHandlerBintrayExtension::class.java].bintray {
        owner = "olivki"
        repositoryName = "epubby"
        packageName = "core"
    }
}