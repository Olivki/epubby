import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version("1.3.21")
    
    id("kotlinx-serialization").version("1.3.21") // For serialization.
    id("com.adarshr.test-logger").version("1.6.0") // For pretty-printing for tests.
    id("com.jfrog.bintray").version("1.8.4") // For publishing to BinTray.
    id("org.jetbrains.dokka").version("0.9.17") // The KDoc engine.
    id("com.github.ben-manes.versions").version("0.20.0") // For checking for new dependency versions.
    
    `maven-publish`
}

// Project Specific Variables
group = "moe.kanon.epubby"
description = "Framework for working with the EPUB file format for Kotlin and Java."
version = "0.1.0"
val artifactName = "epubby"
val gitUrl = "https://gitlab.com/Olivki/epubby"

// General Tasks
repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx") { setName("kotlinx") }
}

dependencies {
    // Normal Dependencies
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-html-jvm", version = "0.6.12")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable", version = "0.1")
    
    // Kanon
    implementation(group = "moe.kanon.kommons", name = "kanon.kommons", version = "0.6.0-alpha-3")
    implementation(group = "moe.kanon.xml", name = "kanon.xml", version = "2.0.0")
    
    // css handler
    implementation(group = "com.helger", name = "ph-css", version = "6.1.2")
    
    // kotlin logging wrapper
    implementation(group = "io.github.microutils", name = "kotlin-logging", version = "1.6.24")
    
    // Arrow
    // functional programming enhancements for kotlin, only the core for now.
    compile(group = "io.arrow-kt", name = "arrow-core-data", version = "0.9.0")
    
    // JSoup
    implementation(group = "org.jsoup", name = "jsoup", version = "1.11.3")
    
    // Swiftzer
    implementation(group = "com.github.zafarkhaja", name = "java-semver", version = "0.9.0")
    
    // Test Dependencies
    testImplementation(group = "io.kotlintest", name = "kotlintest-runner-junit5", version = "3.1.11")
    testImplementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.11.1")
    testImplementation(group = "org.fusesource.jansi", name = "jansi", version = "1.17.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
}

// Dokka Tasks
val dokkaJavaDoc by tasks.creating(DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
    inputs.dir("src/main/kotlin")
    includeNonPublic = false
    skipEmptyPackages = true
    jdkVersion = 8
}

// Test Tasks
testlogger {
    setTheme("mocha")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Artifact Tasks
val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    description = "Assembles the sources of this project into a *-sources.jar file."
    classifier = "sources"
    
    from(project.sourceSets["main"].allSource)
}

val javaDocJar by tasks.creating(Jar::class) {
    description = "Creates a *-javadoc.jar from the generated dokka output."
    classifier = "javadoc"
    
    from(dokkaJavaDoc)
}

artifacts {
    add("archives", sourcesJar)
    add("archives", javaDocJar)
}

// Publishing Tasks
// BinTray
bintray {
    // Credentials.
    user = getVariable("BINTRAY_USER")
    key = getVariable("BINTRAY_KEY")
    
    // Whether or not the "package" should automatically be published.
    publish = true
    
    // Sets the publication to our created maven publication instance.
    setPublications("mavenPublication")
    
    // Details for the actual package that's going up on BinTray.
    with(pkg) {
        repo = "kanon"
        desc = project.description
        name = artifactName
        websiteUrl = gitUrl
        vcsUrl = "$gitUrl.git"
        publicDownloadNumbers = true
        setLicenses("Apache-2.0")
        setLabels("kotlin")
        
        with(version) {
            name = project.version.toString()
            desc = project.version.toString()
            released = `java.util`.Date().toString()
        }
    }
}

// Maven Tasks
publishing {
    publications.invoke {
        register("mavenPublication", MavenPublication::class.java) {
            from(components["java"])
            
            afterEvaluate {
                // General project information.
                groupId = project.group.toString()
                version = project.version.toString()
                artifactId = artifactName
                
                // Any extra artifacts that need to be added, ie: sources & javadoc jars.
                artifact(sourcesJar)
                artifact(javaDocJar)
                
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
        }
    }
}

// Misc Functions & Properties
fun getVariable(name: String) = System.getenv(name)!!