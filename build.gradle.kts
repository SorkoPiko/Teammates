plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
    kotlin("jvm") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.7"
}

class ModData {
    val id = property("mod.id").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
}

val mod = ModData()

version = "${mod.version}+${stonecutter.current.version}"
group = mod.group


base { archivesName.set("${mod.id}-fabric") }

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

repositories {
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }
    maven {
        name = "lunarclient"
        url = uri("https://repo.lunarclient.dev")
    }
    maven {
        url = uri("https://buf.build/gen/maven")
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${stonecutter.current.project}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation("dev.isxander:yet-another-config-lib:${project.property("yacl_version")}")
    modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.protobuf:protobuf-java:4.31.1")
//    implementation("com.lunarclient:apollo-api:1.1.8")
//    implementation("com.lunarclient:apollo-protos:0.0.2")
//    implementation("com.lunarclient:apollo-common:1.1.8")

    include("com.google.code.gson:gson:2.10.1")
    include("com.google.protobuf:protobuf-java:4.31.1")
//    include("com.lunarclient:apollo-api:1.1.8")
//    include("com.lunarclient:apollo-protos:0.0.2")
//    include("com.lunarclient:apollo-common:1.1.8")
}

fletchingTable {
    mixins.register("main") {
        default = "${mod.id}.mixins.json"
    }

    fabric {
        entrypointMappings.put("modmenu", "com.terraformersmc.modmenu.api.ModMenuApi")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    inputs.property("github", project.property("github"))
    inputs.property("yacl_version", project.property("yacl_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "github" to project.property("github"),
            "yacl_version" to project.property("yacl_version")
        )
    }
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${mod.id}" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = mod.id
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
