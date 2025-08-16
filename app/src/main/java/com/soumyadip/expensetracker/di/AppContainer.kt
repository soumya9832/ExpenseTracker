package com.soumyadip.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.soumyadip.expensetracker.data.local.AppDatabase
import com.soumyadip.expensetracker.data.repository.ExpenseRepository
import com.soumyadip.expensetracker.data.repository.ExpenseRepositoryImpl

interface AppContainer {
    val expenseRepository: ExpenseRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "expense_tracker_database"
        ).build()
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }
}