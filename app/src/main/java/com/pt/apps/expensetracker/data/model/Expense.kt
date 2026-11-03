package com.pt.apps.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val tags: String = "",
    val note: String = "",
    val paymentMethod: String = "Card"
){
    fun getTagList(): List<String> {
        return if (tags.isBlank()) {
            emptyList()
        } else {
            tags.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
    }
}
