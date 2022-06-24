plugins {
    kotlin("jvm")
    application
    id("com.github.d-costa.sessionkotlin.plugin")
}

val kotlinxCoroutinesVersion: String by project

dependencies {
    implementation(project(":protocols"))
    implementation(project(":util"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}

application {
    mainClass.set("app.AppFluentKt")
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/sessionkotlin/main/kotlin"),
    )
}
