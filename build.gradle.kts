// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.legacy.kapt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.dagger) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
}

buildscript {
    repositories {
        google()
        maven {
            setUrl("https://jitpack.io")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}