package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM account_info WHERE id = 1 LIMIT 1")
    fun getAccountFlow(): Flow<AccountInfoEntity?>

    @Query("SELECT * FROM account_info WHERE id = 1 LIMIT 1")
    suspend fun getAccount(): AccountInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: AccountInfoEntity)

    @Query("UPDATE account_info SET balance = :newBalance WHERE id = 1")
    suspend fun updateBalance(newBalance: Double)

    @Query("DELETE FROM account_info")
    suspend fun clearAccount()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE referenceNumber = :ref LIMIT 1")
    suspend fun getTransactionByReference(ref: String): TransactionEntity?

    @Query("UPDATE transactions SET title = :newTitle, note = :newNote WHERE referenceNumber = :ref")
    suspend fun updateTransactionByReference(ref: String, newTitle: String, newNote: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByTypeFlow(type: String): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id ASC")
    fun getAllGoalsFlow(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun addFundsToGoal(id: Long, amount: Double)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    @Query("DELETE FROM savings_goals")
    suspend fun clearAllGoals()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id ASC")
    fun getAllBudgetsFlow(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudget(id: Long)

    @Query("UPDATE budgets SET spentAmount = spentAmount + :amount WHERE category = :category")
    suspend fun addSpending(category: String, amount: Double)

    @Query("UPDATE budgets SET monthlyLimit = :limit WHERE category = :category")
    suspend fun updateLimit(category: String, limit: Double)

    @Query("UPDATE budgets SET spentAmount = 0.0 WHERE id = :id")
    suspend fun resetSpent(id: Long)

    @Query("DELETE FROM budgets")
    suspend fun clearAllBudgets()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM bank_notifications WHERE (:uid = '' OR uid = :uid OR uid = 'local_user') ORDER BY timestamp DESC")
    fun getNotificationsFlow(uid: String): Flow<List<BankNotificationEntity>>

    @Query("SELECT * FROM bank_notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<BankNotificationEntity>>

    @Query("SELECT COUNT(*) FROM bank_notifications WHERE isRead = 0 AND (:uid = '' OR uid = :uid OR uid = 'local_user')")
    fun getUnreadCountFlow(uid: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: BankNotificationEntity): Long

    @Query("UPDATE bank_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE bank_notifications SET isRead = 1 WHERE (:uid = '' OR uid = :uid OR uid = 'local_user')")
    suspend fun markAllAsRead(uid: String)

    @Query("DELETE FROM bank_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM bank_notifications WHERE (:uid = '' OR uid = :uid OR uid = 'local_user')")
    suspend fun clearAllNotifications(uid: String)
}

