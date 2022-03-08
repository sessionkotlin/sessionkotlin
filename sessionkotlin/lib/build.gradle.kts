/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.4/userguide/building_java_projects.html
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.6.10"


    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    `maven-publish`
}

group = "com.david"
version = "0.0.1"
//sourceCompatibility = '11'

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.10-1.0.2")

}

tasks.test {
    useJUnitPlatform()
}


publishing {
    publications {

        create<MavenPublication>("maven") {
            groupId = "org.david"
            artifactId = "sessionkotlin"
            version = "0.1"

            from(components["java"])

            pom {
                name.set("SessionKotlin")
                description.set("Multiparty Session Types in Kotlin ")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }

    }
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to rootProject.name,
            "Implementation-Version" to project.version))
    }
    archiveBaseName.set(rootProject.name)
}


