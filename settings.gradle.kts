// settings.gradle.kts (루트)
rootProject.name = "rev-multimodule"
include(":server-backend", ":android-app")

pluginManagement {
    repositories {
        google()            // <- 반드시 있어야 com.android.* 플러그인 찾음
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.5.2"
        id("com.android.library") version "8.5.2"
        kotlin("android") version "2.0.0"
        kotlin("kapt") version "2.0.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}