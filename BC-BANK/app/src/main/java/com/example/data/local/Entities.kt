package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_info")
data class AccountInfoEntity(
    @PrimaryKey val id: Int = 1,
    val accountHolder: String = "",
    val bankName: String = "Cuenta de Ahorros",
    val accountNumber: String = "",
    val cciNumber: String = "",
    val cardLastFour: String = "",
    val balance: Double = 0.0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "GOAL_DEPOSIT"
    val category: String, // "Alimentación", "Servicios", "Entretenimiento", "Transporte", "Salud", "Compras", "Sueldo", "Transferencia", "Ahorro", "Otro"
    val timestamp: Long = System.currentTimeMillis(),
    val recipientOrSender: String = "",
    val referenceNumber: String = "",
    val note: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val categoryIcon: String = "SAVINGS", // "EMERGENCY", "TRAVEL", "TECH", "HOME", "CAR", "SAVINGS"
    val targetDate: String = "Dic 2026",
    val colorHex: String = "#10B981",
    val status: String = "IN_PROGRESS" // "IN_PROGRESS", "COMPLETED", "WITHDRAWN"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val spentAmount: Double = 0.0,
    val iconName: String = "DEFAULT"
)

@Entity(tableName = "bank_notifications")
data class BankNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String = "",
    val title: String,
    val message: String,
    val category: String, // "Transacciones", "Retiros", "Depósitos", "Servicios", "Seguridad"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val amountTag: String? = null,
    val type: String = "GENERAL" // "TRANSFER_RECEIVED", "TRANSFER_SENT", "WITHDRAWAL_PENDING", "WITHDRAWAL_COMPLETED", "WITHDRAWAL_CANCELLED", "DEPOSIT_CONFIRMED", "SERVICE_PAYMENT", "SECURITY"
)

