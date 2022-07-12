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

/**
 * Note:
 *
 * Comment everything below this if you want to run the client without (re)generating the protocol
 *
 */
tasks.register("copyGenerated", Copy::class.java) {
    dependsOn(":protocol:run")
    from("../protocol/build/generated")
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
