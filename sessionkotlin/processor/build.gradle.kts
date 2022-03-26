group = "org.david"
version = "0.0.1"

plugins {
    kotlin("jvm")
    `maven-publish`
}

// Versions are declared in 'gradle.properties' file
val kspVersion: String by project
val kotlinPoetVersion: String by project
val kotlinPoetInteropKSPVersion: String by project

dependencies {
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetInteropKSPVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation(project(":lib"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin.sourceSets.all {
    languageSettings.optIn("com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview")
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
                description.set("Multiparty Session Types in Kotlin")
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
