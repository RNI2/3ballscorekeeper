// One parent classloader for the Android and JVM Kotlin plugins.
plugins {
    id("com.android.application") apply false
    kotlin("android") apply false
    kotlin("jvm") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
}
