package com.example.ui.viewmodel

import com.example.data.local.TransactionEntity

enum class NavigationTab {
    DASHBOARD,
    METAS,
    PRESUPUESTOS,
    ANALITICA
}

sealed interface ActiveWindow {
    object None : ActiveWindow
    data class Transfer(
        val initialRecipient: String = "",
        val initialIdentifier: String = "",
        val initialAmount: Double? = null
    ) : ActiveWindow
    object Deposit : ActiveWindow
    object PayServices : ActiveWindow
    object WithdrawQr : ActiveWindow
    object Profile : ActiveWindow
    object Notifications : ActiveWindow
    data class TransactionDetail(val transaction: TransactionEntity) : ActiveWindow
}

enum class SessionState {
    SPLASH,
    AUTH,
    EMAIL_VERIFICATION,
    ONBOARDING,
    LOCKED,
    AUTHENTICATED
}

data class TransferReceipt(
    val recipient: String,
    val accountOrPhone: String,
    val amount: Double,
    val concept: String,
    val category: String,
    val operationCode: String,
    val timestamp: Long = System.currentTimeMillis()
)

