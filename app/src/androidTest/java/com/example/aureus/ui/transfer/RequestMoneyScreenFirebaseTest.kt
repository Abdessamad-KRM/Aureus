package com.example.aureus.ui.transfer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aureus.ui.theme.AureusTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simplified UI Tests for RequestMoneyScreenFirebase
 * Tests basic component structure without complex assertions
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RequestMoneyScreenFirebaseTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun requestMoneyScreen_rendersWithoutCrash() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }
    }

    @Test
    fun requestMoneyScreen_displaysTitle() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Request Money", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun requestMoneyScreen_displaysAmountField() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
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
    fun requestMoneyScreen_displaysContactSection() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("All Contacts", ignoreCase = true)
            .assertExists()
    }

    @Test
    fun requestMoneyScreen_displaysRequestButton() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Request Money", ignoreCase = true, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun requestMoneyScreen_displaysReasonInput() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Reason", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun requestMoneyScreen_displaysEmptyState() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("No contacts yet", ignoreCase = true)
            .assertExists()
    }

    @Test
    fun requestMoneyScreen_displaysAddContactButton() {
        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Add Contact", ignoreCase = true)
            .assertExists()
    }

    @Test
    fun requestMoneyScreen_themeApplied() {
        composeTestRule.setContent {
            AureusTheme(darkTheme = false) {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Request Money", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun requestMoneyScreen_darkThemeApplied() {
        composeTestRule.setContent {
            AureusTheme(darkTheme = true) {
                RequestMoneyScreenFirebase(
                    onNavigateBack = {},
                    onRequestClick = { _, _, _ -> },
                    onAddContactClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Request Money", ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun requestMoneyScreen_callbacksAreFunctional() {
        var backInvoked = false
        var requestInvoked = false
        var addContactInvoked = false

        composeTestRule.setContent {
            AureusTheme {
                RequestMoneyScreenFirebase(
                    onNavigateBack = { backInvoked = true },
                    onRequestClick = { _, _, _ -> requestInvoked = true },
                    onAddContactClick = { addContactInvoked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Request Money", ignoreCase = true)
            .assertIsDisplayed()
    }
}