package com.example.phone_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phone_app.network.ChatRequest
import com.example.phone_app.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Define a data class for messages for clarity
data class Message(val text: String, val isUserMessage: Boolean, val timestamp: Long = System.currentTimeMillis())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var initialMessageFromIntent: String? = null
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                initialMessageFromIntent = it
            }
            // To prevent processing the same intent multiple times if the activity is recreated
            // without a new intent, you might clear the intent or use a flag.
            // For simple cases, if the app is always launched fresh by Assistant or share,
            // this might be okay. A more robust solution uses onNewIntent and setIntent.
            // For now, we assume Assistant/Share will launch it fresh or provide a new intent.
        }

        setContent {
            PhoneAiChatTheme {
                ChatScreen(initialMessage = initialMessageFromIntent)
            }
        }
    }

    // It's good practice to handle new intents if the activity is already running (singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        // If you need to react immediately to this new intent in Compose,
        // you'd need a way to signal your Composable.
        // For this subtask, we'll focus on onCreate handling.
        // A full solution might involve a ViewModel or a mutableState passed down
        // that gets updated here, triggering recomposition.
         var newInitialMessage: String? = null
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                newInitialMessage = it
            }
        }
        // This is tricky with setContent in onCreate. A common pattern is to have a
        // mutable state holder (like a ViewModel) that gets updated with new intents,
        // and Compose observes this state.
        // For now, the onCreate logic will handle the initial launch.
        // If app is already open and receives a new SEND intent, this simplistic approach
        // might not show the new message until next full onCreate.
        // To keep it within scope, we'll rely on onCreate for this task.
        // A more robust solution would involve a ViewModel or a different way to trigger recomposition with new initial data.
        // For this task, we'll assume the intent is primarily handled on initial creation.
         if (newInitialMessage != null) {
             // Re-set content might be too heavy, ideally, update a state that ChatScreen observes.
             // For this task, we are focusing on the initial intent.
             // A full app might use a MutableStateFlow in a ViewModel.
         }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(initialMessage: String? = null) {
    val messages = remember { mutableStateListOf<Message>() }
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Helper function to send a message (used by button and intent)
    val sendMessage = fun(messageText: String) {
        if (messageText.isBlank()) {
            //return@sendMessage
            return
        }

        val userMessage = Message(messageText, true)
        messages.add(userMessage)
        if (textFieldValue.text == messageText) { // Clear input only if it was from text field
             textFieldValue = TextFieldValue("")
        }

        isLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitClient.instance.sendMessage(ChatRequest(messageText))
                if (response.isSuccessful) {
                    response.body()?.response?.let { aiText ->
                        messages.add(Message(aiText, false))
                    } ?: run {
                        messages.add(Message("Error: Empty response from server", false))
                        Toast.makeText(context, "Error: Empty response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    messages.add(Message("Error: ${response.code()} - $errorBody", false))
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                messages.add(Message("Network Error: ${e.message}", false))
                Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Add initial AI message
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) { // Add only if no messages exist (e.g. from intent)
            messages.add(Message("Hello! How can I help you today?", false))
        }
    }

    // Process initial message from intent (if any)
    LaunchedEffect(initialMessage) {
        initialMessage?.let {
            if (messages.none { msg -> msg.text == it && msg.isUserMessage }) {
                sendMessage(it)
            }
        }
    }

    // Coroutine to scroll to the bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone AI Chat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            MessageInputRow(
                textFieldValue = textFieldValue,
                onTextFieldChange = { textFieldValue = it },
                onSendClick = { sendMessage(textFieldValue.text.trim()) },
                isLoading = isLoading
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.timestamp }) { message ->
                MessageBubble(message)
            }
        }
    }
}

@Composable
fun MessageInputRow(
    textFieldValue: TextFieldValue,
    onTextFieldChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = onTextFieldChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            enabled = !isLoading,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSendClick,
            enabled = !isLoading && textFieldValue.text.isNotBlank(),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { /* TODO: Implement voice input */ }, enabled = !isLoading) {
            Icon(Icons.Filled.Mic, contentDescription = "Voice Input")
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isUserMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isUserMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val alignment = if (message.isUserMessage) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = if (message.isUserMessage) 16.dp else 4.dp,
                topEnd = if (message.isUserMessage) 4.dp else 16.dp, // Corrected: isUser_message to isUserMessage
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.align(alignment)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun PhoneAiChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF006CFF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDCE1FF),
            onPrimaryContainer = Color(0xFF001A40),
            secondary = Color(0xFF535F70),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF191C20),
        ),
        typography = Typography(),
        content = content
    )
}
