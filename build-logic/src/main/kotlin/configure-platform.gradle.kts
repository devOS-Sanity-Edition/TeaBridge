plugins {
    id("configure-common")
}

// configurations used to include common code in built artifacts
val commonJava: Configuration by configurations.dependencyScope("commonJava")
val commonResources: Configuration by configurations.dependencyScope("commonResources")

val compileOnly: Configuration = configurations.getByName("compileOnly")

dependencies {
    compileOnly(project(":common"))
    commonJava(project(path = ":common", configuration = "commonJava"))
    commonResources(project(path = ":common", configuration = "commonResources"))
}

// include common stuff in assembly tasks

val resolvableCommonJava: Configuration by configurations.resolvable("resolvableCommonJava") {
    extendsFrom(commonJava)
}
val resolvableCommonResources: Configuration by configurations.resolvable("resolvableCommonResources") {
    extendsFrom(commonResources)
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(resolvableCommonJava)
    source(resolvableCommonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(resolvableCommonResources)
    from(resolvableCommonResources)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(resolvableCommonJava)
    from(resolvableCommonJava)
    dependsOn(resolvableCommonResources)
    from(resolvableCommonResources)
}
