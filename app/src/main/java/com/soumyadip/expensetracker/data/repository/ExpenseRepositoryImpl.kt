package com.soumyadip.expensetracker.data.repository

import com.soumyadip.expensetracker.data.local.ExpenseDao
import com.soumyadip.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepositoryImpl(private val expenseDao: ExpenseDao) : ExpenseRepository {
    override suspend fun addExpense(expense: Expense) {
        expenseDao.insert(expense)
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
    }

    override fun getAllExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return expenseDao.getAllExpensesBetweenDates(startDate,endDate)
    }

    override fun getTotalSpentBetweenDates(startDate: Long, endDate: Long): Flow<Double?> {
        return expenseDao.getTotalSpentBetweenDates(startDate,endDate)
    }
}