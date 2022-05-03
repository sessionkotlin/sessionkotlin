group = "org.david"
version = "0.0.1"

plugins {
    kotlin("jvm") apply false
    `java-library`
    id("org.jlleitschuh.gradle.ktlint") // Linter
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Linter

    repositories {
        mavenCentral()
    }

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        disabledRules.set(setOf("no-wildcard-imports"))
    }
}
