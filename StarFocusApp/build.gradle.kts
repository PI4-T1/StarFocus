// Top-level build file where you can add configuration options common to all sub-projects/modules.
// build.gradle.kts no nível de projeto

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {

    dependencies {
        // Adicione classpath fora do bloco plugins
        classpath("com.android.tools.build:gradle:8.7.2")
        classpath("com.google.gms:google-services:4.4.2")
    }
}
