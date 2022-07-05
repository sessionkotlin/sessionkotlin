plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.d-costa.sessionkotlin.plugin")
    id("me.champeau.jmh") version "0.6.6"

}

val kotlinxCoroutinesVersion: String by project
val scribbleVersion: String by project

dependencies {
    implementation(project(":protocols"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.scribble:scribble-core:$scribbleVersion")
    implementation("org.scribble:scribble-runtime:$scribbleVersion")
    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}

jmh {
    warmupIterations.set(1)
    iterations.set(3)
    fork.set(1)
    benchmarkMode.set(listOf("Throughput"))
    failOnError.set(true)
}
