plugins {
    kotlin("jvm")
    `java-library`
}

val kotlinxCoroutinesVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
}
