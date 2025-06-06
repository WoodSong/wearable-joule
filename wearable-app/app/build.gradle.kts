@Suppress("DSL_SCOPE_VIOLATION") // Required for libs access in plugins block
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.wearableaichat"
    compileSdk = 35 // Target API 33 or higher for Wear OS

    defaultConfig {
        applicationId = "com.example.wearableaichat"
        minSdk = 30 // Wear OS 3 requires API 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui.wear)
    implementation(libs.androidx.compose.ui.text.wear)
    implementation(libs.androidx.compose.material.wear.general)
    implementation(libs.androidx.compose.ui.tooling.preview.wear)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Wear Compose specific
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)

    // For icons if needed (e.g. microphone)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Other Wear OS libraries
    implementation(libs.google.play.services.wearable)

    // Retrofit & Networking
    implementation(libs.squareup.retrofit.core)
    implementation(libs.squareup.retrofit.converter.gson) // Version will be updated by catalog
    implementation(libs.squareup.okhttp.logging.interceptor)
    implementation(libs.jetbrains.kotlinx.coroutines.android)

    // Needed for Horologist components if you use them, e.g. Media UI
    // implementation("com.google.android.horologist:horologist-compose-tools:0.5.14")
    // implementation("com.google.android.horologist:horologist-tiles:0.5.14")
    // implementation("com.google.android.horologist:horologist-compose-layout:0.5.14")

    // Test implementations
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso.core)
    androidTestImplementation(libs.test.androidx.compose.ui.junit4.wear)
    androidTestImplementation(libs.test.androidx.espresso.intents)
    debugImplementation(libs.debug.androidx.compose.ui.tooling.wear)
    debugImplementation(libs.debug.androidx.compose.ui.test.manifest.wear)
}
