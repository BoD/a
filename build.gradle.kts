plugins {
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.hilt).apply(false)
    alias(libs.plugins.ksp).apply(false)
}

// See https://developer.android.com/build/releases/agp-9-0-0-release-notes#runtime-dependency-on-kotlin-gradle-plugin
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}
