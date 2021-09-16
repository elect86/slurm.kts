import magik.createGithubPublication
import magik.github

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.0.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version embeddedKotlinVersion

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    id("elect86.magik") version "0.2.0"
    `maven-publish`
}

version = "0.0.6"
group = "elect86"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(kotlin("bom", embeddedKotlinVersion)))

    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))

    // Use the Kotlin test library.
    testImplementation(kotlin("test"))

    // Use the Kotlin JUnit integration.
    //    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {

    test { useJUnitPlatform() }

}

publishing {
    publications {
        createGithubPublication {
            from(components["java"])
            //    artifact(sourceJar)
        }
        repositories {
            github {
                domain = "kotlin-graphics/mary" // aka https://github.com/kotlin-graphics/mary
            }
        }
    }
}