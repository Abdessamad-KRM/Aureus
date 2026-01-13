package com.example.aureus.ui.transfer

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Contact
import com.example.aureus.ui.contact.viewmodel.ContactViewModel

/**
 * Send Money Screen - Entry Point
 * Delegates to Firebase-based implementation
 * Maintains backward compatibility with existing navigation
 */
@Composable
fun SendMoneyScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onSendClick: (Contact, Double) -> Unit = { _, _ -> },
    onAddContactClick: () -> Unit = {}
) {
    // Delegate to Firebase implementation
    SendMoneyScreenFirebase(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onSendClick = onSendClick,
        onAddContactClick = onAddContactClick
    )
}