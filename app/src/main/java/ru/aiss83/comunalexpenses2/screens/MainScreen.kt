package ru.aiss83.comunalexpenses2.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import ru.aiss83.comunalexpenses2.NavRoutes
import ru.aiss83.comunalexpenses2.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.SettingsViewModel

@Composable
fun MainScreen(viewModel: ResourcesDataViewModel, settingsModel: SettingsViewModel) {
    val allResourcesData by viewModel.allResourcesData.observeAsState(initial = listOf())
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination= NavRoutes.Home.route) {
        composable(route = NavRoutes.Home.route) {
            HomeScreen(
                allResourceData = allResourcesData,
                viewModel = viewModel,
                onNavigateToAddExpenses = {
                    navController.navigate(NavRoutes.EditResources.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                })
        }
        composable(route = NavRoutes.EditResources.route) {
            AddResourcesDataScreen(viewModel = viewModel, navController = navController)
        }
        composable(route = NavRoutes.Settings.route) {
            SettingsScreen(viewModel = settingsModel, navHostController = navController)
        }
    }
}

