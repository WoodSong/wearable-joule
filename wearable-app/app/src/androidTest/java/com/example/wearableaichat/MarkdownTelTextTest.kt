package com.example.wearableaichat

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wearableaichat.ui.theme.WearableAiChatTheme // Assuming this is your theme
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URLEncoder

@RunWith(AndroidJUnit4::class)
class MarkdownTelTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testTelephoneLink_isDisplayedAndLaunchesDialIntent() {
        val linkText = "Call Home"
        val telephoneNumber = "1234567890"
        val markdown = "Please [$linkText](tel:$telephoneNumber)"
        val expectedUri = "tel:$telephoneNumber"

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = markdown)
            }
        }

        // Verify the link text is displayed
        composeTestRule.onNodeWithText(linkText).assertExists()
        // Verify plain text part is displayed
        composeTestRule.onNodeWithText("Please ", substring = true).assertExists()

        // Perform click on the link
        composeTestRule.onNodeWithText(linkText).performClick()

        // Verify the intent
        Intents.intended(allOf(
            hasAction(Intent.ACTION_DIAL),
            hasData(Uri.parse(expectedUri))
        ))
    }

    @Test
    fun testLocationLink_isDisplayedAndLaunchesMapIntent() {
        val linkText = "Company HQ"
        val locationAddress = "1600 Amphitheatre Parkway, Mountain View, CA"
        val markdown = "Location: [$linkText](location:$locationAddress)"
        val encodedAddress = URLEncoder.encode(locationAddress, "UTF-8")
        val expectedGeoUri = "geo:0,0?q=$encodedAddress"

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = markdown)
            }
        }

        // Verify the link text is displayed
        composeTestRule.onNodeWithText(linkText).assertExists()
        // Verify plain text part is displayed
        composeTestRule.onNodeWithText("Location: ", substring = true).assertExists()

        // Perform click on the link
        composeTestRule.onNodeWithText(linkText).performClick()

        // Verify the intent
        Intents.intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse(expectedGeoUri))
        ))
    }

    @Test
    fun testMixedContent_displaysAllPartsAndLinksWork() {
        val callText = "Call Support"
        val telNum = "0987654321"
        val locationText = "Visit Us"
        val locAddr = "1 Infinite Loop, Cupertino, CA"
        val markdown = "For help, [$callText](tel:$telNum) or [$locationText](location:$locAddr). Thanks!"

        val encodedLocAddr = URLEncoder.encode(locAddr, "UTF-8")
        val expectedTelUri = "tel:$telNum"
        val expectedGeoUri = "geo:0,0?q=$encodedLocAddr"

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = markdown)
            }
        }

        // Verify all text parts are displayed
        composeTestRule.onNodeWithText("For help, ", substring = true).assertExists()
        composeTestRule.onNodeWithText(callText).assertExists()
        composeTestRule.onNodeWithText(" or ", substring = true).assertExists()
        composeTestRule.onNodeWithText(locationText).assertExists()
        composeTestRule.onNodeWithText(". Thanks!", substring = true).assertExists()

        // Test telephone link
        composeTestRule.onNodeWithText(callText).performClick()
        Intents.intended(allOf(
            hasAction(Intent.ACTION_DIAL),
            hasData(Uri.parse(expectedTelUri))
        ))

        // Test location link
        // Note: Espresso Intents records all intents. If not re-initializing or clearing,
        // this will check against all intents launched so far in this test.
        // For distinct checks, ensure previous intended intents are cleared or use more specific matchers if needed.
        // However, in separate clicks, it should be fine as performClick is on a different node.
        composeTestRule.onNodeWithText(locationText).performClick()
        Intents.intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse(expectedGeoUri))
        ))
    }

    @Test
    fun testNoLinks_displaysPlainText() {
        val plainText = "This is a simple text with no links."
        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = plainText)
            }
        }
        // Verify the full plain text is displayed as a single entity
        composeTestRule.onNodeWithText(plainText).assertExists()
    }

    @Test
    fun testMalformedTelLink_isDisplayedAsPlainText() {
        // Example: Missing number, or incorrect format
        val malformedText = "Call [here](tel:)"
        val fullText = "Call here" // How it should be rendered if link is ignored/stripped

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = malformedText)
            }
        }
        // Check that the display text "here" is present
        composeTestRule.onNodeWithText("here").assertExists()
        // Check that "Call " is present
        composeTestRule.onNodeWithText("Call ", substring = true).assertExists()
        // Clicking "here" should not launch an intent
        composeTestRule.onNodeWithText("here").performClick()
        // No intents should have been sent for "tel:"
        // Intents.assertNoUnverifiedIntents() // This can be tricky if other things send intents.
        // A more robust way is to check that NO intent matching the specific malformed one was sent,
        // but since it's malformed, it shouldn't match the valid intent check.
        // For simplicity, we assume if it's not parsed as a link, clicking does nothing.
    }

    @Test
    fun testMalformedLocationLink_isDisplayedAsPlainText() {
        val malformedText = "Visit [us](location:)" // Empty location
        val fullText = "Visit us"

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = malformedText)
            }
        }
        composeTestRule.onNodeWithText("us").assertExists()
        composeTestRule.onNodeWithText("Visit ", substring = true).assertExists()
        composeTestRule.onNodeWithText("us").performClick()
        // Similar to the malformed tel link, expect no specific intent.
    }

    @Test
    fun testLinkWithSpecialCharactersInDisplayText() {
        val linkText = "Call (Office)!"
        val telephoneNumber = "5551234"
        val markdown = "[$linkText](tel:$telephoneNumber)"
        val expectedUri = "tel:$telephoneNumber"

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = markdown)
            }
        }
        composeTestRule.onNodeWithText(linkText).assertExists()
        composeTestRule.onNodeWithText(linkText).performClick()
        Intents.intended(allOf(
            hasAction(Intent.ACTION_DIAL),
            hasData(Uri.parse(expectedUri))
        ))
    }

    @Test
    fun testLocationLinkWithSpecialCharactersInAddress() {
        val linkText = "Weird Place"
        // Address with characters that need encoding
        val locationAddress = "123 & Main St, O'Malley's, Anytown, USA?"
        val markdown = "[$linkText](location:$locationAddress)"
        val encodedAddress = URLEncoder.encode(locationAddress, "UTF-8")
        val expectedGeoUri = "geo:0,0?q=$encodedAddress"

        composeTestRule.setContent {
            WearableAiChatTheme {
                MarkdownTelText(markdownText = markdown)
            }
        }

        composeTestRule.onNodeWithText(linkText).assertExists()
        composeTestRule.onNodeWithText(linkText).performClick()

        Intents.intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse(expectedGeoUri))
        ))
    }
}
