package com.pt.apps.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pt.apps.expensetracker.data.model.Budget
import com.pt.apps.expensetracker.data.model.Categories
import com.pt.apps.expensetracker.domain.ExpenseValidator
import java.util.Locale

@Preview
@Composable
fun AddEditBudgetDialog(
    initialBudget: Budget? = null,
    onDismiss: () -> Unit = {},
   // onSave: (category: String, limit: Double, alertThresholdPercent: Int) -> Unit = {} as (category: String, limit: Double, alertThresholdPercent: Int) -> Unit
) {
    val isEditing = initialBudget != null
    var selectedCategory by remember { mutableStateOf(initialBudget?.category ?: Budget.OVERALL_CATEGORY) }
    var limitText by remember { mutableStateOf(initialBudget?.let { String.format(Locale.US, "%.2f", it.monthlyLimit) } ?: "") }
    var thresholdPercent by remember { mutableIntStateOf(initialBudget?.alertThresholdPercent ?: 80) }

    var limitError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("add_edit_budget_dialog"),
            color = colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Budget" else "Set Budget Limit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_budget_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = colorScheme.outlineVariant
                )

                // Category Selection (Overall vs Specific Category)
                if (!isEditing) {
                    Column {
                        Text(
                            text = "Budget Target *",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = selectedCategory == Budget.OVERALL_CATEGORY,
                                onClick = { selectedCategory = Budget.OVERALL_CATEGORY },
                                label = { Text("⭐ Overall Monthly") },
                                modifier = Modifier.testTag("budget_target_overall")
                            )

                            Categories.list.forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category.name,
                                    onClick = { selectedCategory = category.name },
                                    label = { Text(category.name) },
                                    modifier = Modifier.testTag("budget_target_${category.name}")
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Target: ${if (selectedCategory == Budget.OVERALL_CATEGORY) "Overall Monthly Budget" else selectedCategory}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary
                    )
                }

                // Limit Amount
                Column {
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = {
                            limitText = it
                            if (hasAttemptedSubmit) {
                                limitError = ExpenseValidator.validateBudgetLimit(it).errorMessage
                            }
                        },
                        label = { Text("Monthly Limit ($) *") },
                        placeholder = { Text("e.g. 500.00") },
                        leadingIcon = {
                            Text(
                                "$",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                        },
                        singleLine = true,
                        isError = limitError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_limit_input")
                    )
                    if (limitError != null) {
                        Text(
                            text = limitError ?: "",
                            color = colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp).testTag("budget_limit_error")
                        )
                    }
                }

                // Alert Threshold Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Warning Alert Threshold",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$thresholdPercent%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                    Slider(
                        value = thresholdPercent.toFloat(),
                        onValueChange = { thresholdPercent = it.toInt() },
                        valueRange = 50f..95f,
                        steps = 8,
                        modifier = Modifier.testTag("budget_threshold_slider")
                    )
                    Text(
                        text = "You will receive visual alerts when spending reaches $thresholdPercent% of this limit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            hasAttemptedSubmit = true
                            val validation = ExpenseValidator.validateBudgetLimit(limitText)
                            limitError = validation.errorMessage
                            if (validation.isValid) {
                                val cleanLimit = limitText.trim().replace("$", "").replace(",", "").toDouble()
                              //  onSave(selectedCategory, cleanLimit, thresholdPercent)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("save_budget_button")
                    ) {
                        Text("Save Budget")
                    }
                }
            }
        }
    }
}