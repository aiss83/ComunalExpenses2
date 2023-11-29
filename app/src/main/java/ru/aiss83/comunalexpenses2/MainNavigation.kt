package ru.aiss83.comunalexpenses2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import ru.aiss83.comunalexpenses2.MainContent

@Composable
fun MainNavigation(viewModel: ResourcesDataViewModel) {
    val allResourcesData by viewModel.allResourcesData.observeAsState(initial = listOf())
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination="main") {
        composable(route = "main", ) { MainContent(modifier = Modifier, onNavigateToAddExpenses = {
            navController.navigate("add_expenses")
        }) }
        composable(route = "add_expenses") { AddExpensesView(modifier = Modifier.fillMaxSize()) }
    }
}

