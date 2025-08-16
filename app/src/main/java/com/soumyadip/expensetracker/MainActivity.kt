package com.soumyadip.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.soumyadip.expensetracker.di.AppContainer
import com.soumyadip.expensetracker.di.AppDataContainer
import com.soumyadip.expensetracker.ui.screen.ExpenseTrackerApp
import com.soumyadip.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppDataContainer(this)

        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerApp(appContainer.expenseRepository)
                }
            }
        }
    }
}