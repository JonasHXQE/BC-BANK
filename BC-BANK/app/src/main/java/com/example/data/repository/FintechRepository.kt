package com.example.data.repository

import com.example.data.firebase.CloudWithdrawal
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.PublicService
import com.example.data.firebase.ServicePayment
import com.example.data.firebase.UserCloudData
import com.example.data.local.AccountDao
import com.example.data.local.AccountInfoEntity
import com.example.data.local.BankNotificationEntity
import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.NotificationDao
import com.example.data.local.SavingsGoalDao
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import kotlin.random.Random

class FintechRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val budgetDao: BudgetDao,
    private val notificationDao: NotificationDao
) {
    val accountFlow: Flow<AccountInfoEntity?> = accountDao.getAccountFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val savingsGoalsFlow: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoalsFlow()
    val budgetsFlow: Flow<List<BudgetEntity>> = budgetDao.getAllBudgetsFlow()

    fun getNotificationsFlow(uid: String): Flow<List<BankNotificationEntity>> =
        notificationDao.getNotificationsFlow(uid)

    fun getUnreadNotificationsCountFlow(uid: String): Flow<Int> =
        notificationDao.getUnreadCountFlow(uid)

    suspend fun insertNotification(notification: BankNotificationEntity): Long =
        notificationDao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long, uid: String? = null) {
        notificationDao.markAsRead(id)
        val targetUid = uid ?: getCurrentUid()
        if (targetUid.isNotBlank()) {
            FirebaseManager.updateNotificationStatusInFirestore(targetUid, id, "SEEN")
        }
    }

    suspend fun markAllNotificationsAsRead(uid: String) {
        notificationDao.markAllAsRead(uid)
        if (uid.isNotBlank()) {
            FirebaseManager.markAllNotificationsAsSeenInFirestore(uid)
        }
    }

    suspend fun deleteNotification(id: Long, uid: String? = null) {
        notificationDao.deleteNotification(id)
        val targetUid = uid ?: getCurrentUid()
        if (targetUid.isNotBlank()) {
            FirebaseManager.updateNotificationStatusInFirestore(targetUid, id, "DELETED")
        }
    }

    suspend fun clearAllNotifications(uid: String) {
        notificationDao.clearAllNotifications(uid)
        if (uid.isNotBlank()) {
            FirebaseManager.clearAllNotificationsInFirestore(uid)
        }
    }

    private fun getCurrentUid(): String {
        return FirebaseManager.getCurrentUserUid() ?: "local_user"
    }

    suspend fun recalculateAndSyncBalance(): Double {
        val txs = transactionDao.getAllTransactions()
        var computedBalance = 0.0
        for (t in txs) {
            when (t.type) {
                "INCOME" -> computedBalance += t.amount
                "EXPENSE", "GOAL_DEPOSIT" -> computedBalance -= t.amount
            }
        }
        computedBalance = computedBalance.coerceAtLeast(0.0)
        accountDao.updateBalance(computedBalance)
        val acc = accountDao.getAccount()
        if (acc != null) {
            val uid = getCurrentUid()
            FirebaseManager.syncAccountToFirestore(acc, uid)
        }
        return computedBalance
    }

    suspend fun checkAndSeedInitialData() {
        val uid = FirebaseManager.getCurrentUserUid()
        if (uid != null) {
            val cloudData = FirebaseManager.loadUserDataFromFirestore(uid)
            if (cloudData?.account != null) {
                accountDao.clearAccount()
                transactionDao.clearAllTransactions()
                savingsGoalDao.clearAllGoals()
                budgetDao.clearAllBudgets()

                accountDao.insertOrUpdate(cloudData.account)
                for (tx in cloudData.transactions) transactionDao.insertTransaction(tx)
                for (g in cloudData.savingsGoals) savingsGoalDao.insertGoal(g)
                budgetDao.insertBudgets(cloudData.budgets)
                for (n in cloudData.notifications) notificationDao.insertNotification(n)

                if (cloudData.account.balance == 0.0 && cloudData.transactions.isNotEmpty()) {
                    recalculateAndSyncBalance()
                } else {
                    accountDao.updateBalance(cloudData.account.balance)
                }
            }
        }
    }

    suspend fun clearLocalData() {
        accountDao.clearAccount()
        transactionDao.clearAllTransactions()
        savingsGoalDao.clearAllGoals()
        budgetDao.clearAllBudgets()
        val uid = getCurrentUid()
        notificationDao.clearAllNotifications(uid)
    }

    suspend fun syncWithCloud(uid: String): UserCloudData? {
        val cloudData = FirebaseManager.loadUserDataFromFirestore(uid)
        if (cloudData != null && cloudData.account != null) {
            // Restore from cloud
            accountDao.clearAccount()
            transactionDao.clearAllTransactions()
            savingsGoalDao.clearAllGoals()
            budgetDao.clearAllBudgets()

            accountDao.insertOrUpdate(cloudData.account)
            for (tx in cloudData.transactions) transactionDao.insertTransaction(tx)
            for (g in cloudData.savingsGoals) savingsGoalDao.insertGoal(g)
            budgetDao.insertBudgets(cloudData.budgets)
            for (n in cloudData.notifications) notificationDao.insertNotification(n)

            if (cloudData.account.balance == 0.0 && cloudData.transactions.isNotEmpty()) {
                recalculateAndSyncBalance()
            } else {
                accountDao.updateBalance(cloudData.account.balance)
            }
        } else {
            // Push current local data to Firestore if exists
            val localAccount = accountDao.getAccount()
            if (localAccount != null) {
                FirebaseManager.syncAccountToFirestore(localAccount, uid)
            }
        }
        return cloudData
    }

    suspend fun updateBalanceFromCloud(newBalance: Double) {
        val current = accountDao.getAccount()
        if (current != null) {
            accountDao.updateBalance(newBalance)
        }
    }

    suspend fun syncAccountDetailsFromCloud(acc: AccountInfoEntity) {
        accountDao.insertOrUpdate(acc)
    }

    suspend fun syncSavingsGoalsFromCloud(goals: List<SavingsGoalEntity>) {
        savingsGoalDao.clearAllGoals()
        for (g in goals) {
            savingsGoalDao.insertGoal(g)
        }
    }

    suspend fun syncBudgetsFromCloud(budgets: List<BudgetEntity>) {
        budgetDao.clearAllBudgets()
        if (budgets.isNotEmpty()) {
            budgetDao.insertBudgets(budgets)
        }
    }

    suspend fun syncTransactionsFromCloud(txs: List<TransactionEntity>) {
        transactionDao.clearAllTransactions()
        for (tx in txs) {
            transactionDao.insertTransaction(tx)
        }
    }

    suspend fun syncNotificationsFromCloud(notifs: List<BankNotificationEntity>, uid: String) {
        notificationDao.clearAllNotifications(uid)
        for (n in notifs) {
            notificationDao.insertNotification(n)
        }
    }

    suspend fun setupNewUser(
        uid: String,
        fullName: String,
        dni: String,
        accountType: String,
        initialBalance: Double
    ) {
        // Clear any old data
        accountDao.clearAccount()
        transactionDao.clearAllTransactions()
        savingsGoalDao.clearAllGoals()
        budgetDao.clearAllBudgets()

        val dniClean = dni.filter { it.isDigit() }.padStart(8, '0').takeLast(8)
        val timeSeed = System.currentTimeMillis()
        val accMid = ((timeSeed % 89999999L) + 10000000L).toString()
        val randomSuffix = Random.nextInt(10, 99)
        val accNum = "194-$accMid-0-$randomSuffix"

        val cciUniqueLong = (timeSeed * 17L + dniClean.toLong()) % 8999999999L + 1000000000L
        val cciCheckDigit = Random.nextInt(10, 99)
        val cciNum = "002-194-00$cciUniqueLong-$cciCheckDigit"
        val cardLast4 = String.format("%04d", (timeSeed % 9000 + 1000).toInt())

        val newAccount = AccountInfoEntity(
            id = 1,
            accountHolder = fullName,
            bankName = accountType,
            accountNumber = accNum,
            cciNumber = cciNum,
            cardLastFour = cardLast4,
            balance = initialBalance
        )
        accountDao.insertOrUpdate(newAccount)
        FirebaseManager.syncAccountToFirestore(newAccount, uid)

        // Initial opening deposit transaction if initial balance > 0
        if (initialBalance > 0) {
            val welcomeTx = TransactionEntity(
                title = "Apertura de Cuenta - Saldo Inicial",
                amount = initialBalance,
                type = "INCOME",
                category = "Transferencia",
                timestamp = System.currentTimeMillis(),
                recipientOrSender = "BC-BANK Soles Digital",
                referenceNumber = "OP-${Random.nextInt(100000, 999999)}",
                note = "Saldo de apertura $accountType"
            )
            transactionDao.insertTransaction(welcomeTx)
            FirebaseManager.syncTransactionToFirestore(welcomeTx, uid)
        }
    }

    suspend fun transferMoney(
        recipient: String,
        accountOrPhone: String,
        amount: Double,
        concept: String,
        category: String
    ): Result<String> {
        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Saldo insuficiente en su cuenta en Soles"))
        }

        val opCode = "OP-${Random.nextInt(100000, 999999)}"
        val newBalance = currentAccount.balance - amount
        val updatedAccount = currentAccount.copy(balance = newBalance)
        accountDao.updateBalance(newBalance)

        val tx = TransactionEntity(
            title = if (concept.isNotBlank()) concept else "Transferencia a $recipient",
            amount = amount,
            type = "EXPENSE",
            category = category,
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "$recipient ($accountOrPhone)",
            referenceNumber = opCode,
            note = concept
        )
        transactionDao.insertTransaction(tx)
        budgetDao.addSpending(category, amount)

        val uid = getCurrentUid()
        FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
        FirebaseManager.syncTransactionToFirestore(tx, uid)

        return Result.success(opCode)
    }

    suspend fun depositFunds(amount: Double, note: String): Result<String> {
        // Los depósitos directos desde el cliente están estrictamente prohibidos por seguridad bancaria.
        // Los fondos solo pueden ser acreditados y autorizados desde el Panel Admin mediante Firebase Admin SDK.
        return Result.failure(Exception("Los depósitos de saldo solo pueden ser procesados y autorizados por la administración de BC-BANK."))
    }

    suspend fun depositToSavingsGoal(goalId: Long, amount: Double): Result<Unit> {
        val goal = savingsGoalDao.getGoalById(goalId) ?: return Result.failure(Exception("Meta no encontrada"))
        if (goal.status == "WITHDRAWN") {
            return Result.failure(Exception("Esta meta ya fue retirada y finalizada."))
        }
        if (goal.status == "COMPLETED" || goal.currentAmount >= goal.targetAmount) {
            return Result.failure(Exception("Esta meta ya completó su monto objetivo de S/ ${goal.targetAmount}. No se admiten más abonos."))
        }

        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Saldo insuficiente en su cuenta para abonar a la meta"))
        }

        val newGoalAmount = goal.currentAmount + amount
        val isNowCompleted = newGoalAmount >= goal.targetAmount
        val updatedGoal = goal.copy(
            currentAmount = newGoalAmount,
            status = if (isNowCompleted) "COMPLETED" else "IN_PROGRESS"
        )

        val newBalance = currentAccount.balance - amount
        val updatedAccount = currentAccount.copy(balance = newBalance)
        accountDao.updateBalance(newBalance)
        savingsGoalDao.updateGoal(updatedGoal)

        val tx = TransactionEntity(
            title = "Aporte a Meta: ${goal.name}",
            amount = amount,
            type = "GOAL_DEPOSIT",
            category = "Ahorro",
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "Meta: ${goal.name}",
            referenceNumber = "OP-${Random.nextInt(100000, 999999)}",
            note = if (isNowCompleted) "¡Aporte final! Meta cumplida al 100%" else "Abono directo a meta de ahorro"
        )
        transactionDao.insertTransaction(tx)

        val uid = getCurrentUid()
        FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
        FirebaseManager.syncSavingsGoalToFirestore(updatedGoal, uid)
        FirebaseManager.syncTransactionToFirestore(tx, uid)

        return Result.success(Unit)
    }

    suspend fun createSavingsGoal(
        name: String,
        targetAmount: Double,
        initialAmount: Double,
        categoryIcon: String,
        targetDate: String
    ): Result<Unit> {
        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        if (initialAmount > 0 && currentAccount.balance < initialAmount) {
            return Result.failure(Exception("Saldo insuficiente para el depósito inicial"))
        }

        val color = when (categoryIcon) {
            "EMERGENCY" -> "#10B981"
            "TRAVEL" -> "#3B82F6"
            "TECH" -> "#8B5CF6"
            "HOME" -> "#F59E0B"
            "CAR" -> "#EC4899"
            else -> "#10B981"
        }

        val isCompleted = initialAmount >= targetAmount && targetAmount > 0
        val goalEntity = SavingsGoalEntity(
            name = name,
            targetAmount = targetAmount,
            currentAmount = initialAmount,
            categoryIcon = categoryIcon,
            targetDate = targetDate,
            colorHex = color,
            status = if (isCompleted) "COMPLETED" else "IN_PROGRESS"
        )
        val goalId = savingsGoalDao.insertGoal(goalEntity)
        val savedGoal = goalEntity.copy(id = goalId)

        val uid = getCurrentUid()
        FirebaseManager.syncSavingsGoalToFirestore(savedGoal, uid)

        if (initialAmount > 0) {
            val newBalance = currentAccount.balance - initialAmount
            val updatedAccount = currentAccount.copy(balance = newBalance)
            accountDao.updateBalance(newBalance)

            val tx = TransactionEntity(
                title = "Aporte Inicial: $name",
                amount = initialAmount,
                type = "GOAL_DEPOSIT",
                category = "Ahorro",
                timestamp = System.currentTimeMillis(),
                recipientOrSender = "Meta: $name",
                referenceNumber = "OP-${Random.nextInt(100000, 999999)}",
                note = "Depósito inicial de creación"
            )
            transactionDao.insertTransaction(tx)

            FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
            FirebaseManager.syncTransactionToFirestore(tx, uid)
        }

        return Result.success(Unit)
    }

    suspend fun updateSavingsGoal(
        goalId: Long,
        name: String,
        targetAmount: Double,
        categoryIcon: String,
        targetDate: String
    ): Result<Unit> {
        val existing = savingsGoalDao.getGoalById(goalId) ?: return Result.failure(Exception("Meta no encontrada"))
        val color = when (categoryIcon) {
            "EMERGENCY" -> "#10B981"
            "TRAVEL" -> "#3B82F6"
            "TECH" -> "#8B5CF6"
            "HOME" -> "#F59E0B"
            "CAR" -> "#EC4899"
            else -> "#10B981"
        }

        val isCompleted = existing.currentAmount >= targetAmount && targetAmount > 0
        val newStatus = if (existing.status == "WITHDRAWN") "WITHDRAWN" else if (isCompleted) "COMPLETED" else "IN_PROGRESS"

        val updated = existing.copy(
            name = name,
            targetAmount = targetAmount,
            categoryIcon = categoryIcon,
            targetDate = targetDate,
            colorHex = color,
            status = newStatus
        )
        savingsGoalDao.updateGoal(updated)

        val uid = getCurrentUid()
        FirebaseManager.syncSavingsGoalToFirestore(updated, uid)

        return Result.success(Unit)
    }

    suspend fun deleteSavingsGoal(goalId: Long, returnFundsToAccount: Boolean): Result<Unit> {
        val existing = savingsGoalDao.getGoalById(goalId) ?: return Result.failure(Exception("Meta no encontrada"))
        val uid = getCurrentUid()

        if (returnFundsToAccount && existing.currentAmount > 0) {
            val currentAccount = accountDao.getAccount()
            if (currentAccount != null) {
                val newBalance = currentAccount.balance + existing.currentAmount
                val updatedAccount = currentAccount.copy(balance = newBalance)
                accountDao.updateBalance(newBalance)

                val tx = TransactionEntity(
                    title = "Reintegro por Meta: ${existing.name}",
                    amount = existing.currentAmount,
                    type = "INCOME",
                    category = "Ahorro",
                    timestamp = System.currentTimeMillis(),
                    recipientOrSender = "Meta cancelada: ${existing.name}",
                    referenceNumber = "OP-${Random.nextInt(100000, 999999)}",
                    note = "Devolución de fondos de meta de ahorro eliminada"
                )
                transactionDao.insertTransaction(tx)

                FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
                FirebaseManager.syncTransactionToFirestore(tx, uid)
            }
        }

        savingsGoalDao.deleteGoal(goalId)
        FirebaseManager.deleteSavingsGoalFromFirestore(goalId, uid)

        return Result.success(Unit)
    }

    suspend fun withdrawFromSavingsGoal(goalId: Long, amount: Double? = null): Result<TransactionEntity> {
        val existing = savingsGoalDao.getGoalById(goalId) ?: return Result.failure(Exception("Meta no encontrada"))
        if (existing.status == "WITHDRAWN" || existing.currentAmount <= 0) {
            return Result.failure(Exception("Esta meta no tiene fondos disponibles para retirar."))
        }
        val withdrawAmount = amount ?: existing.currentAmount
        if (withdrawAmount <= 0) {
            return Result.failure(Exception("El monto a retirar debe ser mayor a S/ 0"))
        }
        if (withdrawAmount > existing.currentAmount) {
            return Result.failure(Exception("Fondos insuficientes en la meta de ahorro (Disponible: S/ %.2f)".format(existing.currentAmount)))
        }

        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        val newGoalAmount = (existing.currentAmount - withdrawAmount).coerceAtLeast(0.0)
        val isFullyWithdrawn = newGoalAmount <= 0.001
        val newStatus = if (isFullyWithdrawn) "WITHDRAWN" else if (newGoalAmount >= existing.targetAmount) "COMPLETED" else "IN_PROGRESS"

        val updatedGoal = existing.copy(
            currentAmount = newGoalAmount,
            status = newStatus
        )
        savingsGoalDao.updateGoal(updatedGoal)

        val newBalance = currentAccount.balance + withdrawAmount
        val updatedAccount = currentAccount.copy(balance = newBalance)
        accountDao.updateBalance(newBalance)

        val opCode = "MET-RET-${Random.nextInt(100000, 999999)}"
        val tx = TransactionEntity(
            title = "Retiro de Meta: ${existing.name}",
            amount = withdrawAmount,
            type = "INCOME",
            category = "Ahorro",
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "Meta: ${existing.name}",
            referenceNumber = opCode,
            note = if (isFullyWithdrawn) "Ahorro completado retirado y acreditado a tu cuenta disponible" else "Retiro parcial de fondos de meta"
        )
        val txId = transactionDao.insertTransaction(tx)
        val savedTx = tx.copy(id = txId)

        val uid = getCurrentUid()
        FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
        FirebaseManager.syncSavingsGoalToFirestore(updatedGoal, uid)
        FirebaseManager.syncTransactionToFirestore(savedTx, uid)

        return Result.success(savedTx)
    }

    suspend fun createBudget(
        category: String,
        monthlyLimit: Double,
        icon: String = "OTHER",
        colorHex: String = "#10B981"
    ): Result<Unit> {
        if (category.isBlank()) return Result.failure(Exception("Debe ingresar el nombre de la categoría"))
        if (monthlyLimit <= 0) return Result.failure(Exception("El límite mensual debe ser mayor a S/ 0"))

        val budgetEntity = BudgetEntity(
            category = category,
            monthlyLimit = monthlyLimit,
            spentAmount = 0.0,
            iconName = icon
        )
        val id = budgetDao.insertBudget(budgetEntity)
        val saved = budgetEntity.copy(id = id)

        val uid = getCurrentUid()
        FirebaseManager.syncBudgetToFirestore(saved, uid)

        return Result.success(Unit)
    }

    suspend fun updateBudget(
        id: Long,
        category: String,
        monthlyLimit: Double
    ): Result<Unit> {
        val existing = budgetDao.getBudgetById(id) ?: return Result.failure(Exception("Presupuesto no encontrado"))
        if (monthlyLimit <= 0) return Result.failure(Exception("El límite mensual debe ser mayor a S/ 0"))

        val updated = existing.copy(
            category = category,
            monthlyLimit = monthlyLimit
        )
        budgetDao.updateBudget(updated)

        val uid = getCurrentUid()
        FirebaseManager.syncBudgetToFirestore(updated, uid)

        return Result.success(Unit)
    }

    suspend fun deleteBudget(budgetId: Long): Result<Unit> {
        budgetDao.deleteBudget(budgetId)
        val uid = getCurrentUid()
        FirebaseManager.deleteBudgetFromFirestore(budgetId, uid)
        return Result.success(Unit)
    }

    suspend fun resetBudgetSpent(budgetId: Long): Result<Unit> {
        budgetDao.resetSpent(budgetId)
        val existing = budgetDao.getBudgetById(budgetId)
        if (existing != null) {
            val uid = getCurrentUid()
            FirebaseManager.syncBudgetToFirestore(existing.copy(spentAmount = 0.0), uid)
        }
        return Result.success(Unit)
    }

    suspend fun payService(
        serviceId: String = "",
        serviceName: String,
        supplyCode: String,
        amount: Double,
        commission: Double = 0.0,
        category: String = "Servicios",
        comment: String = "",
        userDni: String = "",
        userPhone: String = ""
    ): Result<String> {
        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        val totalPaid = amount + commission
        if (currentAccount.balance < totalPaid) {
            return Result.failure(Exception("Saldo insuficiente en su cuenta en Soles"))
        }

        val opCode = "OP-${Random.nextInt(100000, 999999)}"
        val newBalance = currentAccount.balance - totalPaid
        val updatedAccount = currentAccount.copy(balance = newBalance)
        accountDao.updateBalance(newBalance)

        val tx = TransactionEntity(
            title = "Pago de Servicio: $serviceName",
            amount = totalPaid,
            type = "EXPENSE",
            category = category,
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "$serviceName (Suministro: $supplyCode)",
            referenceNumber = opCode,
            note = if (comment.isNotBlank()) comment else "Pago de servicio $serviceName - Sum: $supplyCode"
        )
        transactionDao.insertTransaction(tx)
        budgetDao.addSpending(category, totalPaid)

        val uid = getCurrentUid()
        FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
        FirebaseManager.syncTransactionToFirestore(tx, uid)

        // Record structured ServicePayment document in Firestore
        val paymentRecord = ServicePayment(
            id = "sp_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}",
            uid = uid,
            accountHolder = currentAccount.accountHolder,
            userDni = userDni,
            userPhone = userPhone,
            serviceId = serviceId,
            serviceName = serviceName,
            serviceCategory = category,
            supplyCode = supplyCode,
            amount = amount,
            commission = commission,
            totalPaid = totalPaid,
            operationCode = opCode,
            comment = comment,
            status = "COMPLETED",
            timestamp = System.currentTimeMillis()
        )
        FirebaseManager.recordServicePayment(paymentRecord)

        return Result.success(opCode)
    }

    suspend fun withdrawFunds(
        amount: Double,
        withdrawalMethod: String,
        pinCode: String
    ): Result<String> {
        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Saldo insuficiente en su cuenta en Soles"))
        }

        val opCode = "OP-${Random.nextInt(100000, 999999)}"
        val newBalance = currentAccount.balance - amount
        val updatedAccount = currentAccount.copy(balance = newBalance)
        accountDao.updateBalance(newBalance)

        val tx = TransactionEntity(
            title = "Retiro de Efectivo ($withdrawalMethod)",
            amount = amount,
            type = "EXPENSE",
            category = "Otro",
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "Cajero Automático / Agente (Clave: $pinCode)",
            referenceNumber = opCode,
            note = "Retiro sin tarjeta con clave temporal $pinCode"
        )
        transactionDao.insertTransaction(tx)

        val uid = getCurrentUid()
        FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
        FirebaseManager.syncTransactionToFirestore(tx, uid)

        return Result.success(opCode)
    }

    suspend fun reserveWithdrawalFunds(
        amount: Double,
        pinCode: String,
        opCode: String,
        qrData: String,
        dni: String,
        phone: String
    ): Result<CloudWithdrawal> {
        val currentAccount = accountDao.getAccount() ?: return Result.failure(Exception("Cuenta no encontrada"))
        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Saldo insuficiente para retener"))
        }
        val newBalance = currentAccount.balance - amount
        val updatedAccount = currentAccount.copy(balance = newBalance)
        accountDao.updateBalance(newBalance)

        val tx = TransactionEntity(
            title = "Retiro Generado (Retención 1h)",
            amount = amount,
            type = "EXPENSE",
            category = "Otro",
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "Cajero BC-BANK (Clave: $pinCode)",
            referenceNumber = opCode,
            note = "Saldo retenido por 1 hora para retiro en cajero"
        )
        transactionDao.insertTransaction(tx)

        val uid = getCurrentUid()
        FirebaseManager.syncAccountToFirestore(updatedAccount, uid)
        FirebaseManager.syncTransactionToFirestore(tx, uid)

        val cloudRes = FirebaseManager.createPendingWithdrawal(
            uid = uid,
            userDni = dni,
            userPhone = phone,
            accountHolder = currentAccount.accountHolder,
            amount = amount,
            pinCode = pinCode,
            opCode = opCode,
            qrData = qrData
        )
        return cloudRes
    }

    suspend fun refundCancelledWithdrawal(
        withdrawal: CloudWithdrawal
    ): Result<Unit> {
        val uid = getCurrentUid()
        
        // 1. Update the original withdrawal transaction so it is marked as cancelled
        transactionDao.updateTransactionByReference(
            ref = withdrawal.opCode,
            newTitle = "Retiro Cancelado y Reintegrado",
            newNote = "Orden de retiro ${withdrawal.opCode} cancelada. Saldo liberado e integrado a tu cuenta."
        )

        // 2. Insert the income reimbursement transaction
        val refundTx = TransactionEntity(
            title = "Reintegro por Retiro Cancelado",
            amount = withdrawal.amount,
            type = "INCOME",
            category = "Otro",
            timestamp = System.currentTimeMillis(),
            recipientOrSender = "Cancelación de Retiro - ${withdrawal.opCode}",
            referenceNumber = "REF-${withdrawal.opCode}",
            note = "Saldo de S/ ${String.format(Locale.US, "%.2f", withdrawal.amount)} liberado y reintegrado a la cuenta"
        )
        transactionDao.insertTransaction(refundTx)

        // 3. Mark the withdrawal order in cloud as CANCELLED
        FirebaseManager.cancelPendingWithdrawal(withdrawal.id, uid)
        FirebaseManager.syncTransactionToFirestore(refundTx, uid)

        // 4. Recalculate balance dynamically from all transactions
        recalculateAndSyncBalance()

        return Result.success(Unit)
    }

    suspend fun updateBudgetLimit(category: String, newLimit: Double): Result<Unit> {
        budgetDao.updateLimit(category, newLimit)
        return Result.success(Unit)
    }

    suspend fun getTransactionByReference(ref: String): TransactionEntity? {
        return transactionDao.getTransactionByReference(ref)
    }

    suspend fun insertOrUpdateAccount(account: AccountInfoEntity) {
        accountDao.insertOrUpdate(account)
    }

    suspend fun getAccount(): AccountInfoEntity? {
        return accountDao.getAccount()
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }
}
