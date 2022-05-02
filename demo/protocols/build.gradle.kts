plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    application
}

dependencies {
    api("org.david:sessionkotlin:0.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
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
