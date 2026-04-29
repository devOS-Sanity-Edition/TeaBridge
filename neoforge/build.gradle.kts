plugins {
    alias(libs.plugins.mdg)
    alias(libs.plugins.configure.platform)
}

neoForge {
    version = libs.versions.neoforge.get()

    runs {
        create("server") {
            ideName = "NeoForge Server"
            server()
            programArgument("--nogui")

            jvmArgument("-Dmixin.debug.export=true")
        }
    }

    mods {
        create("teabridge") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    "additionalRuntimeClasspath"(jarJar("net.dv8tion:JDA:6.4.1") {
        exclude(module = "opus-java")
        exclude(group = "org.slf4j")
    })
}
