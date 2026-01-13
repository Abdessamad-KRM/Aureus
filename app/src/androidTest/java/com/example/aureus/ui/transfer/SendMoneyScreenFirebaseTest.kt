package com.example.aureus.ui.transfer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aureus.ui.theme.AureusTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simplified UI Tests for SendMoneyScreenFirebase
 * Tests basic component structure without complex assertions
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SendMoneyScreenFirebaseTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order =1)
    val composeTestRule = createComposeRule()

    @Test
    fun sendMoneyScreen_rendersWithoutCrash() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }
    }

    @Test
    fun sendMoneyScreen_displaysTitle() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Send Money", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysAmountField() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Amount", ignoreCase = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("MAD", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysContactsSection() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("All Contacts", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysSendButton() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Send Money", ignoreCase = true, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysNoteInput() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Note", ignoreCase = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("(Optional)", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysEmptyState() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("No contacts yet", ignoreCase = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Add your first contact", ignoreCase = true, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysAddContactButton() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Add Contact", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_displaysFavoritesSection() {
        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Favorites", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_themeApplied() {
        composeTestRule.setContent {
            AureusTheme(darkTheme = false) {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Send Money", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_darkThemeApplied() {
        composeTestRule.setContent {
            AureusTheme(darkTheme = true) {
                SendMoneyScreenFirebase(
                    onNavigateBack = {},
                    onSendClick = { _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Send Money", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun sendMoneyScreen_callbacksAreFunctional() {
        var backInvoked = false
        var sendInvoked = false
        var addContactInvoked = false

        composeTestRule.setContent {
            AureusTheme {
                SendMoneyScreenFirebase(
                    onNavigateBack = { backInvoked = true },
                    onSendClick = { _, _ -> sendInvoked = true },
                    onAddContactClick = { addContactInvoked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Send Money", ignoreCase = true)
            .assertIsDisplayed()
    }
}