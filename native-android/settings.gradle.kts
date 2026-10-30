pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    plugins {
        id("com.android.application") version "8.9.1"
        kotlin("android") version "2.1.20"
        kotlin("jvm") version "2.1.20"
        id("com.google.devtools.ksp") version "2.1.20-1.0.32"
        kotlin("plugin.serialization") version "2.1.20"
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.20"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "ThreeBallScorekeeper"
if (providers.gradleProperty("engineOnly").isPresent) {
    rootProject.buildFileName = "build-engine.gradle.kts"
}
include(":engine", ":records")
// Allows rules tests on machines without the Android SDK or Google artifacts.
if (!providers.gradleProperty("engineOnly").isPresent) include(":app")
