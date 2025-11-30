pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7-beta.6"
}

stonecutter {
    create(rootProject) {
        versions("1.21", "1.21.2", "1.21.4", "1.21.5", "1.21.6", "1.21.9")
    }
}