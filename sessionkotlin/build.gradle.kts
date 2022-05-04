group = "org.david"
version = "0.0.1"

plugins {
    kotlin("jvm") apply false
    `java-library`
    id("org.jlleitschuh.gradle.ktlint") // Linter
    id("org.jetbrains.dokka") version "1.6.10" // Documentation
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Linter
    apply(plugin = "jacoco") // Code coverage
    apply(plugin = "org.jetbrains.dokka") //  Documentation

    repositories {
        mavenCentral()
    }

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        disabledRules.set(setOf("no-wildcard-imports"))
    }

    tasks.dokkaHtml {
        moduleName.set(rootProject.name)
        dokkaSourceSets {
            configureEach {
                pluginsMapConfiguration.set(
                    mapOf("org.jetbrains.dokka.base.DokkaBase" to """{ "separateInheritedMembers": true}""")
                )
            }
        }
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
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude("org/david/sessionkotlin/api")
                }
            }
        )
    )

    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}
