plugins {
    kotlin("jvm") version "1.6.20" apply false
    id("com.github.d-costa.sessionkotlin.plugin") version "0.1.1" apply false
}

subprojects{
    repositories {
        mavenLocal()
        mavenCentral()
    }
}