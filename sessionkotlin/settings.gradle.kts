pluginManagement {
    // Versions are declared in 'gradle.properties' file
    val kotlinVersion: String by settings
    val klintVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version klintVersion apply false
    }
    repositories {
        mavenLocal()

        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "sessionkotlin"

include(":lib")
