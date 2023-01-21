plugins {
    kotlin("jvm")
    application
    id("com.github.sessionkotlin.plugin")
}

dependencies {
    api(project(":lib"))
}

application {
    mainClass.set("AppKt")
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/sessionkotlin/main/kotlin"),
    )
}