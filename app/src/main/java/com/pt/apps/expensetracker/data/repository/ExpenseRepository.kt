package com.pt.apps.expensetracker.data.repository

import com.pt.apps.expensetracker.data.dao.BudgetDao
import com.pt.apps.expensetracker.data.dao.ExpenseDao
import com.pt.apps.expensetracker.data.model.Budget
import com.pt.apps.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
){
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetween(start, end)
    }

    suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun saveBudget(budget: Budget) {
        budgetDao.insertOrUpdate(budget)
    }

    suspend fun deleteBudget(category: String) {
        budgetDao.deleteBudget(category)
    }
}