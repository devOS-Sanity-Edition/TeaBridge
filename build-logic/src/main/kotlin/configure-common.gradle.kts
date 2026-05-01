plugins {
    `java-library`
    `maven-publish`
}

val libs: VersionCatalog = versionCatalogs.named("libs")
fun versionOf(name: String): String {
    return libs.findVersion(name).get().toString()
}

group = "one.devos.nautical.${parent!!.name}"

val buildNum = providers.environmentVariable("GITHUB_RUN_NUMBER")
    .filter(String::isNotEmpty)
    .map { "build.$it" }
    .orElse("local")
    .get()

val minecraftVersion = versionOf("minecraft")
// x.y.z+build.100-mcX.Y.Z
version = "2.0.0+$buildNum-mc$minecraftVersion"

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<Jar> {
    // copy the license file into every built jar
    from(rootProject.file("LICENSE"))
}

tasks.processResources {
	inputs.properties(
		"version" to version,
        "fabric_loader_version" to versionOf("fabric-loader"),
        "fabric_api_version" to versionOf("fabric-api"),
        "neoforge_version" to versionOf("neoforge"),
        "minecraft_version" to minecraftVersion
	)

	filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
		expand(inputs.properties)
	}
}

if (projectDir.name == "common") {
    configurations.consumable("commonJava")
    configurations.consumable("commonResources")

    artifacts {
        add("commonJava", sourceSets.main.get().java.sourceDirectories.singleFile)
        add("commonResources", sourceSets.main.get().resources.sourceDirectories.singleFile)
    }
}
