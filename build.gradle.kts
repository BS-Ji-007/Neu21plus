/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.xpdustry.ksr.kotlinRelocate
import net.fabricmc.loom.task.RemapJarTask
import neubs.DownloadBackupRepo
import neubs.NEUBuildFlags
import neubs.applyPublishingInformation
import neubs.setVersionFromEnvironment
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    idea
    java
    id("net.fabricmc.fabric-loom") version "1.15.2"
    id("com.gradleup.shadow") version "9.4.1"
    id("io.github.juuxel.loom-quiltflower") version "1.11.0"
    `maven-publish`
    kotlin("jvm") version libs.versions.kotlin.get()
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
    id("net.kyori.blossom") version "2.1.0"
    id("com.xpdustry.ksr") version "1.0.0"
}

apply<NEUBuildFlags>()

group = "io.github.moulberry"
val baseVersion = setVersionFromEnvironment()

loom {
    // 26.1+ is unobfuscated.
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.notenoughupdates.org/releases")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://jitpack.io")
    maven("https://repo.nea.moe/releases")
}

val shadowImplementation: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val shadowOnly: Configuration by configurations.creating {
}

val shadowApi: Configuration by configurations.creating {
    configurations.api.get().extendsFrom(this)
}

val kotlinDependencies: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft(libs.minecraft)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:${libs.versions.kotlin.get()}"))
    kotlinDependencies(kotlin("stdlib"))
    kotlinDependencies(kotlin("reflect"))

    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")

    compileOnly(ksp(project(":annotations"))!!)
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    shadowImplementation("com.mojang:brigadier:1.2.9")
    shadowImplementation("moe.nea:libautoupdate:1.3.1")
    shadowImplementation(libs.nealisp) {
        exclude("org.jetbrains.kotlin")
    }

    compileOnly("org.jetbrains:annotations:24.1.0")

    modImplementation(libs.moulconfig)
    shadowOnly(libs.moulconfig)

    @Suppress("VulnerableLibrariesLocal")
    shadowApi("info.bliki.wiki:bliki-core:3.1.0")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    archiveBaseName.set("NotEnoughUpdates")
    manifest.attributes.run {
        this["Main-Class"] = "NotSkyblockAddonsInstallerFrame"
    }
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("shadow")
    configurations = listOf(shadowImplementation, shadowApi, shadowOnly)
    exclude("**/module-info.class", "LICENSE.txt")
    
    fun relocate(name: String) = kotlinRelocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
    relocate("com.mojang.brigadier")
    relocate("io.github.notenoughupdates.moulconfig")
    relocate("moe.nea.libautoupdate")
    relocate("moe.nea.lisp")
    mergeServiceFiles()
}

val remapJar = tasks.named<RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    inputFile.set(shadowJar.flatMap { it.archiveFile })
}

val sourcesJar = tasks.named<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
}

tasks.assemble {
    dependsOn(remapJar)
}

tasks.processResources {
    val backupRepo = tasks.named<DownloadBackupRepo>("includeBackupRepo")
    from(backupRepo)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version, "mcversion" to libs.versions.minecraft.get())
    }
}

tasks.register<DownloadBackupRepo>("includeBackupRepo") {
    this.branch.set("master")
    this.outputDirectory.set(layout.buildDirectory.dir("downloadedRepo"))
}

tasks.register("signRelease", neubs.CustomSignTask::class)

applyPublishingInformation(
    "deobf" to tasks.jar,
    "all" to remapJar,
    "sources" to sourcesJar,
)
