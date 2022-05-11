group = rootProject.group
version = rootProject.version

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    val plugin by plugins.creating {
        id = "com.github.d-costa.sessionkotlin.plugin"
        displayName = "SessionKotlin Plugin"
        description = "Auxiliary plugin for sessionkotlin"
        implementationClass = "com.github.d_costa.sessionkotlin.CopyZ3"
    }

    publishing {
        repositories {
            val githubPackagesRepo: String by project
            add(project.repositories.getByName(githubPackagesRepo))
        }
    }
}
