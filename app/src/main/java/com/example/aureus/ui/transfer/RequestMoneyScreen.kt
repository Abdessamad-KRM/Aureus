package com.example.aureus.ui.transfer

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aureus.domain.model.Contact
import com.example.aureus.ui.contact.viewmodel.ContactViewModel

/**
 * Request Money Screen - Entry Point
 * Delegates to Firebase-based implementation
 * Maintains backward compatibility with existing navigation
 */
@Composable
fun RequestMoneyScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onRequestClick: (Contact, Double, String) -> Unit = { _, _, _ -> },
    onAddContactClick: () -> Unit = {}
) {
    // Delegate to Firebase implementation
    RequestMoneyScreenFirebase(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onRequestClick = onRequestClick,
        onAddContactClick = onAddContactClick
    )
}