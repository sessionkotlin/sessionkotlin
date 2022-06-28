plugins {
    kotlin("jvm")
    application
    id("com.github.d-costa.sessionkotlin.plugin")
}

val kotlinxCoroutinesVersion: String by project

dependencies {
    api(project(":protocol"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}

application {
    mainClass.set("SMTPClientKt")
}

