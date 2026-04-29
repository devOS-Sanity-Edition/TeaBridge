enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/" )
        maven("https://mvn.devos.one/releases/")
        maven("https://mvn.devos.one/snapshots/")
    }
}

rootProject.name = "teabridge"

includeBuild("build-logic")
include("common", "fabric", "neoforge")
