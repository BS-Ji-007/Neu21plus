/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://maven.xpdustry.com/releases") {
            name = "xpdustry-releases"
            mavenContent { releasesOnly() }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.notenoughupdates.org/releases")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
        maven("https://jitpack.io")
        maven("https://repo.nea.moe/releases")
    }
}

include("annotations")
rootProject.name = "NotEnoughUpdates"
