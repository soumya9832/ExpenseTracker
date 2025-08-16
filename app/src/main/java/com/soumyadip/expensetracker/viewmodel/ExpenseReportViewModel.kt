package com.soumyadip.expensetracker.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.soumyadip.expensetracker.data.model.Expense
import com.soumyadip.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ExpenseReportViewModel(private val expenseRepository: ExpenseRepository) : ViewModel() {
    // --- State for the UI ---

    // Summary Cards
    private val _sevenDayTotal = MutableStateFlow(0.0)
    val sevenDayTotal: StateFlow<Double> = _sevenDayTotal.asStateFlow()

    private val _dailyAverage = MutableStateFlow(0.0)
    val dailyAverage: StateFlow<Double> = _dailyAverage.asStateFlow()

    // Quick Insights
    private val _highestSpendingDay = MutableStateFlow<Expense?>(null)
    val highestSpendingDay: StateFlow<Expense?> = _highestSpendingDay.asStateFlow()

    private val _topCategory = MutableStateFlow<String?>(null)
    val topCategory: StateFlow<String?> = _topCategory.asStateFlow()

    // Chart Data
    private val _dailySpendingData = MutableStateFlow<Map<String, Double>>(emptyMap())
    val dailySpendingData: StateFlow<Map<String, Double>> = _dailySpendingData.asStateFlow()

    private val _categorySpendingAndCount = MutableStateFlow<Map<String, Pair<Double, Int>>>(emptyMap())
    val categorySpendingAndCount: StateFlow<Map<String, Pair<Double, Int>>> = _categorySpendingAndCount.asStateFlow()

    private val _sevenDaysDates = MutableStateFlow<List<String>>(emptyList())
    val sevenDaysDates: StateFlow<List<String>> = _sevenDaysDates.asStateFlow()


    init {
        fetchReportsData()
        generateSevenDaysLabels()
    }

    private fun fetchReportsData() {
        viewModelScope.launch {
            // Calculate start and end dates for the last 7 days
            val today = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val sevenDaysAgo = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -6)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startMillis = sevenDaysAgo.timeInMillis
            val endMillis = today.timeInMillis + TimeUnit.DAYS.toMillis(1) // Include today

            // Fetch all expenses for the last 7 days
            expenseRepository.getAllExpensesBetweenDates(startMillis, endMillis).collect { expenses ->
                _sevenDayTotal.value = expenses.sumOf { it.amount }
                _dailyAverage.value = _sevenDayTotal.value / 7

                // Calculate Top Category and Category-wise spending data and count
                val categoryMap = expenses.groupBy { it.category }
                val categoryData = categoryMap.mapValues { (_, value) ->
                    Pair(value.sumOf { it.amount }, value.size)
                }
                _categorySpendingAndCount.value = categoryData
                _topCategory.value = categoryData.maxByOrNull { it.value.first }?.key


                // Calculate Highest Spending Day and Daily Spending Data
                val dailyTotals = expenses.groupBy {
                    Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.DAY_OF_YEAR)
                }
                val dailySpendingMap = dailyTotals.mapValues { (_, value) ->
                    value.sumOf { it.amount }
                }

                // Note: The design needs labels for the chart, so we'll group by a formatted date
                val dateFormat = SimpleDateFormat("MMM d")
                val dailySpendingWithLabels = expenses.groupBy {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    dateFormat.format(cal.time)
                }.mapValues { (_, value) -> value.sumOf { it.amount } }

                _dailySpendingData.value = dailySpendingWithLabels

                val highestDayExpense = expenses.maxByOrNull { it.amount }
                _highestSpendingDay.value = highestDayExpense
            }
        }
    }

    private fun generateSevenDaysLabels() {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6) // Start from 6 days ago
        }
        repeat(7) {
            dates.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
        }
        _sevenDaysDates.value = dates
    }

    // Report Sharing Logics

    fun shareReport(context: Context) {
        val reportText = buildReportText()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Report")
        context.startActivity(shareIntent)
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            val fileName = "expense_report.csv"
            val file = File(context.cacheDir, fileName)
            try {
                PrintWriter(FileOutputStream(file)).use { writer ->
                    // Write CSV headers
                    writer.println("Category,Amount,Count")

                    // Write data
                    categorySpendingAndCount.value.forEach { (category, data) ->
                        writer.println("$category,${data.first},${data.second}")
                    }
                }
                shareFile(context, file, "text/csv")
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    fun exportToPdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = android.graphics.Paint()

            // Title
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("Expense Report", 50f, 80f, paint)

            // Summary
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("7-Day Total: ₹${"%.0f".format(sevenDayTotal.value)}", 50f, 120f, paint)
            canvas.drawText("Daily Average: ₹${"%.0f".format(dailyAverage.value)}", 50f, 140f, paint)

            // Category-wise list (simple drawing)
            paint.textSize = 14f
            canvas.drawText("Category-wise Spending:", 50f, 180f, paint)
            var yPos = 200f
            categorySpendingAndCount.value.forEach { (category, data) ->
                val text = "$category: ₹${"%.0f".format(data.first)} (${data.second} items)"
                canvas.drawText(text, 50f, yPos, paint)
                yPos += 20f
            }

            pdfDocument.finishPage(page)

            // Save and share
            val fileName = "expense_report.pdf"
            val file = File(context.cacheDir, fileName)
            try {
                pdfDocument.writeTo(FileOutputStream(file))
                shareFile(context, file, "application/pdf")
            } catch (e: Exception) {
                // Handle exceptions
            } finally {
                pdfDocument.close()
            }
        }
    }

    // --- Helper Functions ---

    private fun buildReportText(): String {
        val builder = StringBuilder()
        builder.append("📊 Expense Report\n\n")
        builder.append("Total Spending (Last 7 Days): ₹${"%.0f".format(sevenDayTotal.value)}\n")
        builder.append("Daily Average: ₹${"%.0f".format(dailyAverage.value)}\n\n")

        builder.append("Category-wise Spending:\n")
        categorySpendingAndCount.value.forEach { (category, data) ->
            builder.append("- $category: ₹${"%.0f".format(data.first)} (${data.second} items)\n")
        }
        builder.append("\nView more insights in the app!")
        return builder.toString()
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
    }


    // --- ViewModel Factory ---
    companion object {
        fun Factory(repository: ExpenseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ExpenseReportViewModel::class.java)) {
                        return ExpenseReportViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

}