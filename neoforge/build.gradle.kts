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
    "additionalRuntimeClasspath"(jarJar("net.sf.trove4j:core:3.1.0")!!)
    "additionalRuntimeClasspath"(jarJar("org.apache.commons:commons-collections4:4.4")!!)
    "additionalRuntimeClasspath"(jarJar("com.fasterxml.jackson.core:jackson-core:2.17.0")!!)
    "additionalRuntimeClasspath"(jarJar("com.fasterxml.jackson.core:jackson-databind:2.17.0")!!)
    "additionalRuntimeClasspath"(jarJar("com.fasterxml.jackson.core:jackson-annotations:2.17.0")!!)
    "additionalRuntimeClasspath"(jarJar("com.squareup.okhttp3:okhttp:4.12.0")!!)
    "additionalRuntimeClasspath"(jarJar("com.squareup.okio:okio-jvm:3.4.0")!!)
    "additionalRuntimeClasspath"(jarJar("com.neovisionaries:nv-websocket-client:2.14")!!)
}
