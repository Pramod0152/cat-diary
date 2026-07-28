package com.petwell.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Profile : Screen(
        route = "profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Pets,
        unselectedIcon = Icons.Outlined.Pets
    )

    data object Log : Screen(
        route = "log",
        label = "Log",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    data object Reminders : Screen(
        route = "reminders",
        label = "Reminders",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )
}

val bottomNavItems = listOf(Screen.Home, Screen.Profile, Screen.Log, Screen.Reminders)
