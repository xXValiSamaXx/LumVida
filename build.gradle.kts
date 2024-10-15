// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// En el build.gradle del proyecto (no el del módulo app)
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}