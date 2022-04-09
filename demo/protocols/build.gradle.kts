
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api("org.david:sessionkotlin_lib:0.0.1")
}

application {
    // Define the main class for the application.
    mainClass.set("demo.AppKt")
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/sessionkotlin/main/kotlin"),
    )
}