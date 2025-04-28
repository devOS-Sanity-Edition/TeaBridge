// versions
val minecraftVersion = "1.21.5"
val minecraftDep = "=1.21.5"
// https://parchmentmc.org/docs/getting-started
val parchmentVersion = "2025.04.19"
// https://fabricmc.net/develop
val loaderVersion = "0.16.14"
val fapiVersion = "0.121.0+1.21.5"

// dev env mods
val flkVersion = "1.13.2+kotlin.2.1.20"

// buildscript
plugins {
    id("fabric-loom") version "1.10.1"
    id("maven-publish")
}

base.archivesName = "teabridge"
group = "one.devos.nautical"

version = "2.0.0-mc$minecraftVersion"

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://api.modrinth.com/maven")

}

repositories {
    maven("https://maven.parchmentmc.org")
	maven("https://m2.dv8tion.net/releases/") {
		content { includeGroup("net.dv8tion") }
	}
}

dependencies {
    // dev environment
    minecraft("com.mojang:minecraft:$minecraftVersion")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:$parchmentVersion@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiVersion")
	include(implementation("net.dv8tion:JDA:5.4.0") {
        exclude(module = "opus-java")
	})
	include("net.sf.trove4j:core:3.1.0")
	include("org.apache.commons:commons-collections4:4.4")
	include("com.fasterxml.jackson.core:jackson-core:2.17.0")
	include("com.fasterxml.jackson.core:jackson-databind:2.17.0")
	include("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
	include("com.squareup.okhttp3:okhttp:4.12.0")
	include("com.squareup.okio:okio-jvm:3.4.0")
	include("com.neovisionaries:nv-websocket-client:2.14")

	// for "squareup" libs (they all use kotlin)
	modLocalRuntime("net.fabricmc:fabric-language-kotlin:$flkVersion")
}

tasks.withType(ProcessResources::class) {
	inputs.properties(
		"version" to version,
		"loader_version" to loaderVersion,
		"fapi_version" to fapiVersion,
		"minecraft_dependency" to minecraftDep
	)

	filesMatching("fabric.mod.json") {
		expand(inputs.properties)
	}
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "teabridge"
            from(components["java"])
        }
    }

    repositories {
        maven("https://mvn.devos.one/snapshots") {
            name = "devOsSnapshots"
            credentials(PasswordCredentials::class)
        }
        maven("https://mvn.devos.one/releases") {
            name = "devOsReleases"
            credentials(PasswordCredentials::class)
        }
    }
}
