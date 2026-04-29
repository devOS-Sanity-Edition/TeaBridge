plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.configure.platform)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.bundles.fabric)

    runtimeOnly(libs.jda) {
        exclude(module = "opus-java")
    }
}

loom {
    runs {
        named("server") {
            configName = "Fabric Server"
            ideConfigGenerated(true)
            appendProjectPathToConfigName = false

            property("mixin.debug.export", "true")
        }
    }
}
