plugins {
    kotlin("jvm")
    `java-library`
}

val kotlinPoetVersion: String by project
val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project
val betterParseVersion: String by project

dependencies {
    api(project(":parser"))
    testImplementation(kotlin("test"))
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}

java {
    withJavadocJar()
    withSourcesJar()
}
