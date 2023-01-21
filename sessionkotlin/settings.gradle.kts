
pluginManagement {
    // Versions are declared in 'gradle.properties' file
    val kotlinVersion: String by settings
    val klintVersion: String by settings
    val dokkaVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version klintVersion apply false
        id("org.jetbrains.dokka") version dokkaVersion apply false
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
            // Load GitHub credentials
            val props = java.util.Properties()
            val envFile = File(rootDir.path + "/.env")
            if (envFile.exists())
                props.load(java.io.FileInputStream(envFile))

            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/sessionkotlin/sessionkotlin")
            credentials {
                username = props.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = props.getProperty("TOKEN") ?: System.getenv("TOKEN")
            }
        }
    }
}

rootProject.name = "sessionkotlin"

include(":parser")
include(":lib")
include(":plugin")
include(":test-protocol")
include(":test-app")
