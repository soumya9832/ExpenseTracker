package com.soumyadip.expensetracker.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soumyadip.expensetracker.data.model.Expense
import com.soumyadip.expensetracker.viewmodel.ExpenseListViewModel
import com.soumyadip.expensetracker.viewmodel.GroupBy
import com.soumyadip.expensetracker.viewmodel.SortBy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.soumyadip.expensetracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    expenseListViewModel : ExpenseListViewModel = viewModel(),
    onAddFirstExpenseClicked : () -> Unit
){

    val totalCount by expenseListViewModel.totalExpenseCount.collectAsState()
    val totalAmount by expenseListViewModel.totalExpenseAmount.collectAsState()
    val expenses by expenseListViewModel.expenses.collectAsState()
    val selectedDate by expenseListViewModel.selectedDate.collectAsState()
    val groupBy by expenseListViewModel.groupBy.collectAsState()
    val sortBy by expenseListViewModel.sortBy.collectAsState()

    val expensesForDisplay by expenseListViewModel.expensesForDisplay.collectAsState()

    // State to control the visibility of the date picker dialog
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // If the state is true, display the dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            // Set initial date from ViewModel
            initialSelectedDateMillis = selectedDate.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = {
                showDatePickerDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                        // Convert milliseconds to a Date object and update ViewMode
                        val newDate = Date(selectedMillis)

                        expenseListViewModel.onDateSelected(newDate)
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Summary Cards Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(vertical = 12.dp)
            ,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryCard("Total Count", totalCount.toString(), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            SummaryCard("Total Amount", "₹${"%.0f".format(totalAmount)}", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter & Sort Buttons Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date Picker Button
            DateButton(
                date = selectedDate.time,
                onClick = { showDatePickerDialog = true }
            )
            // Group By Toggle Button
            FilterButton(
                text = if (groupBy == GroupBy.CATEGORY) "Group by Category" else "Group by Time",
                iconId = R.drawable.outline_filter_alt_24,
                onClick = expenseListViewModel::onToggleGroupBy
            )
            // Sort By Toggle Button
            FilterButton(
                text = if (sortBy == SortBy.AMOUNT) "Sort by Amount" else "Sort by Date",
                iconId = R.drawable.outline_sort_24,
                onClick = expenseListViewModel::onToggleSortBy
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expense List or Empty State
        if (expenses.isEmpty()) {
            EmptyState(onAddFirstExpenseClicked, selectedDate.time)
        } else {
            // Conditionally show the list based on the group by state
            when (groupBy) {
                GroupBy.CATEGORY -> {
                    expensesForDisplay.forEach { (category, expenses) ->
                        GroupExpenseListItem(category, expenses)
                    }
                }
                GroupBy.TIME -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Loop through the flattened list of expenses
                            expensesForDisplay.values.flatten().forEachIndexed { index, expense ->
                                ExpenseListItem(expense = expense)
                                // Add a Spacer between items, but not after the last item
                                if (index < expensesForDisplay.values.flatten().size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF80DEEA), Color(0xFF4DB6AC))
                ),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun DateButton(date: java.util.Date, onClick: () -> Unit) {
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    OutlinedButton(onClick = onClick) {
        Icon(Icons.Default.DateRange, contentDescription = "Select Date", modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(formatter.format(date), fontSize = 14.sp)
    }
}

@Composable
fun FilterButton(text: String, iconId: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(painter = painterResource(id = iconId), contentDescription = text, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun EmptyState(onAddFirstExpenseClicked: () -> Unit, selectedDate: Date) {
    val formattedDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(vertical = 12.dp)
        ,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "No expenses found",
                modifier = Modifier.size(72.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No expenses found", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No expenses recorded for\n$formattedDate", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onAddFirstExpenseClicked,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF80DEEA), Color(0xFF4DB6AC))
                        ),
                        shape = MaterialTheme.shapes.small // Use the default button shape
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Make the button transparent to show the gradient
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(16.dp)

            ) {
                Text("Add First Expense")
            }
        }
    }
}

@Composable
fun ExpenseListItem(expense: Expense) {
    val categoryColor = when (expense.category) {
        "Staff" -> Color(0xFF00BCD4)
        "Travel" -> Color(0xFF4CAF50)
        "Food" -> Color(0xFFFFC107)
        "Utility" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(0.5.dp,Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundedGrayBackground(
                        modifier = Modifier.width(60.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = expense.category,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }

                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(expense.date)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = "₹${expense.amount}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun GroupExpenseListItem(category: String, expenses: List<Expense>){
    val categoryColor = when (category) {
        "Staff" -> Color(0xFF00BCD4)
        "Travel" -> Color(0xFF4CAF50)
        "Food" -> Color(0xFFFFC107)
        "Utility" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))

            RoundedGrayBackground(modifier = Modifier.width(40.dp)){
                Text(
                    text = " ${expenses.size} items",
                    fontSize = 10.sp,
                    color = Color.Black,
                )
            }


        }

        Spacer(modifier = Modifier.height(16.dp))

        expenses.forEach { expense ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ){
                ExpenseListItem(expense = expense)
                Spacer(modifier = Modifier.height(8.dp))
            }

        }

    }
}

@Composable
fun RoundedGrayBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp), // Adjust the corner radius as needed
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(0.5f) // Light gray color
        )
    ) {
        content()
    }
}