// Top-level settings file
pluginManagement {
    repositories {
        maven {
            url = uri("https://int.repositories.cloud.sap/artifactory/build-snapshots/")
        }
        google()
        mavenCentral()
        gradlePluginPortal()

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://int.repositories.cloud.sap/artifactory/build-snapshots/")
        }
        google()
        mavenCentral()

    }
}

rootProject.name = "WearableAiChat"
include(":app")
