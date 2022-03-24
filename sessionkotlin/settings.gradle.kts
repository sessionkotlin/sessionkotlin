pluginManagement {
    // Versions are declared in 'gradle.properties' file
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
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
include(":processor")
include(":tester")
