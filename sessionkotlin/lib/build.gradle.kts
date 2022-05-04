import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

val kotlinPoetVersion: String by project
val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project

dependencies {
    project(":parser")
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
            artifactId = rootProject.name
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
