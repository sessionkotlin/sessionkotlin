plugins {
    kotlin("jvm")
}

val kotlinPoetVersion: String by project
val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by project
val betterParseVersion: String by project
val javaSMTVersion: String by project
val javaSMTZ3Version: String by project

repositories {
    mavenCentral {
        metadataSources {
            mavenPom()
        }
    }
    // Some dependencies from 'org.sosy-lab' do not have pom files
    mavenCentral {
        metadataSources {
            artifact()
        }
    }
}

val dependenciesFolder = "$buildDir/dependencies"

dependencies {
    api(project(":parser"))
    testImplementation(kotlin("test"))
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")
    implementation("org.sosy-lab:java-smt:$javaSMTVersion")
    runtimeOnly("org.sosy-lab:javasmt-solver-z3:$javaSMTZ3Version:com.microsoft.z3@jar")
    runtimeOnly("org.sosy-lab:javasmt-solver-z3:$javaSMTZ3Version:libz3@so")
    runtimeOnly("org.sosy-lab:javasmt-solver-z3:$javaSMTZ3Version:libz3java@so")

    implementation(fileTree("dir" to dependenciesFolder, "include" to "*.jar"))
}

configurations {
    register("javaSMTConfig").configure {
        dependencies.addAll(runtimeOnly.get().dependencies.filter { it.group == "org.sosy-lab" })
        dependencies.addAll(implementation.get().dependencies.filter { it.group == "org.sosy-lab" })
    }
}

// Copy and rename all JavaSMT dependencies
// This is necessary as Gradle renames the JavaSMT dependencies, but we need them to have certain names,
// and they need to be in a specific location for JavaSMT to be found (the easiest ist the same folder)
// For more information about this look up the sosy-commons loading process
tasks.register<Copy>("copyDependencies") {
    dependsOn("cleanDownloadedDependencies")
    from(configurations["javaSMTConfig"])
    into(dependenciesFolder)
    rename(".*(lib[^-]*)-?.*.so", "\$1.so")
}

// Cleans the dependencies folder
tasks.register<Delete>("cleanDownloadedDependencies") {
    delete(file(dependenciesFolder))
}

// Copy the JavaSMT dependencies before using them.
tasks.compileKotlin {
    dependsOn("copyDependencies")
}

// When clean is called we want to delete our copied JavaSMT files
tasks.clean {
    dependsOn("cleanDownloadedDependencies")
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
