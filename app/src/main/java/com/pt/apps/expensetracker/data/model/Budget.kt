package com.pt.apps.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey
    val category: String, // "OVERALL" or Category Name
    val monthlyLimit: Double,
    val alertThresholdPercent: Int = 80
) {
    companion object {
        const val OVERALL_CATEGORY = "OVERALL"
    }

    val isOverall: Boolean
        get() = category == OVERALL_CATEGORY
}

