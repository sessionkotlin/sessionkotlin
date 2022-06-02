import java.io.FileInputStream
import java.util.*

group = "com.github.d-costa"
version = "0.1.5"

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
    id("com.github.d-costa.sessionkotlin.plugin") version "0.1.1" apply false
}

allprojects {
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
}

fun File.appendLine(line: String) {
    appendText(line)
    appendText("\n")
}

lateinit var testSummary: File

subprojects {
    afterEvaluate {
        if (tasks.test.isPresent) {
            tasks.test {
                testLogging {
                    lifecycle {
                        showExceptions = true
                        showCauses = true
                        showStackTraces = false
                        showStandardStreams = false
                    }
                    info.exceptionFormat = lifecycle.exceptionFormat
                }

                // See https://github.com/gradle/kotlin-dsl/issues/836
                addTestListener(object : TestListener {
                    override fun beforeSuite(suite: TestDescriptor) {}
                    override fun beforeTest(testDescriptor: TestDescriptor) {}
                    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}

                    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                        if (suite.parent == null) { // root suite
                            val reg = "(?<=:).*(?=:)".toRegex()
                            val module = reg.find(suite.displayName)?.value
                            val success = result.successfulTestCount
                            val failed = result.failedTestCount
                            val skipped = result.skippedTestCount

                            val elapsed = (result.endTime - result.startTime)
                            val sec = (elapsed / 1000) % 60
                            val min = (elapsed / (1000 * 60)) % 60
                            val duration = "${min}min ${sec}s"

                            try {
                                testSummary.appendLine("| $module | ${result.resultType} | $success | $failed | $skipped | $duration |")
                            } catch (e: UninitializedPropertyAccessException) {
                                // skip
                            }
                        }
                    }
                })
            }
        }
    }
}

tasks.register("finalizeTests") {
    project.subprojects.forEach { dependsOn("${it.name}:test") }
}

tasks.test {
    val dir = File(buildDir.path)
    if (!dir.exists()) {
        dir.mkdir()
    }
    testSummary = File(dir, "test_summary.md")
    testSummary.writeText("") // truncate file

    finalizedBy("finalizeTests")
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

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}
