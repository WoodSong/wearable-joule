plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.phone_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.phone_app"
        minSdk = 26 // Updated
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true // Enabled Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // As requested
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3") // Added
    implementation("androidx.activity:activity-compose:1.9.0") // Added

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00")) // BOM for Compose
    implementation("androidx.compose.ui:ui") // Added
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview") // Added
    implementation("androidx.compose.material3:material3:1.2.1") // Added

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Added
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Added
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Added
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Added

    // Test Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
