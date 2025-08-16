package com.soumyadip.expensetracker.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.soumyadip.expensetracker.ui.dialog.SuccessDialog
import com.soumyadip.expensetracker.viewmodel.ExpenseEntryViewModel


// Data class to hold category information and its color
data class ExpenseCategory(val name: String, val color: Color)

@Composable
fun ExpenseEntryScreen(
    navController: NavController,
    expenseEntryViewModel: ExpenseEntryViewModel = viewModel()
){
    val title by expenseEntryViewModel.expenseTitle.collectAsState()
    val amount by expenseEntryViewModel.expenseAmount.collectAsState()
    val category by expenseEntryViewModel.expenseCategory.collectAsState()
    val notes by expenseEntryViewModel.expenseNotes.collectAsState()
    val totalSpentToday by expenseEntryViewModel.totalSpentToday.collectAsState()

    val isTitleError by expenseEntryViewModel.isTitleError.collectAsState()
    val isAmountError by expenseEntryViewModel.isAmountError.collectAsState()

    val context = LocalContext.current
    val imageUri by expenseEntryViewModel.imageUri.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        expenseEntryViewModel.setImageUri(uri)
    }

    // New state variables for the success dialog
    val showSuccessMessage by expenseEntryViewModel.showSuccessMessage.collectAsState()
    val successMessageText by expenseEntryViewModel.successMessageText.collectAsState()


    Box(
        modifier = Modifier.fillMaxSize().padding(top = 12.dp)
    ){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())

    ) {
        // Top Section (Total Spent Today)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(horizontal = 24.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF80DEEA), Color(0xFF4DB6AC))
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Total Spent Today",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "₹${"%.0f".format(totalSpentToday)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Expense Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Expense Details",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { expenseEntryViewModel.onTitleChange(it) },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isTitleError,
                    singleLine = true,
                    supportingText = {
                        if (isTitleError) {
                            Text("Required")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Text
                OutlinedTextField(
                    value = amount,
                    onValueChange = { expenseEntryViewModel.onAmountChange(it) },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isTitleError,
                    singleLine = true,
                    supportingText = {
                        if (isAmountError) {
                            Text("Required")
                        }
                    }

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Dropdown
                CategoryDropdown(
                    selectedCategory = category,
                    onCategorySelected = { expenseEntryViewModel.onCategoryChange(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { expenseEntryViewModel.onNotesChange(it) },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    supportingText = {
                        Text("${notes.length}/100 characters")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Upload Section (Mocked)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
                        .padding(16.dp)
                        .clickable {
                            // Launch the gallery when the Box is clicked
                            launcher.launch("image/*")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    if (imageUri == null) {
                        // Show placeholder when no image is selected
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Upload Receipt",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Upload receipt image",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        // Show the selected image using Coil

                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Receipt Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )

                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                // Add Expense Button
                Button(
                    onClick = {
                        imageUri?.let { uri ->
                            expenseEntryViewModel.saveImageFromUri(context, uri)
                        }
                        expenseEntryViewModel.addExpense()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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
                    contentPadding = PaddingValues(16.dp) // Add padding to make the button look taller
                ) {
                    Text("Add Expense")
                }


            }
        }
    }
        // Dialog placed on top of the main content
        SuccessDialog(message = successMessageText, showSuccessMessage)


    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        ExpenseCategory("Staff", Color(0xFF00BCD4)),
        ExpenseCategory("Travel", Color(0xFF4CAF50)),
        ExpenseCategory("Food", Color(0xFFFFC107)),
        ExpenseCategory("Utility", Color(0xFFF44336))
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (selectedCategory.isBlank()) "Select category" else selectedCategory,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },

            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(color = Color.White)
        ) {
            categories.forEach { category ->
                val isSelected = category.name == selectedCategory

                // Use a modifier to set the background color directly
                val itemBackgroundColor = if (isSelected) Color(0xFFB2DFDB) else Color.White

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(category.color)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = category.name, color = Color.Black)
                        }
                    },
                    onClick = {
                        onCategorySelected(category.name)
                        expanded = false
                    },
                    modifier = Modifier.background(itemBackgroundColor)
                )
            }
        }
    }
}