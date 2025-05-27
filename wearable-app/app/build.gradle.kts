plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
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
        kotlinCompilerExtensionVersion = "1.5.14" // Ensure compatibility with your Kotlin version
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.compose.ui:ui:1.8.2")
    implementation("androidx.compose.ui:ui-text:1.8.2")
    implementation("androidx.compose.material:material:1.8.2") // General Material Design components
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Wear Compose specific
    implementation("androidx.wear.compose:compose-material:1.4.1")
    implementation("androidx.wear.compose:compose-foundation:1.4.1")

    // For icons if needed (e.g. microphone)
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")


    // Other Wear OS libraries
    implementation("com.google.android.gms:play-services-wearable:19.0.0")

    // Retrofit & Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // Needed for Horologist components if you use them, e.g. Media UI
    // implementation("com.google.android.horologist:horologist-compose-tools:0.5.14")
    // implementation("com.google.android.horologist:horologist-tiles:0.5.14")
    // implementation("com.google.android.horologist:horologist-compose-layout:0.5.14")


    // Test implementations
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.2")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1") // Added for Intent testing
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.2")
}
