package com.github.d_costa.sessionkotlin

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class SessionKotlinPluginTest {

    @Test
    fun `plugin registers tasks`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()

        // Dummy tasks
        project.tasks.register("compileKotlin")
        project.tasks.register("clean")

        // Dummy configs
        project.configurations.register("runtimeOnly")
        project.configurations.register("implementation")
        project.configurations.register("api")

        project.plugins.apply("com.github.d-costa.sessionkotlin.plugin")

        assertNotNull(project.tasks.findByName("copyDependencies"))
        assertNotNull(project.tasks.findByName("cleanDownloadedDependencies"))
    }
}
