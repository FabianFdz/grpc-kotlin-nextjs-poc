// Root build file. Declares plugin versions once (resolved via the plugin
// management block below) so both service modules stay in sync; actual
// `apply` happens per-module in apps/*/build.gradle.kts.
plugins {
    kotlin("jvm") version "1.9.24" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}
