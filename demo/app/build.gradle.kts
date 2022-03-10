
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.31"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("idea")

}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Get latest version
    implementation("org.david:sessionkotlin:0.0.1")
}

application {
    // Define the main class for the application.
    mainClass.set("demo.AppKt")
}

idea {
    module {
        isDownloadJavadoc = true
    }
}