rootProject.name = "teabridge"

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

includeBuild("build-logic")

for (platform in listOf("common", "fabric", "neoforge")) {
    val name = "${rootProject.name}-$platform"
    include(name)
    project(":$name").projectDir = file(platform)
}
