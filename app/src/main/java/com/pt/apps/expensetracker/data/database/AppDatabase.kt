package com.pt.apps.expensetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pt.apps.expensetracker.data.dao.BudgetDao
import com.pt.apps.expensetracker.data.dao.ExpenseDao
import com.pt.apps.expensetracker.data.model.Budget
import com.pt.apps.expensetracker.data.model.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Expense::class, Budget::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendwise_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val expenseDao = database.expenseDao()
            val budgetDao = database.budgetDao()

            if (budgetDao.getBudgetCount() == 0) {
                val initialBudgets = listOf(
                    Budget(category = Budget.OVERALL_CATEGORY, monthlyLimit = 2400.0, alertThresholdPercent = 80),
                    Budget(category = "Food & Dining", monthlyLimit = 550.0, alertThresholdPercent = 80),
                    Budget(category = "Groceries", monthlyLimit = 400.0, alertThresholdPercent = 80),
                    Budget(category = "Transportation", monthlyLimit = 220.0, alertThresholdPercent = 80),
                    Budget(category = "Shopping", monthlyLimit = 350.0, alertThresholdPercent = 80),
                    Budget(category = "Entertainment", monthlyLimit = 180.0, alertThresholdPercent = 80),
                    Budget(category = "Utilities", monthlyLimit = 200.0, alertThresholdPercent = 80)
                )
                budgetDao.insertAll(initialBudgets)
            }

            if (expenseDao.getExpenseCount() == 0) {
                val cal = Calendar.getInstance()
                val now = cal.timeInMillis

                fun daysAgo(days: Int): Long {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -days)
                    return c.timeInMillis
                }

                val sampleExpenses = listOf(
                    Expense(
                        title = "Whole Foods Organic Groceries",
                        amount = 112.50,
                        category = "Groceries",
                        date = daysAgo(0),
                        tags = "groceries,weekly",
                        note = "Weekly pantry refill and vegetables",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Dinner at Olive Bistro",
                        amount = 68.20,
                        category = "Food & Dining",
                        date = daysAgo(1),
                        tags = "dining,weekend",
                        note = "Italian pasta dinner with friends",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Monthly Metro Transit Pass",
                        amount = 75.00,
                        category = "Transportation",
                        date = daysAgo(2),
                        tags = "transit,commute",
                        note = "Subway and bus monthly pass",
                        paymentMethod = "UPI/Transfer"
                    ),
                    Expense(
                        title = "Coffee & Bakery",
                        amount = 14.75,
                        category = "Food & Dining",
                        date = daysAgo(2),
                        tags = "coffee,work",
                        note = "Latte and croissant before morning meeting",
                        paymentMethod = "Cash"
                    ),
                    Expense(
                        title = "Electric & Water Utility Bill",
                        amount = 145.30,
                        category = "Utilities",
                        date = daysAgo(4),
                        tags = "bills,home",
                        note = "Monthly power and water invoice",
                        paymentMethod = "UPI/Transfer"
                    ),
                    Expense(
                        title = "Sneakers & Athletic Wear",
                        amount = 120.00,
                        category = "Shopping",
                        date = daysAgo(5),
                        tags = "shopping,fitness",
                        note = "Running shoes replacement",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "IMAX Cinema Tickets",
                        amount = 36.00,
                        category = "Entertainment",
                        date = daysAgo(6),
                        tags = "movies,weekend",
                        note = "Sci-fi screening premiere",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Pharmacy & Vitamin Supplements",
                        amount = 42.80,
                        category = "Health & Medical",
                        date = daysAgo(8),
                        tags = "health,wellness",
                        note = "Monthly multivitamin and omega-3",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Supermarket Pantry Essentials",
                        amount = 89.40,
                        category = "Groceries",
                        date = daysAgo(10),
                        tags = "groceries,essentials",
                        note = "Olive oil, grains, and produce",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Rideshare to Airport",
                        amount = 45.50,
                        category = "Transportation",
                        date = daysAgo(12),
                        tags = "travel,commute",
                        note = "Uber ride during rainy morning",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Sushi Lunch Special",
                        amount = 28.50,
                        category = "Food & Dining",
                        date = daysAgo(14),
                        tags = "dining,work",
                        note = "Lunch combo box",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "High-Speed Internet Fiber",
                        amount = 60.00,
                        category = "Utilities",
                        date = daysAgo(16),
                        tags = "bills,internet",
                        note = "Home Gigabit plan",
                        paymentMethod = "UPI/Transfer"
                    ),
                    Expense(
                        title = "Online Course Subscription",
                        amount = 29.99,
                        category = "Education",
                        date = daysAgo(18),
                        tags = "learning,skills",
                        note = "Advanced Kotlin & System Design",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Streaming & Music Subscriptions",
                        amount = 24.98,
                        category = "Entertainment",
                        date = daysAgo(21),
                        tags = "entertainment,subs",
                        note = "Family audio and video streaming",
                        paymentMethod = "Card"
                    ),
                    Expense(
                        title = "Haircut & Grooming",
                        amount = 40.00,
                        category = "Personal Care",
                        date = daysAgo(23),
                        tags = "personal,grooming",
                        note = "Barbershop visit",
                        paymentMethod = "Cash"
                    )
                )
                expenseDao.insertAll(sampleExpenses)
            }
        }
    }
}
