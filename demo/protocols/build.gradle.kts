plugins {
    kotlin("jvm")
    id("com.github.d-costa.sessionkotlin.plugin")
    application
}

dependencies {
    api("com.github.d-costa:sessionkotlin-lib:0.1.1")
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
