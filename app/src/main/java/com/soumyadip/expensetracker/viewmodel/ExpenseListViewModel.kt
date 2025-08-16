package com.soumyadip.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soumyadip.expensetracker.data.model.Expense
import com.soumyadip.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseListViewModel(private val expenseRepository: ExpenseRepository) : ViewModel(){

    // States for UI
    private val _totalExpenseCount = MutableStateFlow(0)
    val totalExpenseCount: StateFlow<Int> = _totalExpenseCount.asStateFlow()

    private val _totalExpenseAmount = MutableStateFlow(0.0)
    val totalExpenseAmount: StateFlow<Double> = _totalExpenseAmount.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    // Filter & Sort States
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _groupBy = MutableStateFlow(GroupBy.CATEGORY)
    val groupBy: StateFlow<GroupBy> = _groupBy.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.DATE)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    // Combined Flow for UI data
    val expensesForDisplay: StateFlow<Map<String, List<Expense>>> =
        combine(_expenses, _groupBy, _sortBy) { expenses, groupBy, sortBy ->
            val sortedList = when (sortBy) {
                SortBy.AMOUNT -> expenses.sortedByDescending { it.amount }
                SortBy.DATE -> expenses.sortedByDescending { it.date }
            }

            when (groupBy) {
                GroupBy.CATEGORY -> {
                    sortedList.groupBy { it.category }
                }
                GroupBy.TIME -> {
                    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    sortedList.groupBy { "Time" + dateFormat.format(Date(it.date)) }
                    // Note: Grouping by time for a single day is a bit redundant.
                    // This will be simpler in the UI just to show a flat list.
                    // We'll handle this directly in the screen UI.
                    sortedList.groupBy { "All Expenses" } // A simple grouping for the time view
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )



    init {
        // Fetch data for the current date on initialization
        fetchExpensesByDate()
    }

    // Functions to fetch and refresh data
    private fun fetchExpensesByDate() {
        val tempCalendar = Calendar.getInstance().apply {
            time = _selectedDate.value.time // Use a temporary instance based on the current state
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = tempCalendar.timeInMillis
        tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = tempCalendar.timeInMillis

        viewModelScope.launch {
            expenseRepository.getAllExpensesBetweenDates(startOfDay, endOfDay).collectLatest { fetchedExpenses ->
                _expenses.value = fetchedExpenses
                _totalExpenseCount.value = fetchedExpenses.size
            }
        }
        viewModelScope.launch {
            expenseRepository.getTotalSpentBetweenDates(startOfDay, endOfDay).collectLatest { totalAmount ->
                _totalExpenseAmount.value = totalAmount ?: 0.0
            }
        }
    }

    // UI event handlers
    fun onDateSelected(newDate: Date) {
        _selectedDate.value = Calendar.getInstance().apply { time = newDate }
        fetchExpensesByDate()
    }

    fun onToggleGroupBy() {
        _groupBy.value = when (_groupBy.value) {
            GroupBy.CATEGORY -> GroupBy.TIME
            GroupBy.TIME -> GroupBy.CATEGORY
        }
    }

    fun onToggleSortBy() {
        _sortBy.value = when (_sortBy.value) {
            SortBy.AMOUNT -> SortBy.DATE
            SortBy.DATE -> SortBy.AMOUNT
        }
    }


    companion object {
        fun Factory(repository: ExpenseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ExpenseListViewModel::class.java)) {
                        return ExpenseListViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }


}

// Enums to represent the toggle states
enum class GroupBy {
    CATEGORY, TIME
}

enum class SortBy {
    AMOUNT, DATE
}