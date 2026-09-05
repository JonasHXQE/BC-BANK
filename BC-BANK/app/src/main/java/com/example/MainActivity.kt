package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.GoogleAuthHelper
import kotlinx.coroutines.launch
import com.example.ui.components.AccountStatusOverlay
import com.example.ui.components.AppStatusGateOverlay
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddSavingsGoalDialog
import com.example.ui.components.AlertType
import com.example.ui.components.AppAlert
import com.example.ui.components.DeleteBudgetDialog
import com.example.ui.components.DeleteSavingsGoalDialog
import com.example.ui.components.DepositToGoalDialog
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.EditSavingsGoalDialog
import com.example.ui.components.GoogleAuthProgressDialog
import com.example.ui.components.NoInternetOverlay
import com.example.ui.components.SolesFinBottomBar
import com.example.ui.components.SolesFinSideBar
import com.example.ui.components.StatusAlertBanner
import com.example.ui.components.TransferReceiptDialog
import com.example.ui.components.WithdrawFromGoalDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.util.ConnectivityObserver
import com.example.ui.util.NetworkConnectivityObserver
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.EmailVerificationScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PayServicesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SecurityPinScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransactionDetailScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.screens.WithdrawQrScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ActiveWindow
import com.example.ui.viewmodel.FintechViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.SessionState
import kotlinx.coroutines.flow.collectLatest

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.util.PushNotificationHelper

