package com.example.aureus.ui.auth.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.aureus.ui.theme.AureusTheme

/**
 * Preview for SMS Verification Screen
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SmsVerificationScreenPreview() {
    AureusTheme {
        SmsVerificationScreen(
            phoneNumber = "+212 6 12 34 56 78",
            onVerificationSuccess = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Different Phone")
@Composable
fun SmsVerificationScreenPreview2() {
    AureusTheme {
        SmsVerificationScreen(
            phoneNumber = "+33 6 98 76 54 32",
            onVerificationSuccess = {},
            onNavigateBack = {}
        )
    }
}
