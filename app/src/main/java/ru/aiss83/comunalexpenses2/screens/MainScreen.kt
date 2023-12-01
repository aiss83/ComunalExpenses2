package ru.aiss83.comunalexpenses2.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import ru.aiss83.comunalexpenses2.AddExpensesView
import ru.aiss83.comunalexpenses2.MainContent
import ru.aiss83.comunalexpenses2.NavRoutes
import ru.aiss83.comunalexpenses2.ResourcesDataViewModel

@Composable
fun MainScreen(viewModel: ResourcesDataViewModel) {
    val allResourcesData by viewModel.allResourcesData.observeAsState(initial = listOf())
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination= NavRoutes.Home.route) {
        composable(route = NavRoutes.Home.route) {
            MainContent(modifier = Modifier,
                allResourceData = allResourcesData,
                viewModel = viewModel,
                onNavigateToAddExpenses = {
                    navController.navigate(NavRoutes.EditResources.route) {
                        popUpTo(NavRoutes.Home.route)
                    }
                })
        }
        composable(route = NavRoutes.EditResources.route) {
            AddExpensesView(modifier = Modifier.fillMaxSize())
        }
        composable(route = NavRoutes.Settings.route) {
            SettingsScreen()
        }
    }
}

