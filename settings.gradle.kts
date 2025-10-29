// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // 또는 FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}
rootProject.name = "re-v-backend"
