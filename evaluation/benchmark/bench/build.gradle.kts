plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.d-costa.sessionkotlin.plugin")
    id("me.champeau.jmh") version "0.6.6"
}

val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project

dependencies {
    implementation(project(":protocols"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-network:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}

jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(3)
    jmhTimeout.set("60s")
    benchmarkMode.set(listOf("Throughput"))
}
