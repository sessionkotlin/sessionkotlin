pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "demo"
include("protocols")
include("app-fluent")
include("app-callbacks")
