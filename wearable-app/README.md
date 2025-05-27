# Wearable AI Chat Client

This is an Android Wear OS application that allows users to interact with a backend chat service using voice input. It displays the conversation history and uses Text-to-Speech to read out responses from the backend.

## Features

*   **Voice Input:** Uses Android's built-in speech recognition to capture user messages.
*   **Conversation Display:** Shows the chat history in a scrollable list.
*   **Backend Communication:** Sends user messages to a backend service and receives responses.
*   **Text-to-Speech (TTS):** Reads out the backend's responses aloud.
*   **User-Friendly Interface:** Includes an initial prompt, thinking state, and error messages.
*   **Auto-Scrolling:** Automatically scrolls to the latest message.
*   **Mic Button State:** Disables the microphone button during ongoing network requests.

## Requirements

*   **Android Studio:** Version Hedgehog (2023.1.1) or newer recommended.
*   **Wear OS Emulator or Physical Device:** API Level 30 (Android 11) or higher.
*   **Running Backend Service:** The `backend-service` (included in this project) must be running and accessible.

## Setup & Build

1.  **Clone the Repository:**
    ```bash
    git clone <repository_url>
    cd <repository_name>
    ```

2.  **Open in Android Studio:**
    *   Open Android Studio.
    *   Select "Open" and navigate to the `wearable-app` directory within the cloned project.
    *   Allow Android Studio to perform a Gradle sync. This may take a few minutes.

3.  **Build the Application:**
    *   Once Gradle sync is complete, build the project by selecting `Build > Make Project` from the Android Studio menu, or by clicking the "Play" button (Run 'app') with a target device/emulator selected.

## Running the Application

1.  **Select a Target:**
    *   In Android Studio, choose a Wear OS emulator (API 30+) or connect a physical Wear OS device (ensure USB debugging is enabled).

2.  **Run 'app' Configuration:**
    *   Click the "Play" button (Run 'app') in Android Studio. The application will be built and installed on the selected target.

3.  **Backend Service Dependency (CRUCIAL):**
    *   This application **requires** the `backend-service` (located in the `backend-service` directory of this project) to be running.
    *   **If using an Android Emulator:** The backend service, when run on your host machine, is typically accessible to the emulator at `http://10.0.2.2:5000/`. This is the default `BASE_URL` in `wearable-app/app/src/main/java/com/example/wearableaichat/network/ApiService.kt`. No changes should be needed.
    *   **If using a Physical Wear OS Device:**
        1.  Ensure your physical device and the machine running the backend service are on the **same Wi-Fi network**.
        2.  Find the **network IP address** of the machine running the backend service (e.g., `192.168.1.100`).
            *   On macOS/Linux: `ifconfig` or `ip addr`
            *   On Windows: `ipconfig`
        3.  You **MUST** update the `BASE_URL` in `wearable-app/app/src/main/java/com/example/wearableaichat/network/ApiService.kt`. Change it from `http://10.0.2.2:5000/` to your host machine's network IP, for example:
            ```kotlin
            // private const val BASE_URL = "http://10.0.2.2:5000/" // Emulator
            private const val BASE_URL = "http://192.168.1.100:5000/" // Replace with your actual IP
            ```
        4.  Rebuild and reinstall the app after changing the `BASE_URL`.

## Project Structure Overview

*   `app/src/main/java/com/example/wearableaichat/`:
    *   `MainActivity.kt`: The main entry point and primary UI logic, built with Jetpack Compose for Wear OS. Handles ASR, TTS, and conversation display.
    *   `network/`:
        *   `ApiService.kt`: Defines the Retrofit interface for communicating with the backend and includes the Retrofit client setup.
        *   `ChatModels.kt`: Contains the data classes (`ChatRequest`, `ChatResponse`) for network communication.
*   `app/src/main/AndroidManifest.xml`: Declares app components, permissions (RECORD_AUDIO, INTERNET), and features.
*   `app/src/main/res/`: Contains app resources.
    *   `values/strings.xml`: User-facing strings, error messages, and prompts.
    *   `mipmap-*/`: Launcher icons.
*   `app/build.gradle.kts`: Module-level Gradle build script, defining dependencies and Android configurations.
*   `build.gradle.kts` (Project Level): Top-level Gradle build script.

## Troubleshooting Tips

*   **"Cannot reach server" / Network Errors:**
    *   Ensure the `backend-service` is running on your host machine.
    *   Verify the `BASE_URL` in `ApiService.kt` is correctly configured for your target (emulator vs. physical device on network).
    *   If using emulator to access the backend as `http://127.0.0.1:port/`, run `adb -s <wearable_device_id> reverse tcp:port tcp:port` to forward ports via ADB.
    *   Check that your Wear OS device/emulator has network connectivity.
    *   Look at Logcat in Android Studio for detailed error messages (filter by your app's package name: `com.example.wearableaichat`).
*   **No Speech Input / "Speech input not available":**
    *   Ensure the Wear OS device/emulator has a working microphone and that the app has been granted the `RECORD_AUDIO` permission. The system usually prompts for this when the ASR intent is first launched.
    *   Check Logcat for ASR-related errors.
*   **TTS Not Working:**
    *   Verify device volume is up.
    *   Check Logcat for TTS initialization errors (e.g., language not supported).
*   **App Crashes:**
    *   Check Logcat for stack traces and error messages to identify the cause.

---

This application serves as a practical example of building a voice-interactive chat client on Wear OS with backend communication and TTS features.
