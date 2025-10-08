import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // 필요하면 여기에 추가 repo
        // maven("https://repo.spring.io/milestone")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 프로젝트에서 repositories 금지
    repositories {
        mavenCentral()
        // 필요하면 여기에 추가 repo
        // maven("https://repo.spring.io/milestone")
    }
}

rootProject.name = "re-v-backend"
