import java.io.FileInputStream
import java.util.*

group = "com.github.d-costa"
version = "0.0.2"

if (JavaVersion.current() != JavaVersion.VERSION_11) {
    throw GradleException("This project requires Java 11, but it's running on ${JavaVersion.current()}")
}

plugins {
    kotlin("jvm") apply false
    `java-library`
    id("org.jlleitschuh.gradle.ktlint") // Linter
    id("org.jetbrains.dokka") // Documentation
    `maven-publish`
    jacoco
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Linter

    repositories {
        mavenCentral()
        maven {
            val githubPackagesRepo: String by project

            // Load GitHub credentials
            val props = Properties()
            val envFile = File(rootDir.path + "/.env")
            if (envFile.exists()) {
                props.load(FileInputStream(envFile))
            }
            name = githubPackagesRepo
            url = uri("https://maven.pkg.github.com/d-costa/sessionkotlin")
            credentials {
                username = props.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = props.getProperty("TOKEN") ?: System.getenv("TOKEN")
            }
        }
    }

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        disabledRules.set(setOf("no-wildcard-imports"))
    }
}

// Gather code coverage from multiple subprojects
// https://docs.gradle.org/6.5.1/samples/sample_jvm_multi_project_with_code_coverage.html
tasks.register<JacocoReport>("codeCoverageReport") {
    // If a subproject applies the 'jacoco' plugin, add the result to the report
    subprojects {
        val subproject = this
        subproject.plugins.withType<JacocoPlugin>().configureEach {
            subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.configureEach {
                val testTask = this
                sourceSets(subproject.sourceSets.main.get())
                executionData(testTask)
            }

            // To automatically run `test` every time `./gradlew codeCoverageReport` is called
            subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.forEach {
                rootProject.tasks["codeCoverageReport"].dependsOn(it)
            }
        }
    }

    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}
