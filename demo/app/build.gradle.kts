
plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("idea")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.david:sessionkotlin_lib:0.0.1")
    ksp("org.david:sessionkotlin_processor:0.0.1")
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