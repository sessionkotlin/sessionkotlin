import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0" apply false
    id("com.github.d-costa.sessionkotlin.plugin") version "0.1.6" apply false
}

subprojects{
    repositories {
        mavenCentral()
        mavenLocal()
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
