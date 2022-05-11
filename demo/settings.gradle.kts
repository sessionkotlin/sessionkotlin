pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "demo"
include("protocols")
include("app_fluent")
include("app_callbacks")
