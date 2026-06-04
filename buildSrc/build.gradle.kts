/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 */

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("gradle-plugin", version = "2.2.21"))
}
