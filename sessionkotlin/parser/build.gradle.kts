plugins {
    kotlin("jvm")
}

val betterParseVersion: String by project

dependencies {
    testImplementation(kotlin("test"))
    api("com.github.h0tk3y.betterParse:better-parse:$betterParseVersion")
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
