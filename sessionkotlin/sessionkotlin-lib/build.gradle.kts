plugins {
    kotlin("jvm")
    `java-library`
    jacoco // Code coverage`
    id("org.jetbrains.dokka") //  Documentation
    `maven-publish`
    id("com.github.d-costa.sessionkotlin.plugin") version "0.0.3"
}

val kotlinPoetVersion: String by project
val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project
val betterParseVersion: String by project

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val dependenciesFolder = "$buildDir/dependencies"

dependencies {
    api(project(":sessionkotlin-parser"))
    testImplementation(kotlin("test"))
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")
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
