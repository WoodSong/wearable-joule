package com.example.wearableaichat

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wearableaichat.network.ApiService
import com.example.wearableaichat.network.ChatRequest
import com.example.wearableaichat.network.ChatResponse
import com.example.wearableaichat.network.RetrofitClient
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)

    private lateinit var mockApiService: MockApiService
    private lateinit var originalApiService: ApiService


    // String resources - it's better to load them via composeTestRule.activity.getString(R.string.X)
    // but for simplicity in this context, we'll hardcode them and ensure they match strings.xml
    private val stringAiPrefix by lazy { composeTestRule.activity.getString(R.string.ai_prefix) }
    private val stringUserPrefix by lazy { composeTestRule.activity.getString(R.string.user_prefix) }
    private val initialPrompt by lazy { composeTestRule.activity.getString(R.string.message_initial_prompt) }
    private val thinkingMessage by lazy { stringAiPrefix + composeTestRule.activity.getString(R.string.info_thinking) }
    private val errorNetworkGeneric by lazy { stringAiPrefix + composeTestRule.activity.getString(R.string.error_network_generic) }
    private val errorServerUnreachable by lazy { stringAiPrefix + composeTestRule.activity.getString(R.string.error_server_unreachable) }
    private val errorAsrFailedCancelled by lazy { stringAiPrefix + composeTestRule.activity.getString(R.string.error_asr_failed_cancelled) }


    inner class MockApiService : ApiService {
        var simulateSuccess: Boolean = true
        var responseMessage: String = "Test AI Response"
        var errorCode: Int = 500
        var simulateNetworkError: Boolean = false

        override suspend fun sendMessage(request: ChatRequest): Response<ChatResponse> {
            return if (simulateNetworkError) {
                throw java.net.UnknownHostException("Simulated network error")
            } else if (simulateSuccess) {
                Response.success(ChatResponse(responseMessage, null))
            } else {
                Response.error(
                    errorCode,
                    "{\"error\":\"Simulated server error\"}".toResponseBody("application/json".toMediaTypeOrNull())
                )
            }
        }
    }

    @Before
    fun setUp() {
        mockApiService = MockApiService()
        // Get the original ApiService instance from RetrofitClient
        originalApiService = RetrofitClient.instance

        // Replace the original ApiService with the mock
        RetrofitClient.instance = mockApiService

        // Initialize string resources here using composeTestRule.activity
        // This ensures that they are loaded after the activity is created.
        // Example: initialPrompt = composeTestRule.activity.getString(R.string.message_initial_prompt)
        // (already done with by lazy, but if not lazy, this is where it would go)
    }


    @Test
    fun testInitialPromptIsDisplayed() {
        composeTestRule.onNodeWithText(initialPrompt).assertIsDisplayed()
    }

    @Test
    fun testUserMessageReplacesInitialPrompt() {
        val userMessage = "Hello AI"
        val fullUserMessage = stringUserPrefix + userMessage

        // Mock successful speech recognition
        val resultData = Intent()
        resultData.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf(userMessage))
        Intents.intending(IntentMatchers.actionMatches(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.cd_voice_input_button)).performClick()

        composeTestRule.onNodeWithText(fullUserMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText(initialPrompt).assertDoesNotExist()
    }

    @Test
    fun testThinkingMessageReplacesUserMessage() {
        val userMessage = "Tell me a joke"
        val fullUserMessage = stringUserPrefix + userMessage

        val resultData = Intent()
        resultData.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf(userMessage))
        Intents.intending(IntentMatchers.actionMatches(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.cd_voice_input_button)).performClick()

        // Verify user message is shown first
        composeTestRule.onNodeWithText(fullUserMessage).assertIsDisplayed()

        // Then, verify "Thinking..." message replaces it
        composeTestRule.waitUntil(timeoutMillis = 2000) { // Give some time for the coroutine to launch
            composeTestRule.onAllNodesWithText(thinkingMessage).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(thinkingMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText(fullUserMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(initialPrompt).assertDoesNotExist()
    }

    @Test
    fun testAIResponseMessageReplacesThinkingMessage() {
        val userMessage = "What's the weather?"
        val aiResponse = "It's sunny!"
        val fullAiResponse = stringAiPrefix + aiResponse
        mockApiService.responseMessage = aiResponse
        mockApiService.simulateSuccess = true

        val resultData = Intent()
        resultData.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf(userMessage))
        Intents.intending(IntentMatchers.actionMatches(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.cd_voice_input_button)).performClick()

        // Wait for AI response
        composeTestRule.waitUntil(timeoutMillis = 5000) { // Increased timeout for network op
             composeTestRule.onAllNodesWithText(fullAiResponse).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(fullAiResponse).assertIsDisplayed()
        composeTestRule.onNodeWithText(thinkingMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(stringUserPrefix + userMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(initialPrompt).assertDoesNotExist()
    }

    @Test
    fun testNetworkErrorMessageReplacesThinkingMessage() {
        val userMessage = "Test network error"
        // The actual error message will be "AI: Server is unreachable" or "AI: Network error occurred (Simulated network error)"
        // depending on the exact exception and string resource.
        // Let's use a flexible check with startsWith for the prefix.
        val expectedErrorMessagePrefix = errorServerUnreachable // More specific for UnknownHostException

        mockApiService.simulateNetworkError = true

        val resultData = Intent()
        resultData.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf(userMessage))
        Intents.intending(IntentMatchers.actionMatches(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.cd_voice_input_button)).performClick()

        // Wait for error message
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(expectedErrorMessagePrefix, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(expectedErrorMessagePrefix, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(thinkingMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(stringUserPrefix + userMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(initialPrompt).assertDoesNotExist()
    }
    
    @Test
    fun testServerErrorMessageReplacesThinkingMessage() {
        val userMessage = "Test server error"
        val
                httpErrorPrefix = composeTestRule.activity.getString(R.string.error_server_http_error_prefix)
        val expectedErrorMessage = stringAiPrefix + httpErrorPrefix + "500 Simulated server error" // Adjusted to match actual error construction

        mockApiService.simulateSuccess = false
        mockApiService.errorCode = 500


        val resultData = Intent()
        resultData.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arrayListOf(userMessage))
        Intents.intending(IntentMatchers.actionMatches(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.cd_voice_input_button)).performClick()

        // Wait for error message
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(expectedErrorMessage, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(expectedErrorMessage, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(thinkingMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(stringUserPrefix + userMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(initialPrompt).assertDoesNotExist()
    }


    @After
    fun tearDown() {
        // Restore the original ApiService instance
        RetrofitClient.instance = originalApiService
        Intents.release() // Release intents if initialized
    }
}
