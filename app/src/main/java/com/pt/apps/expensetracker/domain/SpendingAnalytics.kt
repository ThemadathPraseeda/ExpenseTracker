package com.pt.apps.expensetracker.domain

import com.pt.apps.expensetracker.data.model.Budget
import com.pt.apps.expensetracker.data.model.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.filter

enum class DateRangePreset(val displayName: String) {
    THIS_MONTH("This Month"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year"),
    ALL("All Time"),
    CUSTOM("Custom")
}

data class CategorySpending(
    val category: String,
    val totalAmount: Double,
    val percentage: Float, // 0.0 to 100.0
    val count: Int
)
data class BudgetAlertItem(
    val category: String,
    val spent: Double,
    val limit: Double,
    val percentUsed: Float,
    val isExceeded: Boolean,
    val isWarning: Boolean,
    val remainingAmount: Double,
    val overAmount: Double,
    val thresholdPercent: Int
) {
    val isSafe: Boolean
        get() = !isExceeded && !isWarning
}
data class MonthlyTrendPoint(
    val monthLabel: String,
    val year: Int,
    val month: Int,
    val total: Double
)
data class DailyTrendPoint(
    val dayLabel: String,
    val dateMillis: Long,
    val total: Double
)
object SpendingAnalytics {

    fun getDateRange(
        preset: DateRangePreset,
        customStart: Long? = null,
        customEnd: Long? = null
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val nowMillis = calendar.timeInMillis

        return when (preset) {
            DateRangePreset.ALL -> Pair(0L, Long.MAX_VALUE)
            DateRangePreset.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.CUSTOM -> {
                val s = customStart ?: 0L
                val e = customEnd ?: nowMillis
                Pair(s.coerceAtMost(e), s.coerceAtLeast(e))
            }
        }
    }


    fun filterExpenses(
        expenses: List<Expense>,
        preset: DateRangePreset,
        customStart: Long? = null,
        customEnd: Long? = null,
        category: String? = null,
        tag: String? = null,
        searchQuery: String = ""
    ): List<Expense> {
        val (start, end) = getDateRange(preset, customStart, customEnd)
        val query = searchQuery.trim().lowercase(Locale.ROOT)

        return expenses.filter { expense ->
            val matchesDate = expense.date in start..end
            val matchesCategory = category.isNullOrBlank() || expense.category.equals(category, ignoreCase = true)
            val matchesTag = tag.isNullOrBlank() || expense.getTagList().any { it.equals(tag, ignoreCase = true) }
            val matchesQuery = query.isEmpty() ||
                    expense.title.lowercase(Locale.ROOT).contains(query) ||
                    expense.note.lowercase(Locale.ROOT).contains(query) ||
                    expense.tags.lowercase(Locale.ROOT).contains(query) ||
                    expense.category.lowercase(Locale.ROOT).contains(query)

            matchesDate && matchesCategory && matchesTag && matchesQuery
        }
    }

    fun calculateCategoryBreakdown(expenses: List<Expense>): List<CategorySpending> {
        if (expenses.isEmpty()) return emptyList()

        val totalAll = expenses.sumOf { it.amount }
        if (totalAll <= 0.0) return emptyList()

        return expenses.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val pct = ((sum / totalAll) * 100.0).toFloat()
                CategorySpending(
                    category = cat,
                    totalAmount = sum,
                    percentage = pct,
                    count = list.size
                )
            }
            .sortedByDescending { it.totalAmount }
    }
    fun calculateBudgetAlerts(
        expensesThisMonth: List<Expense>,
        budgets: List<Budget>
    ): List<BudgetAlertItem> {
        if (budgets.isEmpty()) return emptyList()

        val categorySpendingMap = expensesThisMonth.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val totalThisMonth = expensesThisMonth.sumOf { it.amount }

        return budgets.map { budget ->
            val spent = if (budget.isOverall) {
                totalThisMonth
            } else {
                categorySpendingMap[budget.category] ?: 0.0
            }
            val percentUsed = if (budget.monthlyLimit > 0) {
                ((spent / budget.monthlyLimit) * 100.0).toFloat()
            } else 0f

            val isExceeded = spent >= budget.monthlyLimit && budget.monthlyLimit > 0
            val isWarning = !isExceeded && percentUsed >= budget.alertThresholdPercent

            val remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)
            val over = (spent - budget.monthlyLimit).coerceAtLeast(0.0)

            BudgetAlertItem(
                category = budget.category,
                spent = spent,
                limit = budget.monthlyLimit,
                percentUsed = percentUsed,
                isExceeded = isExceeded,
                isWarning = isWarning,
                remainingAmount = remaining,
                overAmount = over,
                thresholdPercent = budget.alertThresholdPercent
            )
        }.sortedWith(
            compareByDescending<BudgetAlertItem> { it.isExceeded }
                .thenByDescending { it.isWarning }
                .thenByDescending { it.percentUsed }
        )
    }

    fun calculateMonthlyTrend(
        expenses: List<Expense>,
        numberOfMonths: Int = 6
    ): List<MonthlyTrendPoint> {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("MMM", Locale.getDefault())
        val points = mutableListOf<MonthlyTrendPoint>()

        // Generate points for the past N months ending in current month
        for (i in (numberOfMonths - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val start = c.timeInMillis

            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            c.set(Calendar.MILLISECOND, 999)
            val end = c.timeInMillis

            val monthExpenses = expenses.filter { it.date in start..end }
            val total = monthExpenses.sumOf { it.amount }
            val label = format.format(c.time)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)

            points.add(
                MonthlyTrendPoint(
                    monthLabel = label,
                    year = year,
                    month = month,
                    total = total
                )
            )
        }
        return points
    }

    fun calculateDailyTrend(
        expenses: List<Expense>,
        days: Int = 14
    ): List<DailyTrendPoint> {
        val format = SimpleDateFormat("d MMM", Locale.getDefault())
        val points = mutableListOf<DailyTrendPoint>()

        for (i in (days - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val start = c.timeInMillis

            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            c.set(Calendar.MILLISECOND, 999)
            val end = c.timeInMillis

            val dayTotal = expenses.filter { it.date in start..end }.sumOf { it.amount }
            val label = format.format(c.time)

            points.add(
                DailyTrendPoint(
                    dayLabel = label,
                    dateMillis = start,
                    total = dayTotal
                )
            )
        }
        return points
    }

    fun extractAllTags(expenses: List<Expense>): List<String> {
        return expenses.flatMap { it.getTagList() }
            .map { it.lowercase(Locale.ROOT) }
            .distinct()
            .sorted()
    }
}