class MainActivity : FragmentActivity() {
    private val viewModel: FintechViewModel by viewModels()
    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushNotificationHelper.initializeChannels(applicationContext)
        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }
            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.setPushNotificationsEnabled(true)
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val networkStatus by connectivityObserver.observe().collectAsStateWithLifecycle(
                    initialValue = ConnectivityObserver.Status.Available
                )
                val isNetworkLost = networkStatus == ConnectivityObserver.Status.Lost ||
                        networkStatus == ConnectivityObserver.Status.Unavailable ||
                        networkStatus == ConnectivityObserver.Status.Losing

                val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
                val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
                val authError by viewModel.authError.collectAsStateWithLifecycle()
                val accountStatus by viewModel.accountSecurityStatus.collectAsStateWithLifecycle()
                val supportChannels by viewModel.supportChannels.collectAsStateWithLifecycle()
                val systemConfig by viewModel.systemConfig.collectAsStateWithLifecycle()
                val deviceBlock by viewModel.deviceBlock.collectAsStateWithLifecycle()
                val googleAuthState by viewModel.googleAuthState.collectAsStateWithLifecycle()

                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = sessionState,
                        label = "SessionStateTransition"
                    ) { state ->
                        when (state) {
                            SessionState.SPLASH -> {
                                SplashScreen(
                                    onSplashFinished = { viewModel.onSplashFinished() }
                                )
                            }

                            SessionState.AUTH -> {
                                val context = LocalContext.current
                                val coroutineScope = rememberCoroutineScope()
                                AuthScreen(
                                    savedDni = viewModel.savedDni,
                                    savedEmail = viewModel.savedEmail,
                                    isLoading = authLoading,
                                    errorMessage = authError,
                                    onLogin = { docOrEmail, pass, remember ->
                                        viewModel.login(docOrEmail, pass, remember)
                                    },
                                    onRegister = { email, pass ->
                                        viewModel.registerAccount(email, pass)
                                    },
                                    onGoogleSignIn = {
                                        coroutineScope.launch {
                                            GoogleAuthHelper.launchGoogleSignIn(
                                                context = context,
                                                onSuccess = { idToken, email, displayName ->
                                                    viewModel.loginWithGoogle(
                                                        idToken = idToken,
                                                        email = email,
                                                        displayName = displayName
                                                    )
                                                },
                                                onCancelled = { reason ->
                                                    viewModel.setGoogleAuthCancelled(reason)
                                                },
                                                onError = { errorMsg ->
                                                    viewModel.setGoogleAuthError(errorMsg)
                                                }
                                            )
                                        }
                                    },
                                    onForgotPassword = { email ->
                                        viewModel.sendPasswordReset(email)
                                    }
                                )
                            }

                            SessionState.EMAIL_VERIFICATION -> {
                                EmailVerificationScreen(
                                    userEmail = viewModel.getUserEmail(),
                                    userName = viewModel.getUserName(),
                                    isLoading = authLoading,
                                    onCheckVerification = {
                                        viewModel.checkEmailVerificationStatus()
                                    },
                                    onResendEmail = {
                                        viewModel.resendVerificationEmail()
                                    },
                                    onLogoutWithPin = { pin ->
                                        viewModel.logoutWithPin(pin)
                                    },
                                    onDeleteAccountWithPin = { pin, onResult ->
                                        viewModel.deleteAccountWithPin(pin, onResult)
                                    },
                                    onShowAlert = { message ->
                                        viewModel.showAlert(
                                            AppAlert(
                                                title = "BC-BANK",
                                                message = message,
                                                type = AlertType.INFO
                                            )
                                        )
                                    }
                                )
                            }

                            SessionState.ONBOARDING -> {
                                OnboardingScreen(
                                    userEmail = viewModel.getUserEmail(),
                                    isLoading = authLoading,
                                    errorMessage = authError,
                                    onCompleteOnboarding = { accountType, fullName, dni, phone, pin, isBusiness, businessName, businessRuc, birthDate, isKid, age ->
                                        viewModel.completeOnboarding(
                                            accountType = accountType,
                                            fullName = fullName,
                                            dni = dni,
                                            phone = phone,
                                            pin = pin,
                                            isBusiness = isBusiness,
                                            businessName = businessName,
                                            businessRuc = businessRuc,
                                            birthDate = birthDate,
                                            isKid = isKid,
                                            age = age
                                        )
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                    }
                                )
                            }

                            SessionState.LOCKED -> {
                                val activity = context as? androidx.fragment.app.FragmentActivity
                                val pinError by viewModel.pinError.collectAsStateWithLifecycle()
                                SecurityPinScreen(
                                    userName = viewModel.getUserName(),
                                    isBiometricAllowed = viewModel.isBiometricEnabled(),
                                    errorMessage = pinError,
                                    onPinEntered = { pin ->
                                        viewModel.unlockWithPin(pin)
                                    },
                                    onRequestBiometric = {
                                        if (activity != null && viewModel.isBiometricEnabled()) {
                                            com.example.ui.util.BiometricAuthManager.authenticate(
                                                activity = activity,
                                                onSuccess = {
                                                    viewModel.unlockWithBiometrics()
                                                },
                                                onError = { _ -> }
                                            )
                                        }
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                    }
                                )
                            }

                            SessionState.AUTHENTICATED -> {
                                MainAppScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // 1. Google Authentication State Modal
                    GoogleAuthProgressDialog(
                        state = googleAuthState,
                        onDismiss = { viewModel.dismissGoogleAuthError() },
                        onVerifyPin = { pin -> viewModel.completeGoogleLoginWithPin(pin) }
                    )

                    // 2. Account Security Status Blocking Overlay (SUSPENDED / BLOCKED)
                    AccountStatusOverlay(
                        statusInfo = accountStatus,
                        supportChannels = supportChannels,
                        onLogout = { viewModel.logout() }
                    )

                    // 3. Hardware Device Ban / Maintenance / Force Update Gate Overlay
                    AppStatusGateOverlay(
                        deviceBlock = deviceBlock,
                        systemConfig = systemConfig,
                        currentVersionCode = 1
                    )

                    // 4. Real-time Full-Screen No-Internet Overlay
                    NoInternetOverlay(
                        isLost = isNetworkLost,
                        onRetry = {
                            // Trigger re-check
                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isChangingConfigurations && viewModel.sessionState.value == SessionState.AUTHENTICATED) {
            viewModel.lockSession()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && viewModel.sessionState.value == SessionState.AUTHENTICATED) {
            viewModel.lockSession()
        }
    }
}

@Composable
fun MainAppScreen(viewModel: FintechViewModel) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeWindow by viewModel.activeWindow.collectAsStateWithLifecycle()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsStateWithLifecycle()
    val account by viewModel.account.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.rawTransactions.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val filter by viewModel.transactionFilter.collectAsStateWithLifecycle()
    val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()

    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userDni by viewModel.userDni.collectAsStateWithLifecycle()
    val userAccountType by viewModel.userAccountType.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val supportChannels by viewModel.supportChannels.collectAsStateWithLifecycle()

    // Dialog states
    val showAddGoalDialog by viewModel.showAddGoalDialog.collectAsStateWithLifecycle()
    val goalForDeposit by viewModel.goalForDeposit.collectAsStateWithLifecycle()
    val goalForEdit by viewModel.goalForEdit.collectAsStateWithLifecycle()
    val goalForDelete by viewModel.goalForDelete.collectAsStateWithLifecycle()
    val goalForWithdraw by viewModel.goalForWithdraw.collectAsStateWithLifecycle()

    val showAddBudgetDialog by viewModel.showAddBudgetDialog.collectAsStateWithLifecycle()
    val budgetForEdit by viewModel.budgetForEdit.collectAsStateWithLifecycle()
    val budgetForDelete by viewModel.budgetForDelete.collectAsStateWithLifecycle()

    val transferReceipt by viewModel.transferReceipt.collectAsStateWithLifecycle()
    val activeWithdrawalReservation by viewModel.activeWithdrawalReservation.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    BackHandler {
        when {
            transferReceipt != null -> viewModel.dismissReceipt()
            budgetForDelete != null -> viewModel.closeDeleteBudgetDialog()
            budgetForEdit != null -> viewModel.closeBudgetEdit()
            showAddBudgetDialog -> viewModel.closeAddBudgetDialog()
            goalForWithdraw != null -> viewModel.closeWithdrawGoalDialog()
            goalForDelete != null -> viewModel.closeDeleteGoalDialog()
            goalForEdit != null -> viewModel.closeEditGoalDialog()
            goalForDeposit != null -> viewModel.closeGoalDeposit()
            showAddGoalDialog -> viewModel.closeAddGoalDialog()
            activeAlert != null -> viewModel.dismissAlert()

            activeWindow !is ActiveWindow.None -> {
                viewModel.closeActiveWindow()
            }

            currentTab != NavigationTab.DASHBOARD -> {
                viewModel.setTab(NavigationTab.DASHBOARD)
            }

            else -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000L) {
                    (context as? android.app.Activity)?.finish()
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(
                        context,
                        "Presiona atrás una vez más para salir de la aplicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isLargeScreen && activeWindow is ActiveWindow.None) {
                SolesFinSideBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) },
                    onOpenProfile = { viewModel.openProfileWindow() },
                    onOpenNotifications = { viewModel.openNotificationsWindow() },
                    unreadNotificationCount = unreadNotificationsCount
                )
            }

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!isLargeScreen && activeWindow is ActiveWindow.None) {
                        SolesFinBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    }
                },
                containerColor = BackgroundDark,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(innerPadding)
                ) {
            Crossfade(
                targetState = activeWindow,
                label = "ActiveWindowTransition"
            ) { window ->
                when (window) {
                    is ActiveWindow.None -> {
                        Crossfade(
                            targetState = currentTab,
                            label = "ScreenTransition"
                        ) { tab ->
                            when (tab) {
                                NavigationTab.DASHBOARD -> {
                                    DashboardScreen(
                                        account = account,
                                        transactions = transactions,
                                        savingsGoals = savingsGoals,
                                        budgets = budgets,
                                        isBalanceHidden = isBalanceHidden,
                                        selectedFilter = filter,
                                        onTogglePrivacy = { viewModel.toggleBalancePrivacy() },
                                        onFilterChanged = { viewModel.setTransactionFilter(it) },
                                        onNavigate = { viewModel.setTab(it) },
                                        onOpenTransfer = { viewModel.openTransferWindow() },
                                        onOpenDeposit = { viewModel.openDepositWindow() },
                                        onOpenPayServices = { viewModel.openPayServicesWindow() },
                                        onOpenWithdrawQr = { viewModel.openWithdrawQrWindow() },
                                        onOpenProfile = { viewModel.openProfileWindow() },
                                        onOpenNotifications = { viewModel.openNotificationsWindow() },
                                        onOpenTransactionDetail = { viewModel.openTransactionDetail(it) },
                                        onOpenGoalDeposit = { viewModel.openGoalDeposit(it) },
                                        unreadNotificationsCount = unreadNotificationsCount
                                    )

                                }

                                NavigationTab.METAS -> {
                                    SavingsGoalsScreen(
                                        goals = savingsGoals,
                                        isBalanceHidden = isBalanceHidden,
                                        onOpenAddGoal = { viewModel.openAddGoalDialog() },
                                        onOpenDepositGoal = { viewModel.openGoalDeposit(it) },
                                        onOpenEditGoal = { viewModel.openEditGoalDialog(it) },
                                        onOpenDeleteGoal = { viewModel.openDeleteGoalDialog(it) },
                                        onOpenWithdrawGoal = { viewModel.openWithdrawGoalDialog(it) }
                                    )
                                }

                                NavigationTab.PRESUPUESTOS -> {
                                    BudgetsScreen(
                                        budgets = budgets,
                                        isBalanceHidden = isBalanceHidden,
                                        onOpenAddBudget = { viewModel.openAddBudgetDialog() },
                                        onEditBudget = { viewModel.openBudgetEdit(it) },
                                        onDeleteBudget = { viewModel.openDeleteBudgetDialog(it) }
                                    )
                                }

                                NavigationTab.ANALITICA -> {
                                    AnalyticsScreen(
                                        transactions = allTransactions,
                                        budgets = budgets,
                                        savingsGoals = savingsGoals,
                                        isBalanceHidden = isBalanceHidden
                                    )
                                }
                            }
                        }
                    }

                    is ActiveWindow.Transfer -> {
                        TransferScreen(
                            account = account,
                            isBalanceHidden = isBalanceHidden,
                            initialRecipient = window.initialRecipient,
                            initialIdentifier = window.initialIdentifier,
                            initialAmount = window.initialAmount,
                            onBack = { viewModel.closeActiveWindow() },
                            onPerformTransfer = { recipient, phone, amount, concept, category ->
                                viewModel.transferMoney(recipient, phone, amount, concept, category)
                            }
                        )
                    }

                    is ActiveWindow.Deposit -> {
                        DepositScreen(
                            account = account,
                            isBalanceHidden = isBalanceHidden,
                            onBack = { viewModel.closeActiveWindow() }
                        )
                    }

                    is ActiveWindow.PayServices -> {
                        PayServicesScreen(
                            account = account,
                            isBalanceHidden = isBalanceHidden,
                            onBack = { viewModel.closeActiveWindow() },
                            onPayService = { serviceId, serviceName, category, supplyCode, amount, commission, comment ->
                                viewModel.payService(
                                    serviceId = serviceId,
                                    serviceName = serviceName,
                                    supplyCode = supplyCode,
                                    amount = amount,
                                    commission = commission,
                                    category = category,
                                    comment = comment
                                )
                            }
                        )
                    }

                    is ActiveWindow.WithdrawQr -> {
                        WithdrawQrScreen(
                            account = account,
                            userPhone = userPhone,
                            userDni = userDni,
                            isBalanceHidden = isBalanceHidden,
                            activeWithdrawal = activeWithdrawalReservation,
                            onBack = { viewModel.closeActiveWindow() },
                            onCreateWithdrawalReservation = { amount, pin, opCode ->
                                viewModel.createWithdrawalReservation(amount, pin, opCode)
                            },
                            onCancelWithdrawalReservation = { pin1, pin2, onResult ->
                                viewModel.cancelWithdrawalReservation(pin1, pin2, onResult)
                            },
                            onWithdrawFunds = { amount, method, pinCode ->
                                viewModel.withdrawFunds(amount, method, pinCode)
                            },
                            onScanTransfer = { recipient, identifier, amount ->
                                viewModel.openTransferWithData(recipient, identifier, amount)
                            }
                        )
                    }

                    is ActiveWindow.Profile -> {
                        ProfileScreen(
                            account = account,
                            userPhone = userPhone,
                            userEmail = userEmail,
                            userDni = userDni,
                            userAccountType = userAccountType,
                            supportChannels = supportChannels,
                            isBiometricEnabled = viewModel.isBiometricEnabled(),
                            isPushNotificationsEnabled = viewModel.isPushNotificationsEnabled(),
                            currentThemeMode = themeMode,
                            onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
                            onToggleBiometric = { enabled -> viewModel.setBiometricEnabled(enabled) },
                            onTogglePushNotifications = { enabled -> viewModel.setPushNotificationsEnabled(enabled) },
                            onUpdatePin = { newPin -> viewModel.updateUserPin(newPin) },
                            onUpdatePhone = { newPhone, pin1, pin2, onResult ->
                                viewModel.updateUserPhone(newPhone, pin1, pin2, onResult)
                            },
                            onOpenNotifications = { viewModel.openNotificationsWindow() },
                            onBack = { viewModel.closeActiveWindow() },
                            onShowCopiedAlert = { message ->
                                viewModel.showAlert(
                                    AppAlert(
                                        title = "Información",
                                        message = message,
                                        type = AlertType.INFO
                                    )
                                )
                            },
                            onLogout = { viewModel.logout() }
                        )
                    }

                    is ActiveWindow.Notifications -> {
                        NotificationsScreen(
                            notifications = notifications,
                            unreadCount = unreadNotificationsCount,
                            onBack = { viewModel.closeActiveWindow() },
                            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
                            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                            onDeleteNotification = { id -> viewModel.deleteNotification(id) },
                            onClearAll = { viewModel.clearAllNotifications() },
                            onShowAlert = { message ->
                                viewModel.showAlert(
                                    AppAlert(
                                        title = "Notificaciones",
                                        message = message,
                                        type = AlertType.INFO
                                    )
                                )
                            }
                        )
                    }


                    is ActiveWindow.TransactionDetail -> {
                        val liveTx = allTransactions.find {
                            it.referenceNumber == window.transaction.referenceNumber ||
                            (window.transaction.id != 0L && it.id == window.transaction.id)
                        } ?: window.transaction
                        TransactionDetailScreen(
                            transaction = liveTx,
                            activeWithdrawal = activeWithdrawalReservation,
                            onBack = { viewModel.closeActiveWindow() },
                            onCancelPendingWithdrawal = { opCode, onResult ->
                                viewModel.cancelWithdrawalByOpCode(opCode, onResult)
                            }
                        )
                    }
                }
            }

            // Top Status Alert Banner (high elevation)
            StatusAlertBanner(
                alert = activeAlert,
                onDismiss = { viewModel.dismissAlert() },
                modifier = Modifier.padding(top = 8.dp)
            )

            // Dialogs
            if (showAddGoalDialog) {
                AddSavingsGoalDialog(
                    onDismiss = { viewModel.closeAddGoalDialog() },
                    onConfirm = { name, target, initial, icon, date ->
                        viewModel.createSavingsGoal(name, target, initial, icon, date)
                    }
                )
            }

            goalForDeposit?.let { goal ->
                DepositToGoalDialog(
                    goal = goal,
                    onDismiss = { viewModel.closeGoalDeposit() },
                    onConfirm = { amount ->
                        viewModel.depositToSavingsGoal(goal.id, amount)
                    }
                )
            }

            goalForEdit?.let { goal ->
                EditSavingsGoalDialog(
                    goal = goal,
                    onDismiss = { viewModel.closeEditGoalDialog() },
                    onConfirm = { name, targetAmount, targetDate, categoryIcon ->
                        viewModel.updateSavingsGoal(goal.id, name, targetAmount, categoryIcon, targetDate)
                    }
                )
            }

            goalForDelete?.let { goal ->
                DeleteSavingsGoalDialog(
                    goal = goal,
                    onDismiss = { viewModel.closeDeleteGoalDialog() },
                    onConfirmDelete = { returnFunds ->
                        viewModel.deleteSavingsGoal(goal.id, returnFunds)
                    }
                )
            }

            goalForWithdraw?.let { goal ->
                WithdrawFromGoalDialog(
                    goal = goal,
                    onDismiss = { viewModel.closeWithdrawGoalDialog() },
                    onConfirm = { amount ->
                        viewModel.withdrawFromSavingsGoal(goal.id, amount)
                    }
                )
            }

            if (showAddBudgetDialog) {
                AddBudgetDialog(
                    onDismiss = { viewModel.closeAddBudgetDialog() },
                    onConfirm = { category, limit ->
                        viewModel.createBudget(category, limit)
                    }
                )
            }

            budgetForEdit?.let { budget ->
                EditBudgetDialog(
                    budget = budget,
                    onDismiss = { viewModel.closeBudgetEdit() },
                    onConfirm = { category, newLimit ->
                        viewModel.updateBudget(budget.id, category, newLimit)
                    },
                    onResetSpent = {
                        viewModel.resetBudgetSpent(budget.id)
                    },
                    onDeleteClick = {
                        viewModel.openDeleteBudgetDialog(budget)
                    }
                )
            }

            budgetForDelete?.let { budget ->
                DeleteBudgetDialog(
                    budget = budget,
                    onDismiss = { viewModel.closeDeleteBudgetDialog() },
                    onConfirmDelete = {
                        viewModel.deleteBudget(budget.id)
                    }
                )
            }

            transferReceipt?.let { receipt ->
                TransferReceiptDialog(
                    receipt = receipt,
                    onDismiss = { viewModel.dismissReceipt() }
                )
            }
        }
    }
}
}
}


