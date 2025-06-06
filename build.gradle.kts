// Top-level build file where you can add configuration options common to all sub-projects/modules.
// This is a KTS (Kotlin Script) build file.

plugins {
    // Apply the Android Application plugin to this project so it can be used by sub-projects
    // The version is typically managed here for consistency.
    id("com.android.application") version "8.2.2" apply false // Using a common recent version
    // Apply the Kotlin Android plugin
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Using a common recent Kotlin version
}

// It's also good practice to define common configurations here,
// but for fixing the plugin issue, the plugins block is key.

// tasks.wrapper {
//     gradleVersion = "8.4" // Example: Specify Gradle wrapper version
//     distributionType = Wrapper.DistributionType.ALL
// }
