plugins {
    kotlin("jvm")
    application
    id("com.github.d-costa.sessionkotlin.plugin")
}

val kotlinxCoroutinesVersion: String by project

dependencies {
    implementation(project(":protocol"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}

application {
    mainClass.set("SMTPClientKt")
}

/**
 * Note:
 *
 * Comment everything below this if you want to run the app without generating the protocol
 *
 */
tasks.register("copyGenerated", Copy::class.java) {
    dependsOn(":protocols:run")
    from("../protocols/build/generated")
    into("build/generated")
}

tasks.compileKotlin {
    dependsOn("copyGenerated")
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/sessionkotlin/main/kotlin"),
    )
}
