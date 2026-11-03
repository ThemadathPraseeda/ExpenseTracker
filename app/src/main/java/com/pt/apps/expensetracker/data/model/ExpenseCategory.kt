package com.pt.apps.expensetracker.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pt.apps.expensetracker.ui.theme.CatEducation
import com.pt.apps.expensetracker.ui.theme.CatEntertainment
import com.pt.apps.expensetracker.ui.theme.CatFood
import com.pt.apps.expensetracker.ui.theme.CatGroceries
import com.pt.apps.expensetracker.ui.theme.CatHealth
import com.pt.apps.expensetracker.ui.theme.CatHousing
import com.pt.apps.expensetracker.ui.theme.CatOther
import com.pt.apps.expensetracker.ui.theme.CatPersonal
import com.pt.apps.expensetracker.ui.theme.CatShopping
import com.pt.apps.expensetracker.ui.theme.CatTransport
import com.pt.apps.expensetracker.ui.theme.CatTravel
import com.pt.apps.expensetracker.ui.theme.CatUtilities

data class ExpenseCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object Categories {
    val FOOD = ExpenseCategory("Food & Dining", Icons.Default.Restaurant, CatFood)
    val GROCERIES = ExpenseCategory("Groceries", Icons.Default.ShoppingCart, CatGroceries)
    val SHOPPING = ExpenseCategory("Shopping", Icons.Default.LocalMall, CatShopping)
    val HOUSING = ExpenseCategory("Housing & Rent", Icons.Default.Home, CatHousing)
    val TRANSPORT = ExpenseCategory("Transportation", Icons.Default.DirectionsCar, CatTransport)
    val UTILITIES = ExpenseCategory("Utilities", Icons.Default.Bolt, CatUtilities)
    val ENTERTAINMENT =
        ExpenseCategory("Entertainment", Icons.Default.ConfirmationNumber, CatEntertainment)
    val HEALTH = ExpenseCategory("Health & Medical", Icons.Default.MedicalServices, CatHealth)
    val TRAVEL = ExpenseCategory("Travel", Icons.Default.Flight, CatTravel)
    val EDUCATION = ExpenseCategory("Education", Icons.Default.School, CatEducation)
    val PERSONAL = ExpenseCategory("Personal Care", Icons.Default.Spa, CatPersonal)
    val OTHER = ExpenseCategory("Other", Icons.Default.MoreHoriz, CatOther)

    val list = listOf(
        FOOD,
        GROCERIES,
        SHOPPING,
        HOUSING,
        TRANSPORT,
        UTILITIES,
        ENTERTAINMENT,
        HEALTH,
        TRAVEL,
        EDUCATION,
        PERSONAL,
        OTHER
    )

    fun getCategory(name: String): ExpenseCategory {
        return list.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
    }
}