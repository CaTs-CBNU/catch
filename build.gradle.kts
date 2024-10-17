// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // Add OSS licenses plugin here if necessary
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.4") // Example version
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}
