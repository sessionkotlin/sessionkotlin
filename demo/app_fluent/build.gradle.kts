plugins {
    kotlin("jvm")
    application
    id("com.github.d-costa.sessionkotlin.plugin") version "0.1"
}

dependencies {
    implementation(project(":protocols"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
}

application {
    // Define the main class for the application.
    mainClass.set("demo.AppKt")
}
