plugins {
    kotlin("jvm")
    `java-library`
    jacoco // Code coverage`
    id("org.jetbrains.dokka") //  Documentation
    `maven-publish`
}

val betterParseVersion: String by project

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            artifactId = project.name
            version = rootProject.version as String

            from(components["java"])

            pom {
                name.set("SessionKotlin Parser")
                description.set("Parser library for SessionKotlin.")
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
        val githubPackagesRepo: String by project
        add(project.repositories.getByName(githubPackagesRepo))
    }
}

tasks.dokkaHtml {
    moduleName.set(project.name)
    dokkaSourceSets {
        configureEach {
            pluginsMapConfiguration.set(
                mapOf("org.jetbrains.dokka.base.DokkaBase" to """{ "separateInheritedMembers": true}""")
            )
        }
    }
}
