package com.pt.apps.expensetracker.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pt.apps.expensetracker.data.model.*
import com.pt.apps.expensetracker.data.repository.ExpenseRepository
import com.pt.apps.expensetracker.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FilterCriteria(
    val preset: DateRangePreset = DateRangePreset.THIS_MONTH,
    val customStart: Long? = null,
    val customEnd: Long? = null,
    val category: String? = null,
    val tag: String? = null,
    val query: String = ""
)

data class ExpenseUiState(
    val allExpenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val allBudgets: List<Budget> = emptyList(),
    val selectedPreset: DateRangePreset = DateRangePreset.THIS_MONTH,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val selectedCategory: String? = null,
    val selectedTag: String? = null,
    val searchQuery: String = "",
    val allAvailableTags: List<String> = emptyList(),
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val monthlyTrend: List<MonthlyTrendPoint> = emptyList(),
    val dailyTrend: List<DailyTrendPoint> = emptyList(),
    val budgetAlerts: List<BudgetAlertItem> = emptyList(),
    val overallBudgetAlert: BudgetAlertItem? = null,
    val totalFilteredSpent: Double = 0.0,
    val thisMonthSpent: Double = 0.0,
    val averageDailySpending: Double = 0.0,
    val userMessage: String? = null,
    val isLoading: Boolean = false
) {
    val activeAlertsCount: Int
        get() = budgetAlerts.count { it.isExceeded || it.isWarning }
}

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _selectedPreset = MutableStateFlow(DateRangePreset.THIS_MONTH)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _userMessage = MutableStateFlow<String?>(null)

    private val filterFlow: Flow<FilterCriteria> = combine(
        _selectedPreset,
        _customStartDate,
        _customEndDate,
        _selectedCategory,
        _selectedTag
    ) { preset, start, end, cat, tag ->
        FilterCriteria(preset, start, end, cat, tag, "")
    }.combine(_searchQuery) { filter, query ->
        filter.copy(query = query)
    }

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.allExpenses,
        repository.allBudgets,
        filterFlow,
        _userMessage
    ) { expenses: List<Expense>, budgets: List<Budget>, filter: FilterCriteria, msg: String? ->

        val filtered = SpendingAnalytics.filterExpenses(
            expenses = expenses,
            preset = filter.preset,
            customStart = filter.customStart,
            customEnd = filter.customEnd,
            category = filter.category,
            tag = filter.tag,
            searchQuery = filter.query
        )

        // Calculate this month's expenses for budget alerts
        val (thisMonthStart, thisMonthEnd) = SpendingAnalytics.getDateRange(DateRangePreset.THIS_MONTH)
        val thisMonthExpenses = expenses.filter { it.date in thisMonthStart..thisMonthEnd }
        val thisMonthSpent = thisMonthExpenses.sumOf { it.amount }

        val budgetAlerts = SpendingAnalytics.calculateBudgetAlerts(thisMonthExpenses, budgets)
        val overallAlert = budgetAlerts.firstOrNull { it.category == Budget.OVERALL_CATEGORY }

        val categoryBreakdown = SpendingAnalytics.calculateCategoryBreakdown(filtered)
        val monthlyTrend = SpendingAnalytics.calculateMonthlyTrend(expenses, 6)
        val dailyTrend = SpendingAnalytics.calculateDailyTrend(expenses, 14)
        val allTags = SpendingAnalytics.extractAllTags(expenses)

        val totalFiltered = filtered.sumOf { it.amount }
        val daysInPeriod = when (filter.preset) {
            DateRangePreset.TODAY -> 1
            DateRangePreset.THIS_WEEK -> 7
            DateRangePreset.THIS_MONTH, DateRangePreset.LAST_MONTH -> 30
            DateRangePreset.THIS_YEAR -> 365
            else -> 30
        }
        val avgDaily = if (daysInPeriod > 0) totalFiltered / daysInPeriod else 0.0

        ExpenseUiState(
            allExpenses = expenses,
            filteredExpenses = filtered,
            allBudgets = budgets,
            selectedPreset = filter.preset,
            customStartDate = filter.customStart,
            customEndDate = filter.customEnd,
            selectedCategory = filter.category,
            selectedTag = filter.tag,
            searchQuery = filter.query,
            allAvailableTags = allTags,
            categoryBreakdown = categoryBreakdown,
            monthlyTrend = monthlyTrend,
            dailyTrend = dailyTrend,
            budgetAlerts = budgetAlerts,
            overallBudgetAlert = overallAlert,
            totalFilteredSpent = totalFiltered,
            thisMonthSpent = thisMonthSpent,
            averageDailySpending = avgDaily,
            userMessage = msg,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState(isLoading = true)
    )

    fun setDatePreset(preset: DateRangePreset) {
        _selectedPreset.value = preset
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _customStartDate.value = start
        _customEndDate.value = end
        _selectedPreset.value = DateRangePreset.CUSTOM
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setTagFilter(tag: String?) {
        _selectedTag.value = tag
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _selectedTag.value = null
        _searchQuery.value = ""
        _selectedPreset.value = DateRangePreset.THIS_MONTH
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        date: Long,
        tags: String,
        note: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val expense = Expense(
                title = title.trim(),
                amount = amount,
                category = category.trim(),
                date = date,
                tags = tags.trim(),
                note = note.trim(),
                paymentMethod = paymentMethod
            )
            repository.insertExpense(expense)
            _userMessage.value = "Expense \"$title\" added successfully."
        }
    }

    fun updateExpense(
        id: Long,
        title: String,
        amount: Double,
        category: String,
        date: Long,
        tags: String,
        note: String,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.trim(),
                amount = amount,
                category = category.trim(),
                date = date,
                tags = tags.trim(),
                note = note.trim(),
                paymentMethod = paymentMethod
            )
            repository.updateExpense(expense)
            _userMessage.value = "Expense updated."
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.value = "Expense deleted."
        }
    }

    fun saveBudget(category: String, limit: Double, alertThresholdPercent: Int = 80) {
        viewModelScope.launch {
            val budget = Budget(
                category = category,
                monthlyLimit = limit,
                alertThresholdPercent = alertThresholdPercent
            )
            repository.saveBudget(budget)
            _userMessage.value = "Budget for $category updated."
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudget(category)
            _userMessage.value = "Budget removed."
        }
    }




}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
