plugins {
    alias(libs.plugins.loom)
    alias(libs.plugins.configure.common)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    compileOnly(libs.bundles.mixin)

    compileOnlyApi(libs.jda) {
        exclude(group = "org.slf4j")
    }
}
