package com.example.aureus.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.aureus.ui.cards.MyCardsScreen
import com.example.aureus.ui.home.HomeScreen
import com.example.aureus.ui.profile.SettingsScreen
import com.example.aureus.ui.statistics.StatisticsScreen
import com.example.aureus.ui.theme.*
import com.example.aureus.ui.theme.ThemeManager
import androidx.compose.material.icons.automirrored.filled.*
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import com.example.aureus.di.ThemeManagerEntryPoint
import kotlinx.coroutines.launch

import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext

/**
 * Main Screen with Bottom Navigation - Version démo statique
 * Centralizes navigation between main app sections
 */
@Composable
fun MainScreen(
    navController: NavController,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToRequestMoney: () -> Unit = {},
    onNavigateToAddCard: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToContacts: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ThemeManagerEntryPoint::class.java
        ).themeManager()
    }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            MainBottomNavigation(
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigateToStatistics = { selectedTab = 1 },
                    onNavigateToCards = { selectedTab = 2 },
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToSendMoney = onNavigateToSendMoney,
                    onNavigateToRequestMoney = onNavigateToRequestMoney,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToNotifications = onNavigateToNotifications
                )
                1 -> StatisticsScreen(
                    onNavigateBack = { selectedTab = 0 }
                )
                2 -> MyCardsScreen(
                    navController = navController,
                    onNavigateBack = { selectedTab = 0 },
                    onAddCard = onNavigateToAddCard
                )
                3 -> SettingsScreen(
                    onNavigateBack = { selectedTab = 0 },
                    onLogout = onLogout,
                    onContacts = onNavigateToContacts,
                    onCategories = {}
                )
            }
        }
    }
}

@Composable
private fun MainBottomNavigation(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedIndex == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = Icons.Default.BarChart,
                label = "Stats",
                isSelected = selectedIndex == 1,
                onClick = { onTabSelected(1) }
            )
            BottomNavItem(
                icon = Icons.Default.CreditCard,
                label = "Cards",
                isSelected = selectedIndex == 2,
                onClick = { onTabSelected(2) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = selectedIndex == 3,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) SecondaryGold else NeutralMediumGray,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(SecondaryGold)
            )
        }
    }
}
