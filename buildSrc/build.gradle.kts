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
    // Removed explicit kotlin gradle-plugin dependency to avoid putting the Kotlin
    // plugin on the buildSrc classpath which makes the plugin unavailable for
    // compatibility checks when applied in the root build. The Kotlin plugin
    // should be resolved via the plugins block in the root build.gradle.kts.
}
