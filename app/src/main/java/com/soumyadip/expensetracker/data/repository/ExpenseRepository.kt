package com.soumyadip.expensetracker.data.repository

import com.soumyadip.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    suspend fun addExpense(expense: Expense)

    fun getAllExpenses(): Flow<List<Expense>>

    fun getAllExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>>

    fun getTotalSpentBetweenDates(startDate: Long, endDate: Long): Flow<Double?>
}