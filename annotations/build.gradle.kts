/*
 * Copyright (C) 2023-2026 NotEnoughUpdates contributors
 */

plugins {
    kotlin("jvm")
    java
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.release.set(25)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:${rootProject.libs.versions.ksp.get()}")
    implementation("com.squareup:kotlinpoet:1.17.0")
    implementation("com.squareup:kotlinpoet-ksp:1.17.0")
}
