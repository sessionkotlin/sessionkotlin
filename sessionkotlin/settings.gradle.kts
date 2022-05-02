pluginManagement {
    // Versions are declared in 'gradle.properties' file
    val kotlinVersion: String by settings
    val klintVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version klintVersion apply false
    }
}

rootProject.name = "sessionkotlin"

include(":lib")
