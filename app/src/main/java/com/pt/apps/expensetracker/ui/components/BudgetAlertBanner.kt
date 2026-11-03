package com.pt.apps.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pt.apps.expensetracker.domain.BudgetAlertItem
import com.pt.apps.expensetracker.ui.theme.StatusAlert
import com.pt.apps.expensetracker.ui.theme.StatusAlertContainer
import com.pt.apps.expensetracker.ui.theme.StatusSafe
import com.pt.apps.expensetracker.ui.theme.StatusSafeContainer
import com.pt.apps.expensetracker.ui.theme.StatusWarning
import com.pt.apps.expensetracker.ui.theme.StatusWarningContainer
import java.text.NumberFormat
import java.util.Locale

private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

@Composable
fun BudgetAlertBanner(
    alerts: List<BudgetAlertItem>,
    modifier: Modifier = Modifier,
    onViewBudgetsClick: () -> Unit
) {
    val exceededAlerts = alerts.filter { it.isExceeded }
    val warningAlerts = alerts.filter { it.isWarning }

    val hasCritical = exceededAlerts.isNotEmpty()
    val hasWarning = warningAlerts.isNotEmpty()

    if (!hasCritical && !hasWarning) return

    val containerColor = if (hasCritical) StatusAlertContainer else StatusWarningContainer
    val contentColor = if (hasCritical) StatusAlert else StatusWarning
    val icon = if (hasCritical) Icons.Default.ErrorOutline else Icons.Default.WarningAmber

    val headlineText = when {
        hasCritical -> {
            val first = exceededAlerts.first()
            if (exceededAlerts.size == 1) {
                "${first.category} budget exceeded by ${currencyFormat.format(first.overAmount)}!"
            } else {
                "${exceededAlerts.size} budgets exceeded limit!"
            }
        }
        else -> {
            val first = warningAlerts.first()
            if (warningAlerts.size == 1) {
                "${first.category} reached ${first.percentUsed.toInt()}% of budget!"
            } else {
                "${warningAlerts.size} budgets approaching limits (>80%)"
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onViewBudgetsClick() }
            .testTag("budget_alert_banner"),
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Budget Alert",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (hasCritical) "Budget Alert" else "Budget Warning",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = headlineText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun OverallBudgetCard(
    budgetAlert: BudgetAlertItem?,
    thisMonthSpent: Double,
    onSetBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("overall_budget_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (budgetAlert != null) {
                    val statusText = when {
                        budgetAlert.isExceeded -> "Exceeded"
                        budgetAlert.isWarning -> "Warning"
                        else -> "On Track"
                    }
                    val statusColor = when {
                        budgetAlert.isExceeded -> StatusAlert
                        budgetAlert.isWarning -> StatusWarning
                        else -> StatusSafe
                    }
                    val statusBg = when {
                        budgetAlert.isExceeded -> StatusAlertContainer
                        budgetAlert.isWarning -> StatusWarningContainer
                        else -> StatusSafeContainer
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (budgetAlert != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = currencyFormat.format(budgetAlert.spent),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (budgetAlert.isExceeded) StatusAlert else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "of ${currencyFormat.format(budgetAlert.limit)} monthly target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", budgetAlert.percentUsed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (budgetAlert.isExceeded) StatusAlert else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (budgetAlert.isExceeded) {
                                "+${currencyFormat.format(budgetAlert.overAmount)} over"
                            } else {
                                "${currencyFormat.format(budgetAlert.remainingAmount)} left"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (budgetAlert.isExceeded) StatusAlert else StatusSafe
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                val progress = (budgetAlert.spent / budgetAlert.limit).toFloat().coerceIn(0f, 1f)
                val progressColor = when {
                    budgetAlert.isExceeded -> StatusAlert
                    budgetAlert.isWarning -> StatusWarning
                    else -> MaterialTheme.colorScheme.primary
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("budget_progress_bar"),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currencyFormat.format(thisMonthSpent),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Spent this month (No budget set)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = onSetBudgetClick,
                        modifier = Modifier.testTag("set_overall_budget_button")
                    ) {
                        Text("Set Budget")
                    }
                }
            }
        }
    }
}