package com.github.d_costa.sessionkotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip

abstract class SessionKotlinPluginExtension {
    /**
     * If set, the 'cleanDownloadedDependencies' task will execute before 'copyDependencies'.
     *
     * Default: false
     */
    var cleanBeforeCopying: Boolean = false
}

class SessionKotlinPlugin : Plugin<Project> {
    private val extensionName = "sessionkotlin"

    override fun apply(project: Project) {

        val extension = project.extensions.create(extensionName, SessionKotlinPluginExtension::class.java)

        val javaSMTVersion = "3.12.0"
        val javaSMTZ3Version = "4.8.15"
        val configName = "javaSMTConfig"
        val dependenciesFolder = "${project.buildDir}/dependencies"

        // Some dependencies from 'org.sosy-lab' do not have pom files
        project.repositories.mavenCentral().metadataSources { it.artifact() }
        project.repositories.mavenCentral().metadataSources { it.mavenPom() }

        project.dependencies.add("implementation", "org.sosy-lab:java-smt:$javaSMTVersion")
        project.dependencies.add("runtimeOnly", "org.sosy-lab:javasmt-solver-z3:$javaSMTZ3Version:com.microsoft.z3@jar")
        project.dependencies.add("runtimeOnly", "org.sosy-lab:javasmt-solver-z3:$javaSMTZ3Version:libz3@so")
        project.dependencies.add("runtimeOnly", "org.sosy-lab:javasmt-solver-z3:$javaSMTZ3Version:libz3java@so")

        // Register new configuration
        project.configurations.register(configName) { conf ->
            conf.dependencies.addAll(project.configurations.getByName("implementation").dependencies.filter { it.group == "org.sosy-lab" })
            conf.dependencies.addAll(project.configurations.getByName("runtimeOnly").dependencies.filter { it.group == "org.sosy-lab" })
        }

        project.dependencies.add("implementation", project.fileTree(dependenciesFolder) { it.include("*.jar") })

        // Copy and rename all JavaSMT dependencies
        project.tasks.register("copyDependencies", Copy::class.java) {
            it.description = "Copy and rename JavaSMT dependencies (SessionKotlin)"
            if (extension.cleanBeforeCopying) {
                it.dependsOn("cleanDownloadedDependencies")
            }
            it.from(project.configurations.getByName("javaSMTConfig"))
            it.into(dependenciesFolder)
            it.rename(".*(lib[^-]*)-?.*.so", "\$1.so")
        }

        // Clean the dependencies folder
        project.tasks.register("cleanDownloadedDependencies", Delete::class.java) {
            it.description = "Clean the dependencies folder (SessionKotlin)"
            it.delete(project.file(dependenciesFolder))
        }

        // Copy the JavaSMT dependencies before using them.
        project.tasks.getByName("compileKotlin") {
            it.dependsOn("copyDependencies")
        }

        // When clean is called we want to delete our copied JavaSMT files
        project.tasks.getByName("clean") {
            it.dependsOn("cleanDownloadedDependencies")
        }

        project.tasks.withType(Tar::class.java) {
            it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        project.tasks.withType(Zip::class.java) {
            it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
}
