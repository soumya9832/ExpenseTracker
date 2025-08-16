package com.soumyadip.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.soumyadip.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("Select * from expenses order by date desc")
    fun getAllExpenses() : Flow<List<Expense>>

    @Query("select * from expenses where date between :startDate and :endDate order by date desc")
    fun getAllExpensesBetweenDates(startDate:  Long, endDate: Long): Flow<List<Expense>>

    @Query("select sum(amount) from expenses where date between :startDate and :endDate ")
    fun getTotalSpentBetweenDates(startDate: Long, endDate: Long): Flow<Double?>


}