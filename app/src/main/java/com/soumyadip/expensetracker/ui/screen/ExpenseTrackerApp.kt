package com.soumyadip.expensetracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soumyadip.expensetracker.R
import com.soumyadip.expensetracker.data.repository.ExpenseRepository
import com.soumyadip.expensetracker.viewmodel.ExpenseEntryViewModel
import com.soumyadip.expensetracker.viewmodel.ExpenseListViewModel
import com.soumyadip.expensetracker.viewmodel.ExpenseReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(repository: ExpenseRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CustomTopAppBar(navController = navController, currentRoute = currentRoute)
        },
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "expense_entry",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("expense_entry") {
                val viewModel: ExpenseEntryViewModel = viewModel(
                    factory = ExpenseEntryViewModel.Factory(repository)
                )
                ExpenseEntryScreen(navController = navController, expenseEntryViewModel = viewModel)
            }

            composable("expense_list") {
                // Instantiate the ExpenseListViewModel here
                val viewModel: ExpenseListViewModel = viewModel(
                    factory = ExpenseListViewModel.Factory(repository)
                )
                ExpenseListScreen(
                    expenseListViewModel = viewModel,
                    onAddFirstExpenseClicked = { navController.navigate("expense_entry") }
                )
            }

            composable("expense_report") {
                // Instantiate the ExpenseListViewModel here
                val viewModel: ExpenseReportViewModel = viewModel(
                    factory = ExpenseReportViewModel.Factory(repository)
                )
                ExpenseReportScreen(
                    expenseReportViewModel = viewModel
                )
            }


            // Add other screens here as we build them
            // composable("expense_list") { ... }
            // composable("expense_report") { ... }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(navController: NavController, currentRoute: String?) {
    val showBackButton = currentRoute != "expense_entry" // Only show on other screens
    val title = when (currentRoute) {
        "expense_entry" -> "Add Expense"
        "expense_list" -> "All Expenses"
        "expense_report" -> "Reports"
        else -> "Add Expense"
    }

    TopAppBar(
        title = { Text(title, color = Color.White, fontSize = 20.sp) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF00BCD4), Color(0xFF009688))
            )
        )
    )
}


@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
            label = { Text("Add") },
            selected = currentRoute == "expense_entry",
            onClick = {
                navController.navigate("expense_entry") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Expenses") },
            label = { Text("Expenses") },
            selected = currentRoute == "expense_list",
            onClick = {
                navController.navigate("expense_list") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(painter = painterResource(R.drawable.outline_finance_24), contentDescription = "Reports") },
            label = { Text("Reports") },
            selected = currentRoute == "expense_report",
            onClick = {
                navController.navigate("expense_report") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}