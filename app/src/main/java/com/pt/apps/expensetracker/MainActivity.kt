package com.pt.apps.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pt.apps.expensetracker.ui.theme.ExpenseTrackerTheme
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pt.apps.expensetracker.data.database.AppDatabase
import com.pt.apps.expensetracker.data.model.Budget
import com.pt.apps.expensetracker.data.model.Expense
import com.pt.apps.expensetracker.data.repository.ExpenseRepository
import com.pt.apps.expensetracker.ui.components.AddEditBudgetDialog
import com.pt.apps.expensetracker.ui.components.AddEditExpenseDialog
import com.pt.apps.expensetracker.ui.screens.AnalyticsScreen
import com.pt.apps.expensetracker.ui.screens.BudgetsScreen
import com.pt.apps.expensetracker.ui.screens.DashboardScreen
import com.pt.apps.expensetracker.ui.screens.ExpensesScreen
import com.pt.apps.expensetracker.ui.viewmodel.ExpenseViewModel
import com.pt.apps.expensetracker.ui.viewmodel.ExpenseViewModelFactory

enum class NavigationScreen(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    EXPENSES("Expenses", Icons.AutoMirrored.Filled.ReceiptLong),
    ANALYTICS("Analytics", Icons.Default.BarChart),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet)
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {

                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember {
                    ExpenseRepository(database.expenseDao(), database.budgetDao())
                }
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory(repository)
                )

                SpendWiseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SpendWiseApp(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }

    // Dialog States
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<Budget?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Handle user messages via Snackbar
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    // Android Hardware Back button handling
    BackHandler(enabled = currentScreen != NavigationScreen.DASHBOARD) {
        currentScreen = NavigationScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar"),
                tonalElevation = 8.dp
            ) {
                NavigationScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    val badgeCount = if (screen == NavigationScreen.BUDGETS) state.activeAlertsCount else 0

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        label = { Text(screen.label) },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(badge = { Badge { Text("$badgeCount") } }) {
                                    Icon(screen.icon, contentDescription = screen.label)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        },
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == NavigationScreen.DASHBOARD || currentScreen == NavigationScreen.EXPENSES) {
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    modifier = Modifier.testTag("fab_add_expense"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                NavigationScreen.DASHBOARD -> DashboardScreen(
                    state = state,
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onEditExpenseClick = { expenseToEdit = it },
                    onDeleteExpenseClick = { viewModel.deleteExpense(it) },
                    onNavigateToExpenses = { currentScreen = NavigationScreen.EXPENSES },
                    onNavigateToAnalytics = { currentScreen = NavigationScreen.ANALYTICS },
                    onNavigateToBudgets = { currentScreen = NavigationScreen.BUDGETS },
                    onExportClick = { showExportDialog = true }
                )
                NavigationScreen.EXPENSES -> ExpensesScreen(
                    state = state,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onPresetSelected = { viewModel.setDatePreset(it) },
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    onTagSelected = { viewModel.setTagFilter(it) },
                    onClearFilters = { viewModel.clearFilters() },
                    onEditExpense = { expenseToEdit = it },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                NavigationScreen.ANALYTICS -> AnalyticsScreen(
                    state = state,
                    onPresetSelected = { viewModel.setDatePreset(it) },
                    onExportClick = { showExportDialog = true }
                )
                NavigationScreen.BUDGETS -> BudgetsScreen(
                    state = state,
                    onSetOverallBudget = {
                        budgetToEdit = state.allBudgets.firstOrNull { it.category == Budget.OVERALL_CATEGORY }
                            ?: Budget(Budget.OVERALL_CATEGORY, 2000.0)
                        showBudgetDialog = true
                    },
                    onAddCategoryBudget = {
                        budgetToEdit = null
                        showBudgetDialog = true
                    },
                    onEditBudget = { budget ->
                        budgetToEdit = budget
                        showBudgetDialog = true
                    },
                    onDeleteBudget = { category ->
                        viewModel.deleteBudget(category)
                    }
                )
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddEditExpenseDialog(
            availableTags = state.allAvailableTags,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, amount, category, date, tags, note, method ->
                viewModel.addExpense(title, amount, category, date, tags, note, method)
            }
        )
    }

    // Edit Expense Dialog
    if (expenseToEdit != null) {
        AddEditExpenseDialog(
            initialExpense = expenseToEdit,
            availableTags = state.allAvailableTags,
            onDismiss = { expenseToEdit = null },
            onSave = { title, amount, category, date, tags, note, method ->
                viewModel.updateExpense(
                    id = expenseToEdit!!.id,
                    title = title,
                    amount = amount,
                    category = category,
                    date = date,
                    tags = tags,
                    note = note,
                    paymentMethod = method
                )
                expenseToEdit = null
            }
        )
    }

    // Budget Dialog
    if (showBudgetDialog) {
        AddEditBudgetDialog(
            initialBudget = budgetToEdit,
            onDismiss = {
                showBudgetDialog = false
                budgetToEdit = null
            },
           /* onSave = { category, limit, threshold ->
                viewModel.saveBudget(category, limit, threshold)
                showBudgetDialog = false
                budgetToEdit = null
            }*/
        )
    }


}
