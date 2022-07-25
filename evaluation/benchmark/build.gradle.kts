import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import java.util.Properties
import java.io.FileInputStream

plugins {
    kotlin("jvm") version "1.7.0" apply false
    id("com.github.d-costa.sessionkotlin.plugin") version "2.0.0" apply false
}

subprojects{
    repositories {
        mavenCentral()
        maven {
            // Load GitHub credentials
            val props = java.util.Properties()
            val envFile = File(rootDir.path + "/.env")
            if (envFile.exists())
                props.load(java.io.FileInputStream(envFile))

            name = "SessionKotlin-GithubPackages"
            url = uri("https://maven.pkg.github.com/d-costa/sessionkotlin")
            credentials {
                username = props.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = props.getProperty("TOKEN") ?: System.getenv("TOKEN")
            }
        }
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}