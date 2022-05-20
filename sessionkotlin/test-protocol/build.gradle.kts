plugins {
    kotlin("jvm")
    application
    id("com.github.d-costa.sessionkotlin.plugin")
}

dependencies {
    api(project(":sessionkotlin-lib"))
}

application {
    mainClass.set("AppKt")
}
