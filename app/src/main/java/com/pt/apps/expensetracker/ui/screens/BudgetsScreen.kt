package com.pt.apps.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pt.apps.expensetracker.data.model.Budget
import com.pt.apps.expensetracker.data.model.Categories
import com.pt.apps.expensetracker.ui.components.OverallBudgetCard
import com.pt.apps.expensetracker.ui.theme.StatusAlert
import com.pt.apps.expensetracker.ui.theme.StatusAlertContainer
import com.pt.apps.expensetracker.ui.theme.StatusSafe
import com.pt.apps.expensetracker.ui.theme.StatusSafeContainer
import com.pt.apps.expensetracker.ui.theme.StatusWarning
import com.pt.apps.expensetracker.ui.theme.StatusWarningContainer
import com.pt.apps.expensetracker.ui.viewmodel.ExpenseUiState
import java.text.NumberFormat
import java.util.Locale

private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

@Composable
fun BudgetsScreen(
    state: ExpenseUiState,
    onSetOverallBudget: () -> Unit,
    onAddCategoryBudget: () -> Unit,
    onEditBudget: (Budget) -> Unit,
    onDeleteBudget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var budgetCategoryToDelete by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budgets_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Limits & Alerts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Budget Management",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = onAddCategoryBudget,
                    modifier = Modifier.testTag("add_budget_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Budget")
                }
            }
        }

        // Overall Monthly Budget Card
        item {
            OverallBudgetCard(
                budgetAlert = state.overallBudgetAlert,
                thisMonthSpent = state.thisMonthSpent,
                onSetBudgetClick = onSetOverallBudget
            )
        }

        // Category Budgets Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.budgetAlerts.filter { !it.category.equals(Budget.OVERALL_CATEGORY, ignoreCase = true) }.size} active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val categoryAlerts = state.budgetAlerts.filter { !it.category.equals(Budget.OVERALL_CATEGORY, ignoreCase = true) }

        if (categoryAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No category budgets configured",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set limits for dining, groceries, shopping, and more to get automatic warning alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddCategoryBudget,
                            modifier = Modifier.testTag("empty_add_budget_button")
                        ) {
                            Text("Set Your First Category Budget")
                        }
                    }
                }
            }
        } else {
            items(categoryAlerts, key = { it.category }) { alert ->
                val category = Categories.getCategory(alert.category)
                val budgetObj = state.allBudgets.firstOrNull { it.category == alert.category }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_card_${alert.category}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(category.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = category.color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = alert.category,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Limit: ${currencyFormat.format(alert.limit)} / month",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Status Pill
                                val (badgeText, badgeColor, badgeBg) = when {
                                    alert.isExceeded -> Triple("Exceeded", StatusAlert, StatusAlertContainer)
                                    alert.isWarning -> Triple("Warning", StatusWarning, StatusWarningContainer)
                                    else -> Triple("Safe", StatusSafe, StatusSafeContainer)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = {
                                        if (budgetObj != null) onEditBudget(budgetObj)
                                    },
                                    modifier = Modifier.size(32.dp).testTag("edit_budget_${alert.category}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Budget", modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = { budgetCategoryToDelete = alert.category },
                                    modifier = Modifier.size(32.dp).testTag("delete_budget_${alert.category}")
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Budget",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Amount spent & Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${currencyFormat.format(alert.spent)} spent",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (alert.isExceeded) StatusAlert else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (alert.isExceeded) {
                                    "+${currencyFormat.format(alert.overAmount)} over"
                                } else {
                                    "${currencyFormat.format(alert.remainingAmount)} left (${alert.percentUsed.toInt()}%)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (alert.isExceeded) StatusAlert else if (alert.isWarning) StatusWarning else StatusSafe
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val progress = (alert.spent / alert.limit).toFloat().coerceIn(0f, 1f)
                        val progressColor = when {
                            alert.isExceeded -> StatusAlert
                            alert.isWarning -> StatusWarning
                            else -> category.color
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (budgetCategoryToDelete != null) {
        val cat = budgetCategoryToDelete!!
        AlertDialog(
            onDismissRequest = { budgetCategoryToDelete = null },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to remove the monthly budget for \"$cat\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBudget(cat)
                        budgetCategoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_budget_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { budgetCategoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
