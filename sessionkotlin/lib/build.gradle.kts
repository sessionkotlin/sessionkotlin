group = "org.david"
version = "0.0.1"

plugins {
    kotlin("jvm")
    `java-library`
    jacoco
    id("org.jetbrains.dokka") version "1.6.10"
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(true)
    }
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

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    disabledRules.set(setOf("no-wildcard-imports"))
//    reporters {
//        reporter(ReporterType.CHECKSTYLE)
//        reporter(ReporterType.JSON)
//    }
}

java {
    withJavadocJar()
}

publishing {
    publications {

        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = "${rootProject.name}_${project.name}"
            version = project.version as String

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
}
