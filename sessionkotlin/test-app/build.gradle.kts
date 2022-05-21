plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.d-costa.sessionkotlin.plugin")
}

val kotlinxCoroutinesVersion: String by project

dependencies {
    implementation(project(":test-protocol"))
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("copyGenerated", Copy::class.java) {
    dependsOn(":test-protocol:run")
    from("../test-protocol/build/generated")
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
