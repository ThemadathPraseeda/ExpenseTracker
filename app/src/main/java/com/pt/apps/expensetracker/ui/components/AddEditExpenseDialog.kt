package com.pt.apps.expensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pt.apps.expensetracker.data.model.Categories
import com.pt.apps.expensetracker.data.model.Expense
import com.pt.apps.expensetracker.domain.ExpenseValidator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseDialog(
    initialExpense: Expense? = null,
    availableTags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        category: String,
        date: Long,
        tags: String,
        note: String,
        paymentMethod: String
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialExpense != null

    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var amountText by remember { mutableStateOf(initialExpense?.let { String.format(Locale.US, "%.2f", it.amount) } ?: "") }
    var selectedCategory by remember { mutableStateOf(initialExpense?.category ?: Categories.FOOD.name) }
    var dateMillis by remember { mutableStateOf(initialExpense?.date ?: System.currentTimeMillis()) }
    var tagsText by remember { mutableStateOf(initialExpense?.tags ?: "") }
    var note by remember { mutableStateOf(initialExpense?.note ?: "") }
    var paymentMethod by remember { mutableStateOf(initialExpense?.paymentMethod ?: "Card") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    fun validateForm(): Boolean {
        val tResult = ExpenseValidator.validateTitle(title)
        val aResult = ExpenseValidator.validateAmount(amountText)
        val cResult = ExpenseValidator.validateCategory(selectedCategory)

        titleError = tResult.errorMessage
        amountError = aResult.errorMessage
        categoryError = cResult.errorMessage

        return tResult.isValid && aResult.isValid && cResult.isValid
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_expense_dialog"),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Expense" else "Add New Expense",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_expense_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Form Fields inside scrollable column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Input
                    Column {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                if (hasAttemptedSubmit) {
                                    titleError = ExpenseValidator.validateTitle(it).errorMessage
                                }
                            },
                            label = { Text("Expense Title *") },
                            placeholder = { Text("e.g. Groceries at Trader Joe's") },
                            singleLine = true,
                            isError = titleError != null,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_title_input")
                        )
                        if (titleError != null) {
                            Text(
                                text = titleError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("expense_title_error")
                            )
                        }
                    }

                    // Amount Input
                    Column {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                if (hasAttemptedSubmit) {
                                    amountError = ExpenseValidator.validateAmount(it).errorMessage
                                }
                            },
                            label = { Text("Amount ($) *") },
                            placeholder = { Text("0.00") },
                            leadingIcon = {
                                Text(
                                    text = "$",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            singleLine = true,
                            isError = amountError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_amount_input")
                        )
                        if (amountError != null) {
                            Text(
                                text = amountError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("expense_amount_error")
                            )
                        }
                    }

                    // Category Selector
                    Column {
                        Text(
                            text = "Category *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Categories.list.forEach { category ->
                                val isSelected = selectedCategory.equals(category.name, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategory = category.name
                                        categoryError = null
                                    },
                                    label = {
                                        Text(category.name, fontSize = 12.sp)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else category.color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("category_chip_${category.name}")
                                )
                            }
                        }
                        if (categoryError != null) {
                            Text(
                                text = categoryError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }

                    // Date Picker Trigger
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                            }
                                            dateMillis = newCal.timeInMillis
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .testTag("expense_date_picker_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Pick Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dateFormat.format(Date(dateMillis)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Payment Method Selector
                    Column {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val methods = listOf("Card", "Cash", "UPI/Transfer", "Bank")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            methods.forEach { method ->
                                val isSelected = paymentMethod.equals(method, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { paymentMethod = method },
                                    label = { Text(method) }
                                )
                            }
                        }
                    }

                    // Tags Input + Quick Tag Chips
                    Column {
                        OutlinedTextField(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            label = { Text("Tags (comma-separated)") },
                            placeholder = { Text("e.g. food, vacation, work") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_tags_input")
                        )

                        if (availableTags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Existing Tags (tap to add):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableTags.take(8).forEach { tag ->
                                    val currentTagList = tagsText.split(",").map { it.trim().lowercase(Locale.ROOT) }
                                    val isPresent = currentTagList.contains(tag.lowercase(Locale.ROOT))
                                    SuggestionChip(
                                        onClick = {
                                            if (!isPresent) {
                                                tagsText = if (tagsText.isBlank()) tag else "$tagsText, $tag"
                                            }
                                        },
                                        label = { Text("#$tag") }
                                    )
                                }
                            }
                        }
                    }

                    // Note Input
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("Add any details or description...") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_notes_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_expense_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            hasAttemptedSubmit = true
                            if (validateForm()) {
                                val cleanAmount = amountText.trim().replace("$", "").replace(",", "").toDouble()
                                onSave(
                                    title,
                                    cleanAmount,
                                    selectedCategory,
                                    dateMillis,
                                    tagsText,
                                    note,
                                    paymentMethod
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_expense_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditing) "Save Changes" else "Add Expense")
                    }
                }
            }
        }
    }
}
