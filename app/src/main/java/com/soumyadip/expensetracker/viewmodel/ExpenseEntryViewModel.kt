package com.soumyadip.expensetracker.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soumyadip.expensetracker.data.model.Expense
import com.soumyadip.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class ExpenseEntryViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {

    // UI state for the entry form
    private val _expenseTitle = MutableStateFlow("")
    val expenseTitle : StateFlow<String> = _expenseTitle

    private val _expenseAmount = MutableStateFlow("")
    val expenseAmount : StateFlow<String> = _expenseAmount

    private val _expenseCategory = MutableStateFlow("Food")
    val expenseCategory : StateFlow<String> = _expenseCategory

    private val _expenseNotes = MutableStateFlow("")
    val expenseNotes: StateFlow<String> = _expenseNotes

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()

    private val _receiptImagePath = MutableStateFlow<String?>(null)
    val receiptImagePath = _receiptImagePath.asStateFlow()

    // State for total spent today
    private val _totalSpentToday = MutableStateFlow(0.0)
    val totalSpentToday: StateFlow<Double> = _totalSpentToday

    // Validation state
    private val _isTitleError = MutableStateFlow(false)
    val isTitleError: StateFlow<Boolean> = _isTitleError

    private val _isAmountError = MutableStateFlow(false)
    val isAmountError: StateFlow<Boolean> = _isAmountError

    private val _showSuccessMessage = MutableStateFlow(false)
    val showSuccessMessage: StateFlow<Boolean> = _showSuccessMessage.asStateFlow()

    private val _successMessageText = MutableStateFlow("")
    val successMessageText: StateFlow<String> = _successMessageText.asStateFlow()


    init {
        // Fetch total spent today when the ViewModel is created
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 86400000

            expenseRepository.getTotalSpentBetweenDates(startOfDay, endOfDay).collect{ total ->
                _totalSpentToday.value = total ?: 0.0
            }


        }
    }

    fun onTitleChange(title: String) {
        _expenseTitle.value = title
        _isTitleError.value =  title.isBlank()
    }

    fun onAmountChange(amount: String) {
        _expenseAmount.value = amount
        _isAmountError.value = amount.toDoubleOrNull() == null || amount.toDouble() <= 0.0
    }

    fun onCategoryChange(category: String) {
        _expenseCategory.value = category
    }

    fun onNotesChange(notes: String) {
        if (notes.length <= 100) {
            _expenseNotes.value = notes
        }
    }

    fun addExpense() {
        val title = _expenseTitle.value
        val amount = _expenseAmount.value.toDoubleOrNull() ?: 0.0
        val category = _expenseCategory.value
        val notes = _expenseNotes.value
        val receiptImagePath = _receiptImagePath.value
        val currentDate = System.currentTimeMillis()

        _isTitleError.value = title.isBlank()
        _isAmountError.value = amount <= 0.0

        val hasError = _isTitleError.value || _isAmountError.value

        if (hasError) {
            return
        }

        val newExpense = Expense(
            title = title,
            amount = amount,
            category = category,
            date = currentDate,
            notes = notes.takeIf { it.isNotBlank() },
            receiptImagePath = receiptImagePath
        )

        viewModelScope.launch {
            expenseRepository.addExpense(newExpense)

            // Show the animated dialog on success
            _successMessageText.value = "₹${"%.0f".format(newExpense.amount)} ${newExpense.category} expense recorded"
            _showSuccessMessage.value = true

            // Reset input fields after successful entry
            _expenseTitle.value = ""
            _expenseAmount.value = ""
            _expenseNotes.value = ""
            _isTitleError.value = false
            _isAmountError.value = false
            _receiptImagePath.value = null

            // Hide the message after a short duration
            delay(3000) // 3 seconds
            _showSuccessMessage.value = false
        }
    }

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
    }

    // You will also need to add a function to save the image to internal storage.
// This is a simplified function and you'll need to call it from your Composable.
    fun saveImageFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Update the ViewModel's state with the new permanent path
            _receiptImagePath.value = file.absolutePath
        }
    }

    /**
     * Factory for [ExpenseEntryViewModel] that takes [ExpenseRepository] as a dependency
     */
    companion object {
        fun Factory(repository: ExpenseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ExpenseEntryViewModel::class.java)) {
                        return ExpenseEntryViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }



}