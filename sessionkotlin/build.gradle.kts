import java.io.FileInputStream
import java.util.*

group = "org.david"
version = "0.0.1"

plugins {
    kotlin("jvm") apply false
    `java-library`
    id("org.jlleitschuh.gradle.ktlint") // Linter
    id("org.jetbrains.dokka") version "1.6.10" // Documentation
    `maven-publish`
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

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")

    publishing {
        publications {

            create<MavenPublication>("maven") {
                groupId = rootProject.group as String
                artifactId = "${rootProject.name}-${project.name }"
                version = rootProject.version as String

                from(components["java"])

                pom {
                    name.set("SessionKotlin")
                    description.set("Multiparty Session Types in Kotlin ")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                // Load GitHub credentials
                val props = Properties()
                val envFile = File(rootDir.path + "/.env")
                if (envFile.exists()) {
                    props.load(FileInputStream(envFile))
                }
                name = "SessionKotlin-GithubPackages"
                url = uri("https://maven.pkg.github.com/d-costa/sessionkotlin")
                credentials {
                    username = props.getProperty("USERNAME") ?: System.getenv("USERNAME")
                    password = props.getProperty("TOKEN") ?: System.getenv("TOKEN")
                }
            }
        }
    }
}
