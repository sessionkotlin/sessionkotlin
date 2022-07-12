plugins {
    kotlin("jvm")
    application
    id("com.github.d-costa.sessionkotlin.plugin")
}
val kotlinxCoroutinesVersion: String by project
val sessionkotlinVersion: String by project

dependencies {
    api("com.github.d-costa:sessionkotlin-lib:$sessionkotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}

application {
    mainClass.set("AppKt")
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/sessionkotlin/main/kotlin"),
    )
}

