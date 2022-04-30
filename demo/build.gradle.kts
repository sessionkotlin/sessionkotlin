import java.util.Properties
import java.io.FileInputStream

subprojects{
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            // Load GitHub credentials
            val props = Properties()
            val envFile = File(rootDir.path + "/.env")
            if (envFile.exists())
                props.load(FileInputStream(envFile))

            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/d-costa/sessionkotlin")
            credentials {
                username = props.getProperty("USERNAME") ?: System.getenv("USERNAME")
                password = props.getProperty("TOKEN") ?: System.getenv("TOKEN")
            }
        }
    }
}