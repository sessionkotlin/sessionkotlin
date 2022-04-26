
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":protocols"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

application {
    // Define the main class for the application.
    mainClass.set("demo.AppKt")
}
