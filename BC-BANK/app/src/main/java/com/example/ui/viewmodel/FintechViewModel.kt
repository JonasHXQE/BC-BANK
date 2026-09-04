package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AccountSecurityStatus
import com.example.data.firebase.AppSystemConfig
import com.example.data.firebase.BlockedDeviceInfo
import com.example.data.firebase.CloudWithdrawal
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.SupportChannel
import com.example.data.firebase.UserCloudData
import com.example.data.local.AccountInfoEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BankNotificationEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.SessionManager
import com.example.data.local.TransactionEntity
import com.example.data.repository.FintechRepository
import com.example.ui.components.AlertType
import com.example.ui.components.AppAlert
import com.example.ui.components.GoogleAuthState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FintechViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FintechRepository(
        accountDao = db.accountDao(),
        transactionDao = db.transactionDao(),
        savingsGoalDao = db.savingsGoalDao(),
        budgetDao = db.budgetDao(),
        notificationDao = db.notificationDao()
    )
    val sessionManager = SessionManager(application)

    // Session & Navigation States
    private val _sessionState = MutableStateFlow(SessionState.SPLASH)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _activeWindow = MutableStateFlow<ActiveWindow>(ActiveWindow.None)
    val activeWindow: StateFlow<ActiveWindow> = _activeWindow.asStateFlow()

    // Auth States
    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _googleAuthState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.Idle)
    val googleAuthState: StateFlow<GoogleAuthState> = _googleAuthState.asStateFlow()

    // UI Feedback & Alerts
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _activeAlert = MutableStateFlow<AppAlert?>(null)
    val activeAlert: StateFlow<AppAlert?> = _activeAlert.asStateFlow()

    // Privacy & Preferences
    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden: StateFlow<Boolean> = _isBalanceHidden.asStateFlow()

    private val _transactionFilter = MutableStateFlow("ALL")
    val transactionFilter: StateFlow<String> = _transactionFilter.asStateFlow()

    // Dialog States
    private val _showAddGoalDialog = MutableStateFlow(false)
    val showAddGoalDialog: StateFlow<Boolean> = _showAddGoalDialog.asStateFlow()

    private val _goalForDeposit = MutableStateFlow<SavingsGoalEntity?>(null)
    val goalForDeposit: StateFlow<SavingsGoalEntity?> = _goalForDeposit.asStateFlow()

    private val _goalForEdit = MutableStateFlow<SavingsGoalEntity?>(null)
    val goalForEdit: StateFlow<SavingsGoalEntity?> = _goalForEdit.asStateFlow()

    private val _goalForDelete = MutableStateFlow<SavingsGoalEntity?>(null)
    val goalForDelete: StateFlow<SavingsGoalEntity?> = _goalForDelete.asStateFlow()

    private val _goalForWithdraw = MutableStateFlow<SavingsGoalEntity?>(null)
    val goalForWithdraw: StateFlow<SavingsGoalEntity?> = _goalForWithdraw.asStateFlow()

    private val _showAddBudgetDialog = MutableStateFlow(false)
    val showAddBudgetDialog: StateFlow<Boolean> = _showAddBudgetDialog.asStateFlow()

    private val _budgetForEdit = MutableStateFlow<BudgetEntity?>(null)
    val budgetForEdit: StateFlow<BudgetEntity?> = _budgetForEdit.asStateFlow()

    private val _budgetForDelete = MutableStateFlow<BudgetEntity?>(null)
    val budgetForDelete: StateFlow<BudgetEntity?> = _budgetForDelete.asStateFlow()

    private val _transferReceipt = MutableStateFlow<TransferReceipt?>(null)
    val transferReceipt: StateFlow<TransferReceipt?> = _transferReceipt.asStateFlow()

    // Firebase System & Security Overlays
    private val _accountSecurityStatus = MutableStateFlow<AccountSecurityStatus?>(null)
    val accountSecurityStatus: StateFlow<AccountSecurityStatus?> = _accountSecurityStatus.asStateFlow()

    private val _supportChannels = MutableStateFlow<List<SupportChannel>>(emptyList())
    val supportChannels: StateFlow<List<SupportChannel>> = _supportChannels.asStateFlow()

    private val _systemConfig = MutableStateFlow<AppSystemConfig?>(null)
    val systemConfig: StateFlow<AppSystemConfig?> = _systemConfig.asStateFlow()

    private val _deviceBlock = MutableStateFlow<BlockedDeviceInfo?>(null)
    val deviceBlock: StateFlow<BlockedDeviceInfo?> = _deviceBlock.asStateFlow()

    private val _activeWithdrawalReservation = MutableStateFlow<CloudWithdrawal?>(null)
    val activeWithdrawalReservation: StateFlow<CloudWithdrawal?> = _activeWithdrawalReservation.asStateFlow()

    // User details state
    private val _userPhone = MutableStateFlow(sessionManager.getUserPhone())
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userEmail = MutableStateFlow(sessionManager.getUserEmail())
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userDni = MutableStateFlow(sessionManager.getUserDni())
    val userDni: StateFlow<String> = _userDni.asStateFlow()

    private val _userAccountType = MutableStateFlow(sessionManager.getAccountType())
    val userAccountType: StateFlow<String> = _userAccountType.asStateFlow()

    // Repository Flows
    val account: StateFlow<AccountInfoEntity?> = repository.accountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val rawTransactions: StateFlow<List<TransactionEntity>> = repository.transactionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.transactionsFlow,
        _transactionFilter
    ) { txs, filter ->
        when (filter) {
            "INCOME" -> txs.filter { it.type == "INCOME" }
            "EXPENSE" -> txs.filter { it.type == "EXPENSE" || it.type == "GOAL_DEPOSIT" }
            else -> txs
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.savingsGoalsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val budgets: StateFlow<List<BudgetEntity>> = repository.budgetsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<BankNotificationEntity>> = repository.getNotificationsFlow(
        FirebaseManager.getCurrentUserUid() ?: ""
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> =
        repository.getUnreadNotificationsCountFlow(FirebaseManager.getCurrentUserUid() ?: "").stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val savedDni: String
        get() = sessionManager.getUserDni()

    val savedEmail: String
        get() = sessionManager.getUserEmail()

    fun getUserEmail(): String = sessionManager.getUserEmail()
    fun getUserName(): String = sessionManager.getUserName().ifEmpty { "Usuario" }
    fun isBiometricEnabled(): Boolean = sessionManager.isBiometricEnabled()
    fun isPushNotificationsEnabled(): Boolean = true

    init {
        checkSystemConfigAndSecurity()
    }

    private fun checkSystemConfigAndSecurity() {
        viewModelScope.launch {
            try {
                FirebaseManager.listenToAppSystemConfig { config ->
                    _systemConfig.value = config
                }
                FirebaseManager.listenToSupportChannels { channels ->
                    _supportChannels.value = channels
                }
                val uid = FirebaseManager.getCurrentUserUid()
                if (uid != null) {
                    FirebaseManager.listenToUserAccountStatus(uid) { status ->
                        _accountSecurityStatus.value = status
                    }
                }
            } catch (e: Exception) {
                Log.w("FintechViewModel", "Error setting up system config listeners: ${e.message}")
            }
        }
    }

    fun onSplashFinished() {
        viewModelScope.launch {
            val isLogged = sessionManager.isLoggedIn()
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            if (isLogged && firebaseUser != null) {
                // If logged in, require PIN unlock for banking security
                val pin = sessionManager.getUserPin()
                if (pin.isNotBlank()) {
                    _sessionState.value = SessionState.LOCKED
                } else {
                    val uid = firebaseUser.uid
                    val cloudPin = FirebaseManager.getUserSecurityPin(uid)
                    if (!cloudPin.isNullOrBlank()) {
                        sessionManager.setUserPin(cloudPin)
                        _sessionState.value = SessionState.LOCKED
                    } else {
                        _sessionState.value = SessionState.AUTHENTICATED
                    }
                }
                repository.checkAndSeedInitialData()
            } else {
                _sessionState.value = SessionState.AUTH
            }
        }
    }

    // --- Authentication Flow ---

    fun login(documentOrEmail: String, pass: String, remember: Boolean) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null

            try {
                val trimmedInput = documentOrEmail.trim()
                val isDni = trimmedInput.length == 8 && trimmedInput.all { it.isDigit() }

                val loginResult = FirebaseManager.signInWithFirebase(trimmedInput, pass)
                if (loginResult.isFailure) {
                    throw loginResult.exceptionOrNull() ?: Exception("Credenciales incorrectas")
                }
                val (uid, actualEmail) = loginResult.getOrThrow()

                // 2. Fetch user data and PIN from Firestore
                val cloudData = repository.syncWithCloud(uid)

                // Retrieve 6-digit PIN securely from Firestore
                var userPin = FirebaseManager.getUserSecurityPin(uid)?.trim() ?: ""
                if (userPin.isBlank() && cloudData?.securityPin?.isNotBlank() == true) {
                    userPin = cloudData.securityPin.trim()
                }
                if (userPin.isBlank()) {
                    userPin = sessionManager.getUserPin().trim()
                }

                if (userPin.isNotBlank()) {
                    sessionManager.setUserPin(userPin)
                }

                _userEmail.value = actualEmail
                sessionManager.setUserEmail(actualEmail)
                sessionManager.setRememberSession(remember)
                if (isDni) {
                    sessionManager.setUserDni(trimmedInput)
                    _userDni.value = trimmedInput
                } else if (cloudData?.dni?.isNotBlank() == true) {
                    sessionManager.setUserDni(cloudData.dni)
                    _userDni.value = cloudData.dni
                }

                _authLoading.value = false

                // 3. Post-Login 6-Digit PIN Requirement (mirroring Google login flow)
                val holderName = cloudData?.account?.accountHolder
                    ?: sessionManager.getUserName().ifEmpty { actualEmail.substringBefore("@") }

                _googleAuthState.value = GoogleAuthState.RequirePin(
                    uid = uid,
                    email = actualEmail,
                    displayName = holderName,
                    expectedPin = userPin,
                    cloudData = cloudData
                )

            } catch (e: Exception) {
                _authLoading.value = false
                val errorMsg = when {
                    e.message?.contains("password", ignoreCase = true) == true -> "Contraseña incorrecta. Verifica tus datos."
                    e.message?.contains("user-not-found", ignoreCase = true) == true -> "Usuario no registrado en BC-BANK."
                    e.message?.contains("network", ignoreCase = true) == true -> "Error de red. Verifica tu conexión a internet."
                    else -> e.localizedMessage ?: "Error al iniciar sesión en BC-BANK."
                }
                _authError.value = errorMsg
            }
        }
    }

    fun completeGoogleLoginWithPin(enteredPin: String) {
        viewModelScope.launch {
            val currentState = _googleAuthState.value
            if (currentState !is GoogleAuthState.RequirePin) return@launch

            val cleanEnteredPin = enteredPin.trim()
            var expectedPin = currentState.expectedPin.trim()

            if (expectedPin.isBlank()) {
                val cloudPin = FirebaseManager.getUserSecurityPin(currentState.uid)?.trim()
                if (!cloudPin.isNullOrBlank()) {
                    expectedPin = cloudPin
                } else {
                    expectedPin = sessionManager.getUserPin().trim()
                }
            }

            val isValid = if (expectedPin.isNotBlank()) {
                val cleanExp = expectedPin.replace(".0", "").trim()
                cleanEnteredPin == cleanExp ||
                cleanEnteredPin.padStart(6, '0') == cleanExp.padStart(6, '0') ||
                cleanEnteredPin == cleanExp.take(6) ||
                (cleanEnteredPin.toIntOrNull() != null && cleanEnteredPin.toIntOrNull() == cleanExp.toIntOrNull())
            } else {
                cleanEnteredPin.length == 6 && cleanEnteredPin.all { it.isDigit() }
            }

            if (!isValid) {
                _toastEvent.emit("PIN de seguridad incorrecto. Inténtalo de nuevo.")
                return@launch
            }

            // Successfully verified 6-digit PIN!
            sessionManager.setUserPin(cleanEnteredPin)
            sessionManager.setLoggedIn(
                loggedIn = true,
                dni = sessionManager.getUserDni(),
                phone = sessionManager.getUserPhone(),
                email = currentState.email,
                name = currentState.displayName,
                accountType = sessionManager.getAccountType(),
                pin = cleanEnteredPin
            )

            // Keep Firebase updated
            FirebaseManager.updateUserPin(currentState.uid, cleanEnteredPin)

            _userEmail.value = currentState.email
            _userNameOrHolder(currentState.displayName)

            _googleAuthState.value = GoogleAuthState.Idle
            _sessionState.value = SessionState.AUTHENTICATED
            _toastEvent.emit("¡Bienvenido a BC-BANK, ${currentState.displayName}!")

            repository.checkAndSeedInitialData()
        }
    }

    private fun _userNameOrHolder(name: String) {
        if (name.isNotBlank()) {
            sessionManager.setUserName(name)
        }
    }

    fun registerAccount(email: String, pass: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            try {
                val authResult = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email.trim(), pass)
                    .await()

                val user = authResult.user
                user?.sendEmailVerification()

                sessionManager.setUserEmail(email.trim())
                _userEmail.value = email.trim()
                _authLoading.value = false

                _sessionState.value = SessionState.ONBOARDING
                _toastEvent.emit("Cuenta creada con éxito. Completa tu perfil.")
            } catch (e: Exception) {
                _authLoading.value = false
                _authError.value = e.localizedMessage ?: "Error al crear cuenta."
            }
        }
    }

    fun loginWithGoogle(idToken: String, email: String, displayName: String) {
        viewModelScope.launch {
            _googleAuthState.value = GoogleAuthState.Processing("Verificando credenciales de Google...")
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                val uid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID de Google")

                val cloudData = repository.syncWithCloud(uid)
                var userPin = FirebaseManager.getUserSecurityPin(uid)?.trim() ?: ""
                if (userPin.isBlank() && cloudData?.securityPin?.isNotBlank() == true) {
                    userPin = cloudData.securityPin.trim()
                }

                if (userPin.isNotBlank()) {
                    sessionManager.setUserPin(userPin)
                }

                _googleAuthState.value = GoogleAuthState.RequirePin(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    expectedPin = userPin,
                    cloudData = cloudData
                )
            } catch (e: Exception) {
                _googleAuthState.value = GoogleAuthState.Error(e.localizedMessage ?: "Error al autenticar con Google")
            }
        }
    }

    fun setGoogleAuthProcessing(step: String) {
        _googleAuthState.value = GoogleAuthState.Processing(step)
    }

    fun setGoogleAuthCancelled(reason: String) {
        _googleAuthState.value = GoogleAuthState.Cancelled(reason)
    }

    fun setGoogleAuthError(errorMsg: String) {
        _googleAuthState.value = GoogleAuthState.Error(errorMsg)
    }

    fun dismissGoogleAuthError() {
        _googleAuthState.value = GoogleAuthState.Idle
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim()).await()
                _toastEvent.emit("Correo de restablecimiento enviado a $email")
            } catch (e: Exception) {
                _toastEvent.emit("Error: ${e.localizedMessage ?: "No se pudo enviar el correo"}")
            }
        }
    }

    fun checkEmailVerificationStatus() {
        viewModelScope.launch {
            _authLoading.value = true
            try {
                val user = FirebaseAuth.getInstance().currentUser
                user?.reload()?.await()
                if (user?.isEmailVerified == true) {
                    sessionManager.setEmailVerified(true)
                    _sessionState.value = SessionState.ONBOARDING
                    _toastEvent.emit("¡Correo verificado con éxito!")
                } else {
                    _toastEvent.emit("El correo aún no ha sido verificado. Revisa tu bandeja.")
                }
            } catch (e: Exception) {
                _toastEvent.emit("Error al verificar correo: ${e.message}")
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                user?.sendEmailVerification()?.await()
                _toastEvent.emit("Correo de verificación reenviado")
            } catch (e: Exception) {
                _toastEvent.emit("Error al reenviar correo: ${e.message}")
            }
        }
    }

    fun completeOnboarding(
        accountType: String,
        fullName: String,
        dni: String,
        phone: String,
        pin: String,
        isBusiness: Boolean,
        businessName: String,
        businessRuc: String,
        birthDate: String,
        isKid: Boolean,
        age: Int
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            try {
                val uid = FirebaseManager.getCurrentUserUid() ?: throw Exception("Usuario no autenticado")
                val cleanPin = pin.trim()

                // 1. Setup local room data
                repository.setupNewUser(
                    uid = uid,
                    fullName = fullName.trim(),
                    dni = dni.trim(),
                    accountType = accountType,
                    initialBalance = 1500.00
                )

                // 2. Save in SessionManager
                sessionManager.setUserPin(cleanPin)
                sessionManager.setUserDni(dni.trim())
                sessionManager.setUserPhone(phone.trim())
                sessionManager.setUserName(fullName.trim())
                sessionManager.setAccountType(accountType)
                sessionManager.setLoggedIn(
                    loggedIn = true,
                    dni = dni.trim(),
                    phone = phone.trim(),
                    email = sessionManager.getUserEmail(),
                    name = fullName.trim(),
                    accountType = accountType,
                    pin = cleanPin
                )

                _userDni.value = dni.trim()
                _userPhone.value = phone.trim()
                _userAccountType.value = accountType

                // 3. Sync profile and 6-digit PIN to Firestore
                FirebaseManager.updateUserPin(uid, cleanPin)

                _authLoading.value = false
                _sessionState.value = SessionState.AUTHENTICATED
                _toastEvent.emit("¡Bienvenido a BC-BANK! Tu cuenta ha sido activada.")
            } catch (e: Exception) {
                _authLoading.value = false
                _authError.value = e.localizedMessage ?: "Error al configurar perfil"
            }
        }
    }

    // --- PIN & Unlock Session ---

    fun unlockWithPin(pin: String) {
        viewModelScope.launch {
            val cleanPin = pin.trim()
            var currentPin = sessionManager.getUserPin().trim()

            if (currentPin.isBlank()) {
                val uid = FirebaseManager.getCurrentUserUid()
                if (uid != null) {
                    currentPin = FirebaseManager.getUserSecurityPin(uid)?.trim() ?: ""
                    if (currentPin.isNotBlank()) {
                        sessionManager.setUserPin(currentPin)
                    }
                }
            }

            val isValid = if (currentPin.isNotBlank()) {
                val cleanExp = currentPin.replace(".0", "").trim()
                cleanPin == cleanExp ||
                cleanPin.padStart(6, '0') == cleanExp.padStart(6, '0') ||
                cleanPin == cleanExp.take(6) ||
                (cleanPin.toIntOrNull() != null && cleanPin.toIntOrNull() == cleanExp.toIntOrNull())
            } else {
                cleanPin.length == 6 && cleanPin.all { it.isDigit() }
            }

            if (isValid) {
                _sessionState.value = SessionState.AUTHENTICATED
            } else {
                _toastEvent.emit("PIN de seguridad incorrecto")
            }
        }
    }

    fun unlockWithBiometrics() {
        _sessionState.value = SessionState.AUTHENTICATED
    }

    fun lockSession() {
        _sessionState.value = SessionState.LOCKED
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            sessionManager.clearSession()
            repository.clearLocalData()
            _activeWindow.value = ActiveWindow.None
            _currentTab.value = NavigationTab.DASHBOARD
            _sessionState.value = SessionState.AUTH
            _toastEvent.emit("Sesión cerrada con éxito")
        }
    }

    fun logoutWithPin(pin: String) {
        if (sessionManager.validatePin(pin)) {
            logout()
        } else {
            viewModelScope.launch {
                _toastEvent.emit("PIN incorrecto. No se pudo cerrar sesión.")
            }
        }
    }

    fun deleteAccountWithPin(pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!sessionManager.validatePin(pin)) {
                onResult(false, "PIN incorrecto. Operación cancelada.")
                return@launch
            }
            try {
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()?.await()
                sessionManager.clearSession()
                repository.clearLocalData()
                _sessionState.value = SessionState.AUTH
                onResult(true, "Tu cuenta ha sido eliminada permanentemente.")
            } catch (e: Exception) {
                onResult(false, "Error al eliminar cuenta: ${e.message}")
            }
        }
    }

    // --- Navigation & Window Management ---

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
        _activeWindow.value = ActiveWindow.None
    }

    fun closeActiveWindow() {
        _activeWindow.value = ActiveWindow.None
    }

    fun openTransferWindow() {
        _activeWindow.value = ActiveWindow.Transfer()
    }

    fun openTransferWithData(recipient: String, identifier: String, amount: Double? = null) {
        _activeWindow.value = ActiveWindow.Transfer(recipient, identifier, amount)
    }

    fun openDepositWindow() {
        _activeWindow.value = ActiveWindow.Deposit
    }

    fun openPayServicesWindow() {
        _activeWindow.value = ActiveWindow.PayServices
    }

    fun openWithdrawQrWindow() {
        _activeWindow.value = ActiveWindow.WithdrawQr
    }

    fun openProfileWindow() {
        _activeWindow.value = ActiveWindow.Profile
    }

    fun openNotificationsWindow() {
        _activeWindow.value = ActiveWindow.Notifications
    }

    fun openTransactionDetail(transaction: TransactionEntity) {
        _activeWindow.value = ActiveWindow.TransactionDetail(transaction)
    }

    fun toggleBalancePrivacy() {
        _isBalanceHidden.value = !_isBalanceHidden.value
    }

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    // --- Financial Operations ---

    fun transferMoney(
        recipient: String,
        phone: String,
        amount: Double,
        concept: String,
        category: String
    ) {
        viewModelScope.launch {
            val result = repository.transferMoney(recipient, phone, amount, concept, category)
            result.onSuccess { opCode ->
                _transferReceipt.value = TransferReceipt(
                    recipient = recipient,
                    accountOrPhone = phone,
                    amount = amount,
                    concept = concept,
                    category = category,
                    operationCode = opCode
                )
                _activeWindow.value = ActiveWindow.None
                _toastEvent.emit("¡Transferencia exitosa de S/ $amount!")
            }.onFailure { e ->
                showAlert(AppAlert("Error en Transferencia", e.message ?: "Fondos insuficientes", AlertType.ERROR))
            }
        }
    }

    fun payService(
        serviceId: String,
        serviceName: String,
        supplyCode: String,
        amount: Double,
        commission: Double,
        category: String,
        comment: String
    ) {
        viewModelScope.launch {
            val total = amount + commission
            val result = repository.payService(
                serviceId = serviceId,
                serviceName = serviceName,
                supplyCode = supplyCode,
                amount = amount,
                commission = commission,
                category = category,
                comment = comment,
                userDni = sessionManager.getUserDni(),
                userPhone = sessionManager.getUserPhone()
            )
            result.onSuccess { opCode ->
                _transferReceipt.value = TransferReceipt(
                    recipient = serviceName,
                    accountOrPhone = supplyCode,
                    amount = total,
                    concept = "Pago de servicio: $serviceName ($comment)",
                    category = category,
                    operationCode = opCode
                )
                _activeWindow.value = ActiveWindow.None
                _toastEvent.emit("¡Servicio $serviceName pagado exitosamente!")
            }.onFailure { e ->
                showAlert(AppAlert("Error al Pagar Servicio", e.message ?: "No se pudo procesar el pago", AlertType.ERROR))
            }
        }
    }

    fun createWithdrawalReservation(amount: Double, pin: String, opCode: String) {
        viewModelScope.launch {
            if (!sessionManager.validatePin(pin)) {
                showAlert(AppAlert("Seguridad", "PIN incorrecto", AlertType.ERROR))
                return@launch
            }
            val res = repository.reserveWithdrawalFunds(
                amount = amount,
                pinCode = pin,
                opCode = opCode,
                qrData = "BCBANK_WITHDRAW_$opCode",
                dni = sessionManager.getUserDni(),
                phone = sessionManager.getUserPhone()
            )
            res.onSuccess { withdrawal ->
                _activeWithdrawalReservation.value = withdrawal
                _toastEvent.emit("Reserva de retiro por S/ $amount generada")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "Error al reservar retiro", AlertType.ERROR))
            }
        }
    }

    fun cancelWithdrawalReservation(pin1: String, pin2: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val withdrawal = _activeWithdrawalReservation.value
            if (withdrawal != null) {
                val res = repository.refundCancelledWithdrawal(withdrawal)
                res.onSuccess {
                    _activeWithdrawalReservation.value = null
                    onResult(true, "Reserva de retiro cancelada y saldo devuelto.")
                }.onFailure { e ->
                    onResult(false, e.message ?: "Error al cancelar")
                }
            } else {
                onResult(false, "No hay reserva de retiro activa")
            }
        }
    }

    fun cancelWithdrawalByOpCode(opCode: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val withdrawal = _activeWithdrawalReservation.value
            if (withdrawal != null && withdrawal.opCode == opCode) {
                val res = repository.refundCancelledWithdrawal(withdrawal)
                res.onSuccess {
                    _activeWithdrawalReservation.value = null
                    onResult(true, "Retiro cancelado exitosamente.")
                }.onFailure { e ->
                    onResult(false, e.message ?: "No se pudo cancelar el retiro.")
                }
            } else {
                onResult(false, "No se encontró el retiro correspondiente.")
            }
        }
    }

    fun withdrawFunds(amount: Double, method: String, pinCode: String) {
        viewModelScope.launch {
            if (!sessionManager.validatePin(pinCode)) {
                showAlert(AppAlert("Seguridad", "PIN incorrecto", AlertType.ERROR))
                return@launch
            }
            val res = repository.withdrawFunds(amount, method, pinCode)
            res.onSuccess { op ->
                _activeWindow.value = ActiveWindow.None
                _toastEvent.emit("Retiro de S/ $amount completado ($op)")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "Error al retirar fondos", AlertType.ERROR))
            }
        }
    }

    // --- Savings Goals Operations ---

    fun openAddGoalDialog() { _showAddGoalDialog.value = true }
    fun closeAddGoalDialog() { _showAddGoalDialog.value = false }

    fun openGoalDeposit(goal: SavingsGoalEntity) { _goalForDeposit.value = goal }
    fun closeGoalDeposit() { _goalForDeposit.value = null }

    fun openEditGoalDialog(goal: SavingsGoalEntity) { _goalForEdit.value = goal }
    fun closeEditGoalDialog() { _goalForEdit.value = null }

    fun openDeleteGoalDialog(goal: SavingsGoalEntity) { _goalForDelete.value = goal }
    fun closeDeleteGoalDialog() { _goalForDelete.value = null }

    fun openWithdrawGoalDialog(goal: SavingsGoalEntity) { _goalForWithdraw.value = goal }
    fun closeWithdrawGoalDialog() { _goalForWithdraw.value = null }

    fun createSavingsGoal(name: String, targetAmount: Double, initialAmount: Double, categoryIcon: String, targetDate: String) {
        viewModelScope.launch {
            val res = repository.createSavingsGoal(name, targetAmount, initialAmount, categoryIcon, targetDate)
            res.onSuccess {
                closeAddGoalDialog()
                _toastEvent.emit("Meta '$name' creada exitosamente")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "No se pudo crear la meta", AlertType.ERROR))
            }
        }
    }

    fun depositToSavingsGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            val res = repository.depositToSavingsGoal(goalId, amount)
            res.onSuccess {
                closeGoalDeposit()
                _toastEvent.emit("Abono de S/ $amount registrado a la meta")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "No se pudo abonar", AlertType.ERROR))
            }
        }
    }

    fun updateSavingsGoal(goalId: Long, name: String, targetAmount: Double, categoryIcon: String, targetDate: String) {
        viewModelScope.launch {
            val res = repository.updateSavingsGoal(
                goalId = goalId,
                name = name,
                targetAmount = targetAmount,
                categoryIcon = categoryIcon,
                targetDate = targetDate
            )
            res.onSuccess {
                closeEditGoalDialog()
                _toastEvent.emit("Meta actualizada")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "No se pudo actualizar la meta", AlertType.ERROR))
            }
        }
    }

    fun deleteSavingsGoal(goalId: Long, returnFunds: Boolean) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goalId, returnFunds)
            closeDeleteGoalDialog()
            _toastEvent.emit("Meta eliminada")
        }
    }

    fun withdrawFromSavingsGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            val res = repository.withdrawFromSavingsGoal(goalId, amount)
            res.onSuccess {
                closeWithdrawGoalDialog()
                _toastEvent.emit("Retiro de S/ $amount transferido a tu saldo principal")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "No se pudo retirar", AlertType.ERROR))
            }
        }
    }

    // --- Budgets Operations ---

    fun openAddBudgetDialog() { _showAddBudgetDialog.value = true }
    fun closeAddBudgetDialog() { _showAddBudgetDialog.value = false }

    fun openBudgetEdit(budget: BudgetEntity) { _budgetForEdit.value = budget }
    fun closeBudgetEdit() { _budgetForEdit.value = null }

    fun openDeleteBudgetDialog(budget: BudgetEntity) { _budgetForDelete.value = budget }
    fun closeDeleteBudgetDialog() { _budgetForDelete.value = null }

    fun createBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val res = repository.createBudget(category, limit)
            res.onSuccess {
                closeAddBudgetDialog()
                _toastEvent.emit("Presupuesto de $category establecido")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "Error al crear presupuesto", AlertType.ERROR))
            }
        }
    }

    fun updateBudget(budgetId: Long, category: String, newLimit: Double) {
        viewModelScope.launch {
            val res = repository.updateBudget(budgetId, category, newLimit)
            res.onSuccess {
                closeBudgetEdit()
                _toastEvent.emit("Presupuesto actualizado")
            }.onFailure { e ->
                showAlert(AppAlert("Error", e.message ?: "No se pudo actualizar", AlertType.ERROR))
            }
        }
    }

    fun resetBudgetSpent(budgetId: Long) {
        viewModelScope.launch {
            repository.resetBudgetSpent(budgetId)
            closeBudgetEdit()
            _toastEvent.emit("Consumo reiniciado a S/ 0.00")
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId)
            closeDeleteBudgetDialog()
            _toastEvent.emit("Presupuesto eliminado")
        }
    }

    // --- Notifications & Profile Actions ---

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch { repository.markNotificationAsRead(id) }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            val uid = FirebaseManager.getCurrentUserUid() ?: return@launch
            repository.markAllNotificationsAsRead(uid)
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch { repository.deleteNotification(id) }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val uid = FirebaseManager.getCurrentUserUid() ?: return@launch
            repository.clearAllNotifications(uid)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sessionManager.setBiometricEnabled(enabled)
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        // Save preference
    }

    fun updateUserPin(newPin: String) {
        viewModelScope.launch {
            val clean = newPin.trim()
            if (clean.length == 6 && clean.all { it.isDigit() }) {
                sessionManager.setUserPin(clean)
                val uid = FirebaseManager.getCurrentUserUid()
                if (uid != null) {
                    FirebaseManager.updateUserPin(uid, clean)
                }
                _toastEvent.emit("PIN de 6 dígitos actualizado correctamente")
            } else {
                showAlert(AppAlert("Error", "El PIN debe tener exactamente 6 dígitos numéricos", AlertType.ERROR))
            }
        }
    }

    fun updateUserPhone(newPhone: String, pin1: String, pin2: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (!sessionManager.validatePin(pin1)) {
                onResult(false, "PIN de seguridad incorrecto")
                return@launch
            }
            val cleanPhone = newPhone.trim()
            if (cleanPhone.length == 9 && cleanPhone.all { it.isDigit() }) {
                sessionManager.setUserPhone(cleanPhone)
                _userPhone.value = cleanPhone
                val uid = FirebaseManager.getCurrentUserUid()
                if (uid != null) {
                    FirebaseManager.updateUserPhone(uid, cleanPhone, sessionManager.getUserDni())
                }
                onResult(true, "Número de celular actualizado a $cleanPhone")
            } else {
                onResult(false, "El celular debe contener 9 dígitos numéricos")
            }
        }
    }

    fun dismissReceipt() {
        _transferReceipt.value = null
    }

    fun dismissAlert() {
        _activeAlert.value = null
    }

    fun showAlert(alert: AppAlert) {
        _activeAlert.value = alert
    }
}
