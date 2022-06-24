plugins {
    kotlin("jvm")
    application
}

val kotlinxCoroutinesVersion: String by project
val scribbleVersion: String by project

dependencies {
    implementation(project(":util"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.scribble:scribble-core:$scribbleVersion")
    implementation("org.scribble:scribble-runtime:$scribbleVersion")
}

application {
    mainClass.set("app.AppScribbleKt")
}
