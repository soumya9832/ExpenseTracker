package com.soumyadip.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val title : String,
    val amount :  Double,
    val category : String,
    val date : Long,
    val notes: String?,
    val receiptImagePath: String?
)
