package com.purrcare.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.purrcare.ui.screen.DailyLogScreen
import com.purrcare.ui.screen.HomeScreen
import com.purrcare.ui.screen.MedicationScreen
import com.purrcare.ui.screen.ProfileScreen
import com.purrcare.ui.viewmodel.CatProfileViewModel
import com.purrcare.ui.viewmodel.DailyLogViewModel
import com.purrcare.ui.viewmodel.HomeViewModel
import com.purrcare.ui.viewmodel.MedicationViewModel

@Composable
fun PurrCareNavHost(
    catProfileViewModel: CatProfileViewModel,
    dailyLogViewModel: DailyLogViewModel,
    medicationViewModel: MedicationViewModel,
    homeViewModel: HomeViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedCat by catProfileViewModel.selectedCat.collectAsStateWithLifecycle()
    val recentLogs by homeViewModel.recentLogs.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                LaunchedEffect(selectedCat?.id) {
                    selectedCat?.id?.let { homeViewModel.initialize(it) }
                }
                HomeScreen(
                    catProfile = selectedCat,
                    recentLogs = recentLogs,
                    onEditProfile = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = catProfileViewModel,
                    catProfile = selectedCat,
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Log.route) {
                LaunchedEffect(selectedCat?.id) {
                    selectedCat?.id?.let { dailyLogViewModel.initialize(it) }
                }
                DailyLogScreen(
                    viewModel = dailyLogViewModel,
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Medication.route) {
                LaunchedEffect(selectedCat?.id) {
                    selectedCat?.id?.let { medicationViewModel.initialize(it) }
                }
                MedicationScreen(viewModel = medicationViewModel)
            }
        }
    }
}
