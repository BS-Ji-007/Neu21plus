/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */


import com.xpdustry.ksr.kotlinRelocate
import neubs.CustomSignTask
import neubs.DownloadBackupRepo
import neubs.NEUBuildFlags
import neubs.applyPublishingInformation
import neubs.setVersionFromEnvironment
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    idea
    java
    id("net.fabricmc.loom") version "1.15.+"
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

// Build metadata

group = "io.github.moulberry"

val baseVersion = setVersionFromEnvironment()

// Minecraft configuration:
loom {
    // 26.1+ is unobfuscated, no mappings needed
    
    launchConfigs {
        getByName("client") {
            property("mixin.debug", "true")
            property("asmhelper.verbose", "true")
        }
    }
    runConfigs {
        getByName("client") {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
            vmArgs.add("-Xmx4G")
        }
        getByName("server") {
            isIdeConfigGenerated = false
        }
    }
}


// Dependencies:
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

val devEnv: Configuration by configurations.creating {
    configurations.runtimeClasspath.get().extendsFrom(this)
    isCanBeResolved = false
    isCanBeConsumed = false
    isVisible = false
}

val kotlinDependencies: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

configurations {
    val main = getByName(sourceSets.main.get().compileClasspathConfigurationName)
}

dependencies {
    minecraft(libs.minecraft)
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    // Please keep this version in sync with KotlinLoadingTweaker
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
    detektPlugins("org.notenoughupdates:detektrules:1.0.0")
    devEnv("me.djtheredstoner:DevAuth-fabric:1.2.1")
}



java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
    options.isFork = true
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    this.javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}
val badJars = layout.buildDirectory.dir("badjars")

tasks.named("jar", Jar::class) {
    archiveClassifier.set("named")
    destinationDirectory.set(badJars)
}

tasks.withType(Jar::class) {
    archiveBaseName.set("NotEnoughUpdates")
    manifest.attributes.run {
        this["Main-Class"] = "NotSkyblockAddonsInstallerFrame"
    }
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.assemble.get().dependsOn(remapJar)

/* Bypassing https://github.com/johnrengelman/shadow/issues/111 */
// Use Zip instead of Jar as to not include META-INF
val kotlinDependencyCollectionJar by tasks.creating(Zip::class) {
    archiveFileName.set("kotlin-libraries-wrapped.jar")
    destinationDirectory.set(project.layout.buildDirectory.dir("wrapperjars"))
    from(kotlinDependencies)
    into("neu-kotlin-libraries-wrapped")
}

val includeBackupRepo by tasks.registering(DownloadBackupRepo::class) {
    this.branch.set("master")
    this.outputDirectory.set(layout.buildDirectory.dir("downloadedRepo"))
}


tasks.shadowJar {
    archiveClassifier.set("dep-dev")
    configurations = listOf(shadowImplementation, shadowApi, shadowOnly)
    destinationDirectory.set(badJars)
    archiveBaseName.set("NotEnoughUpdates")
    exclude("**/module-info.class", "LICENSE.txt")
    dependencies {
        exclude {
            it.moduleGroup.startsWith("org.apache.") || it.moduleName in
                listOf("logback-classic", "commons-logging", "commons-codec", "logback-core")
        }
    }
    from(kotlinDependencyCollectionJar)
    dependsOn(kotlinDependencyCollectionJar)
    fun relocate(name: String) = kotlinRelocate(name, "io.github.moulberry.notenoughupdates.deps.$name")
    relocate("com.mojang.brigadier")
    relocate("io.github.notenoughupdates.moulconfig")
    relocate("moe.nea.libautoupdate")
    relocate("moe.nea.lisp")
    mergeServiceFiles()
}

tasks.processResources {
    from(tasks["generateBuildFlags"])
    from(includeBackupRepo)
    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml")) {
        expand(
            "version" to project.version, "mcversion" to libs.versions.minecraft.get()
        )
    }
}

idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs =
            generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/java/main"))
    this.blossom {
        this.javaSources {
            this.property("neuVersion", baseVersion)
        }
    }
}

tasks.register("signRelease", CustomSignTask::class)

applyPublishingInformation(
    "deobf" to tasks.jar,
    "all" to tasks.remapJar,
    "sources" to tasks["sourcesJar"],
)
