package ru.aiss83.comunalexpenses2

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import ru.aiss83.comunalexpenses2.MainContent

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination="main") {
        composable(route = "main", ) { MainContent() }
        composable(route = "add_expenses") { AddExpensesView(modifier = Modifier) }
    }
}

