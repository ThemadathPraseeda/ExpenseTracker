package com.pt.apps.expensetracker.domain

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun success() = ValidationResult(isValid = true, errorMessage = null)
        fun error(message: String) = ValidationResult(isValid = false, errorMessage = message)
    }
}
object ExpenseValidator {

    fun validateTitle(title: String): ValidationResult {
        val trimmed = title.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.error("Expense title cannot be empty.")
            trimmed.length < 2 -> ValidationResult.error("Title must be at least 2 characters long.")
            trimmed.length > 60 -> ValidationResult.error("Title cannot exceed 60 characters.")
            else -> ValidationResult.success()
        }
    }

    fun validateAmount(amountStr: String): ValidationResult {
        val trimmed = amountStr.trim().replace("$", "").replace(",", "")
        if (trimmed.isEmpty()) {
            return ValidationResult.error("Please enter an amount.")
        }
        val amount = trimmed.toDoubleOrNull()
            ?: return ValidationResult.error("Please enter a valid numeric amount.")

        return when {
            amount <= 0.0 -> ValidationResult.error("Amount must be greater than $0.00.")
            amount > 1_000_000.0 -> ValidationResult.error("Amount cannot exceed $1,000,000.")
            else -> ValidationResult.success()
        }
    }

    fun validateCategory(category: String): ValidationResult {
        return if (category.trim().isEmpty()) {
            ValidationResult.error("Please select a spending category.")
        } else {
            ValidationResult.success()
        }
    }

    fun validateDate(dateMillis: Long): ValidationResult {
        val currentMillis = System.currentTimeMillis()
        val oneYearFuture = currentMillis + (365L * 24 * 60 * 60 * 1000)
        val year2000Millis = 946684800000L // Jan 1, 2000

        return when {
            dateMillis < year2000Millis -> ValidationResult.error("Date cannot be before the year 2000.")
            dateMillis > oneYearFuture -> ValidationResult.error("Date cannot be more than 1 year in the future.")
            else -> ValidationResult.success()
        }
    }

    fun validateBudgetLimit(limitStr: String): ValidationResult {
        val trimmed = limitStr.trim().replace("$", "").replace(",", "")
        if (trimmed.isEmpty()) {
            return ValidationResult.error("Please enter a monthly budget limit.")
        }
        val limit = trimmed.toDoubleOrNull()
            ?: return ValidationResult.error("Please enter a valid numeric budget amount.")

        return when {
            limit <= 0.0 -> ValidationResult.error("Budget limit must be greater than $0.00.")
            limit > 5_000_000.0 -> ValidationResult.error("Budget limit cannot exceed $5,000,000.")
            else -> ValidationResult.success()
        }
    }
}