package com.soumyadip.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.soumyadip.expensetracker.data.model.Expense

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
     abstract fun expenseDao() : ExpenseDao
}