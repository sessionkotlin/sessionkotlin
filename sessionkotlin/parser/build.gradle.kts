plugins {
    kotlin("jvm")
    `java-library`
}

val betterParseVersion: String by project

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.h0tk3y.betterParse:better-parse:$betterParseVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    explicitApi()
}
