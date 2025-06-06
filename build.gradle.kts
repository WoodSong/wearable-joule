// Top-level build file where you can add configuration options common to all sub-projects/modules.
// This is a KTS (Kotlin Script) build file.

@Suppress("DSL_SCOPE_VIOLATION") // Required for libs access in plugins block
plugins {
    // Apply the Android Application plugin to this project so it can be used by sub-projects
    // The version is now managed by the version catalog.
    alias(libs.plugins.android.application) apply false
    // Apply the Kotlin Android plugin
    alias(libs.plugins.kotlin.android) apply false
}

// It's also good practice to define common configurations here.

// tasks.wrapper {
//     gradleVersion = "8.4" // Example: Specify Gradle wrapper version
//     distributionType = Wrapper.DistributionType.ALL
// }
