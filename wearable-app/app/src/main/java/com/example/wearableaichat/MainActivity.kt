package com.example.wearableaichat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.*
import com.example.wearableaichat.network.ChatRequest
import com.example.wearableaichat.network.RetrofitClient
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.Locale
import androidx.wear.compose.material.Colors // Import Colors
import kotlinx.coroutines.CoroutineScope // Import CoroutineScope
import androidx.compose.ui.tooling.preview.Devices
import android.net.Uri
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val apiService by lazy { RetrofitClient.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Pass the activity's lifecycleScope to WearApp, which will then pass it to ChatScreen
            WearApp(apiService = apiService, coroutineScope = lifecycleScope)
        }
    }

    // Companion object to hold default messages if needed, or they can be directly in composables
}

@Composable
fun WearApp(apiService: com.example.wearableaichat.network.ApiService, coroutineScope: CoroutineScope) {
    WearableAiChatTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            ChatScreen(
                apiService = apiService,
                coroutineScope = coroutineScope, // Pass the coroutineScope here
                listState = listState
            )
        }
    }
}

@Composable
fun ChatScreen(
    apiService: com.example.wearableaichat.network.ApiService,
    coroutineScope: CoroutineScope, // Changed parameter type
    listState: ScalingLazyListState
) {
    // Holds the list of messages displayed in the chat.
    val messages = remember { mutableStateListOf<String>() }
    // Tracks if a network request is currently active (e.g., waiting for backend response).
    // Used to disable the microphone button to prevent concurrent requests.
    var isNetworkRequestInProgress by remember { mutableStateOf(false) }
    // General coroutine scope for launching tasks within this composable.
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    // TextToSpeech engine instance.
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    // Flag to track if TTS has been successfully initialized.
    var ttsInitialized by remember { mutableStateOf(false) }

    val stringAiPrefix = stringResource(id = R.string.ai_prefix)
    val stringUserPrefix = stringResource(id = R.string.user_prefix)

    // Hoist the strings that will be used in the ttsListener
    val errorTtsLangNotSupported = stringResource(id = R.string.error_tts_language_not_supported)
    val errorTtsInitFailed = stringResource(id = R.string.error_tts_init_failed)

    // Listener for TextToSpeech engine initialization.
    val ttsListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            // Attempt to set the language to the device's default.
            val result = tts?.setLanguage(Locale.getDefault())
            // Check if the language is available and supported.
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "TTS Language not supported.")
                messages.add(stringAiPrefix + errorTtsLangNotSupported)
            } else {
                ttsInitialized = true // TTS is ready to use.
                Log.i("TTS", "TTS Initialization successful.")
            }
        } else {
            Log.e("TTS", "TTS Initialization failed.")
            messages.add(stringAiPrefix + errorTtsInitFailed)
        }
    }

    // Initialize TTS when the composable first enters the composition.
    LaunchedEffect(key1 = Unit) {
        Log.d("TTS", "Initializing TTS")
        tts = TextToSpeech(context, ttsListener)
    }

    // Dispose of TTS resources when the composable is removed from the composition.
    DisposableEffect(key1 = tts) {
        onDispose {
            Log.d("TTS", "Disposing TTS engine.")
            tts?.stop() // Stop any ongoing speech.
            tts?.shutdown() // Release TTS engine resources.
            ttsInitialized = false
            Log.d("TTS", "TTS disposed.")
        }
    }

    // Function to speak the given text using TTS.
    // It removes the "AI: " prefix before speaking for a more natural sound.
    fun speak(text: String) {
        if (ttsInitialized && tts != null) {
            tts?.speak(text.removePrefix(stringAiPrefix), TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TTS", "TTS not initialized or null, cannot speak.")
            // Avoid flooding chat with "Cannot speak" messages if TTS repeatedly fails.
            // Consider a one-time notification or a more subtle indicator if needed.
        }
    }

    // Effect to automatically scroll to the newest message when the messages list changes.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val infoThinkingStrRes = stringResource(id = R.string.info_thinking)
    val errorAsrUnavailableStrRes = stringResource(id = R.string.error_asr_unavailable)
    val errorAsrNotRecognizedStrRes = stringResource(id = R.string.error_asr_not_recognized)
    val errorAsrFailedCancelledStrRes = stringResource(id = R.string.error_asr_failed_cancelled)
    val errorNetworkGenericStrRes = stringResource(id = R.string.error_network_generic)
    val errorServerHttpErrorPrefixStrRes = stringResource(id = R.string.error_server_http_error_prefix)
    val errorServerEmptyResponseStrRes = stringResource(id = R.string.error_server_empty_response)
    val errorServerMalformedResponseStrRes = stringResource(id = R.string.error_server_malformed_response)
    val errorServerUnreachableStrRes = stringResource(id = R.string.error_server_unreachable)

    // ActivityResultLauncher for handling results from the speech recognition intent.
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // Extract recognized speech.
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                val recognizedText = results[0]
                messages.add(stringUserPrefix + recognizedText) // Add user's message to chat.

                // Show "Thinking..." message and initiate network request.
                val thinkingMessage = stringAiPrefix + infoThinkingStrRes
                messages.add(thinkingMessage)
                isNetworkRequestInProgress = true // Disable mic button.

                // Launch a coroutine to handle the network request.
                coroutineScope.launch { // Changed to use coroutineScope
                    try {
                        val response = apiService.sendMessage(ChatRequest(recognizedText))
                        messages.remove(thinkingMessage) // Remove "Thinking..." message.

                        if (response.isSuccessful) {
                            val chatResponse = response.body()
                            if (chatResponse != null) {
                                if (chatResponse.response != null) {
                                    // AI successfully responded.
                                    val aiResponseText = stringAiPrefix + chatResponse.response
                                    messages.add(aiResponseText)
                                    speak(aiResponseText) // Speak the AI's response.
                                } else if (chatResponse.error != null) {
                                    // Backend returned an error (e.g., "No message provided").
                                    messages.add(stringAiPrefix + errorServerHttpErrorPrefixStrRes + chatResponse.error)
                                } else {
                                    // Backend response was successful but empty/malformed.
                                    messages.add(stringAiPrefix + errorServerEmptyResponseStrRes)
                                }
                            } else {
                                // Response body was null, indicating a malformed response from server.
                                messages.add(stringAiPrefix + errorServerMalformedResponseStrRes)
                            }
                        } else {
                            // HTTP error (e.g., 404, 500).
                            messages.add(stringAiPrefix + errorServerHttpErrorPrefixStrRes + "${response.code()} ${response.message()}")
                        }
                    } catch (e: Exception) {
                        // Handle network exceptions (e.g., no internet, server unreachable).
                        messages.remove(thinkingMessage) // Ensure "Thinking..." is removed on error.
                        val errorMessage = when (e) {
                            is UnknownHostException, is ConnectException -> stringAiPrefix + errorServerUnreachableStrRes
                            else -> stringAiPrefix + errorNetworkGenericStrRes + " (${e.message})"
                        }
                        messages.add(errorMessage)
                        Log.e("ChatScreen", "Network request failed", e)
                    } finally {
                        isNetworkRequestInProgress = false // Re-enable mic button.
                    }
                }
            } else {
                // Speech recognized, but no results (should be rare).
                messages.add(stringAiPrefix + errorAsrNotRecognizedStrRes)
            }
        } else {
            // Speech recognition failed or was cancelled by the user.
            messages.add(stringAiPrefix + errorAsrFailedCancelledStrRes)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Display initial prompt if no messages are present.
        if (messages.isEmpty()) {
            Text(
                text = stringResource(id = R.string.message_initial_prompt),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1
            )
        } else {
            // Display the conversation history.
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(top = 20.dp, bottom = 70.dp, start = 8.dp, end = 8.dp), // Increased bottom padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(messages.size) { index ->
//                    MarkdownTelText(
//                        markdownText = messages[index],
//                        modifier = Modifier.fillMaxSize().padding(4.dp, 2.dp, 4.dp, 2.dp) // Padding for each message`
//                    )
                    androidx.wear.compose.material.Text(
                        text = messages[index],
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Microphone Button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, infoThinkingStrRes) // Re-use "Thinking..." or a dedicated prompt
                    }
                    try {
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: Exception) {
                        messages.add(stringAiPrefix + errorAsrUnavailableStrRes)
                        Log.e("ChatScreen", "Speech recognition not available", e)
                    }
                },
                enabled = !isNetworkRequestInProgress, // Disable button when network request is in progress
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = stringResource(id = R.string.cd_voice_input_button)
                )
            }
        }
    }
}

