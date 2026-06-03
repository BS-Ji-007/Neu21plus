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

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    // Manually specify versions since Version Catalog might be tricky in subprojects without proper setup
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.21-1.0.30")
    implementation("com.squareup:kotlinpoet:1.17.0")
    implementation("com.squareup:kotlinpoet-ksp:1.17.0")
}
