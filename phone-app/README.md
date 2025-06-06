# Phone AI Chat Application

## Overview

The Phone AI Chat application is an Android app designed to provide a mobile interface for interacting with a backend AI chat service. Users can send text messages to the AI and receive responses. The app also supports basic integration with Google Assistant, allowing users to send queries to the app using voice commands that resolve to text.

## Features

-   **Text-Based Chat Interface:** A Jetpack Compose-based UI for displaying chat messages and user input.
-   **Backend Communication:** Connects to a Python Flask backend service (`backend-service`) to process messages and get AI responses.
-   **Google Assistant Integration:** Handles `ACTION_SEND` intents with `text/plain` data, allowing Google Assistant (or other apps) to share text directly into the app as a new message.
-   **Loading Indicators:** Shows a visual cue when the app is waiting for a response from the backend.
-   **Error Display:** Displays network errors or server issues as messages in the chat and via Toast notifications.
-   **Auto-Scrolling Chat:** The chat list automatically scrolls to the latest message.

## Requirements

-   **Android Studio:** Version Hedgehog (2023.1.1) or newer recommended.
-   **Android Emulator or Physical Device:** API Level 26 (Android 8.0 Oreo) or higher.
-   **Running `backend-service`:** The companion `backend-service` (located in the root project's `backend-service` directory) must be running for the chat functionality to work.

## Setup & Build

1.  **Open the Project:**
    *   Open the entire root project (which includes `phone-app`, `wearable-app`, and `backend-service`) in Android Studio.
    *   Android Studio should recognize `phone-app` as an Android application module.
2.  **Build the Application:**
    *   Select the `phone-app` configuration from the "Edit Run/Debug Configurations" dropdown (it's usually named 'app' or 'phone-app.app').
    *   Click the "Run" button (green play icon) or use the menu `Build > Make Project` followed by Run.
    *   Alternatively, use Gradle: `./gradlew :phone-app:assembleDebug` from the root project directory.

## Running the Application

1.  **Select Target:** Choose an available Android Emulator (API 26+) or a connected physical device.
2.  **Run Configuration:** Ensure the `phone-app` run configuration is selected.
3.  **Start the App:** Click the "Run" button.

### Crucial Backend Dependency

For the app to function correctly, the `backend-service` must be running on your local machine.

**`BASE_URL` Configuration:**

The application connects to the backend via HTTP. The base URL is defined in:
`phone-app/app/src/main/java/com/example/phone_app/network/ApiService.kt`

-   **For Android Emulator (Default):**
    The `BASE_URL` is set to `http://10.0.2.2:5000/`. This is a special alias that allows the Android emulator to access the `localhost` of the host machine (your computer).

-   **For Physical Devices:**
    1.  Ensure your Android device and the computer running the `backend-service` are on the **same Wi-Fi network**.
    2.  Find the **IP address** of your computer on that network (e.g., `192.168.1.100`).
    3.  Update the `BASE_URL` in `ApiService.kt` to `http://<YOUR_COMPUTER_IP_ADDRESS>:5000/`. For example: `http://192.168.1.100:5000/`.
    4.  Rebuild and run the app on your physical device.
    5.  Ensure your computer's firewall allows incoming connections on port 5000.

## Google Assistant Integration

The app can receive text input via Google Assistant or other apps that use Android's `ACTION_SEND` intent with `text/plain` data.

-   **How it Works:** The `MainActivity` is configured with an intent filter to handle these intents. When triggered, the app extracts the shared text and automatically sends it as a message to the AI backend.
-   **Example Trigger (Phrasing may vary):**
    *   "Hey Google, share this with Phone AI Chat: Hello, how are you?"
    *   "Hey Google, send to Phone AI Chat: What's the weather like?"
    *   Using the "Share" functionality in another app and selecting "Phone AI Chat" (if it appears in the share sheet, depending on Android version and Assistant context).
-   **Outcome:** The Phone AI Chat app will launch (or be brought to the foreground), and the message sent via Assistant will appear in the chat, followed by the AI's response.

## Project Structure Overview

-   `app/src/main/java/com/example/phone_app/MainActivity.kt`: The main entry point of the application, containing the Jetpack Compose UI and core logic.
-   `app/src/main/java/com/example/phone_app/network/ApiService.kt`: Defines the Retrofit interface for communicating with the backend service.
-   `app/src/main/java/com/example/phone_app/network/ChatModels.kt`: Contains the data classes (`ChatRequest`, `ChatResponse`) for network communication.
-   `app/build.gradle.kts`: Module-level Gradle build script, defining dependencies and Android configurations.
-   `app/src/main/AndroidManifest.xml`: Declares app components, permissions, and intent filters.
-   `app/src/main/res/`: Resource directory for drawables, layouts (though minimal for Compose), values (strings, colors, themes).

## Troubleshooting Tips

-   **"Cannot reach server" / Network Errors:**
    *   Ensure the `backend-service` is running on your computer.
    *   Verify the `BASE_URL` in `ApiService.kt` is correctly configured for your target (emulator vs. physical device with correct IP).
    *   If using a physical device, check that it's on the same Wi-Fi network as the backend server and that your firewall is not blocking the connection.
-   **Google Assistant Integration Not Working:**
    *   Ensure the Phone AI Chat app is correctly installed on the device/emulator.
    *   Verify the intent filter for `ACTION_SEND` is present in `AndroidManifest.xml`.
    *   The exact phrasing for Google Assistant can be sensitive and may vary. Try different ways of asking Assistant to "send" or "share" text to the app.
-   **App Crashes or Build Fails:**
    *   Clean the project (`Build > Clean Project`) and rebuild.
    *   Ensure all dependencies in `build.gradle.kts` are correctly synced.
    *   Check Android Studio's Logcat for error messages if the app crashes at runtime.
    *   Ensure you meet the minimum Android API level (26).
