pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
            // Load GitHub credentials
            val props = java.util.Properties()
            val envFile = File(rootDir.path + "/.env")
            if (envFile.exists())
                props.load(java.io.FileInputStream(envFile))

            name = "SessionKotlin-GithubPackages"
            url = uri("https://maven.pkg.github.com/d-costa/sessionkotlin")
            credentials {
                username = props.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = props.getProperty("TOKEN") ?: System.getenv("TOKEN")
            }
        }
    }
}

rootProject.name = "smtp"
include("protocol")
include("client")
