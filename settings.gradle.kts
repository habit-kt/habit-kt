pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = "habit-kt"

include(
    "frontend",
    "backend",
    "model",
    "business-logic",
    "data-access"
)