@Composable
fun WearableAiChatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = Colors(
            primary = androidx.compose.ui.graphics.Color.Cyan,
            background = androidx.compose.ui.graphics.Color.Black,
            onPrimary = androidx.compose.ui.graphics.Color.Black,
            onBackground = androidx.compose.ui.graphics.Color.White,
            surface = androidx.compose.ui.graphics.Color.DarkGray,
            onSurface = androidx.compose.ui.graphics.Color.White
            // You can customize other colors like error, secondary etc. here
        ),
        typography = Typography(), // Use default or define your own
        shapes = Shapes(), // Use default or define your own
        content = content
    )
}

//@Composable
//fun MarkdownTelText(markdownText: String, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    val annotatedString = buildAnnotatedString {
//        val regex = """\[([^\]]*)\]\((tel:[0-9]+)\)""".toRegex()
//        var lastIndex = 0
//
//        regex.findAll(markdownText).forEach { matchResult ->
//            Log.e("MarkdownTelText", "Match found: ${matchResult.value}")
//            val (displayText, rawUrl) = matchResult.destructured // rawUrl 可能包含末尾的 ')'
//            val startIndex = matchResult.range.first
//            val endIndex = matchResult.range.last + 1
//
//            // 添加非链接部分
//            if (startIndex > lastIndex) {
//                append(markdownText.substring(lastIndex, startIndex))
//            }
//            val url = rawUrl
//
//            // 添加链接部分
//            pushStringAnnotation(tag = "tel", annotation = url)
//            withStyle(style = SpanStyle(color = MaterialTheme.colors.primary, textDecoration = TextDecoration.Underline)) {
//                append(displayText)
//            }
//            pop() // 弹出 annotation
//            lastIndex = endIndex
//        }
//
//        // 添加剩余文本
//        if (lastIndex < markdownText.length) {
//            append(markdownText.substring(lastIndex, markdownText.length))
//        }
//    }
//
//    Log.e("MarkdownTelText", "Annotated string created: $annotatedString")
//
//    ClickableText(
//        text = annotatedString,
//        modifier = modifier,
//        onClick = { offset ->
//            annotatedString.getStringAnnotations(tag = "tel", start = offset, end = offset)
//                .firstOrNull()?.let { annotation ->
//                    val telUri = annotation.item
//                    if (telUri.startsWith("tel:")) {
//                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(telUri))
//                        // 确保应用程序有拨打电话的权限：
//                        // 在 AndroidManifest.xml 中添加：
//                        // <uses-permission android:name="android.permission.CALL_PHONE" />
//                        // 注意：ACTION_DIAL 不需要 CALL_PHONE 权限，它只是打开拨号界面。
//                        // 如果是 ACTION_CALL，则需要。
//                        context.startActivity(intent)
//                    }
//                }
//        }
//    )
//}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    // Mock API service and lifecycle scope for preview
    val mockApiService = object : com.example.wearableaichat.network.ApiService {
        override suspend fun sendMessage(request: ChatRequest): retrofit2.Response<com.example.wearableaichat.network.ChatResponse> {
            // Simulate a delay and response
            kotlinx.coroutines.delay(1000)
//            val replyContent = "Hello from preview!"
            val replyContent = "name: Andy Wang | tel1: [12345678901](tel:12345678901) ｜ tel2: [9876543210](tel:9876543210)"
            return retrofit2.Response.success(com.example.wearableaichat.network.ChatResponse(replyContent, null))
        }
    }
    // A simple way to get a CoroutineScope for preview. In real app, use lifecycleScope.
    val previewCoroutineScope = rememberCoroutineScope()

    WearableAiChatTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(
            timeText = { TimeText(modifier = Modifier.scrollAway(listState)) },
            vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            ChatScreen(
                apiService = mockApiService,
                coroutineScope = previewCoroutineScope, // Changed to coroutineScope
                listState = listState
            )
        }
    }
}
