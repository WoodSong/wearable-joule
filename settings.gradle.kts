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
        // Repository for Jetpack Compose Compiler
        maven {
            url = uri("https://androidx.dev/storage/compose-compiler/repository/")
        }
    }
}

rootProject.name = "MainProject"

include(":phone-app")
include(":wearable-app:app") // Correctly refers to the app module within wearable-app
// backend-service is not a Gradle module, so it won't be included here.
