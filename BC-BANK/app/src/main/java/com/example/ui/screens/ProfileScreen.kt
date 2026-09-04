package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.SupportChannel
import com.example.data.local.AccountInfoEntity
import com.example.ui.components.Formatters
import com.example.ui.components.SupportChannelCard
import com.example.ui.components.VoucherVerifierModal
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletDark
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import com.example.ui.util.BankNotificationManager
import com.example.ui.util.CategoryItem
import com.example.ui.util.CategoryType
import com.example.ui.util.CustomCategoryManager

@Composable
fun ProfileScreen(
    account: AccountInfoEntity?,
    userPhone: String = "",
    userEmail: String = "",
    userDni: String = "",
    userAccountType: String = "",
    supportChannels: List<SupportChannel> = emptyList(),
    isBiometricEnabled: Boolean = true,
    isPushNotificationsEnabled: Boolean = false,
    onToggleBiometric: (Boolean) -> Unit = {},
    onTogglePushNotifications: (Boolean) -> Unit = {},
    onUpdatePin: (String) -> Unit = {},
    onUpdatePhone: (newPhone: String, pin1: String, pin2: String, onResult: (Boolean, String?) -> Unit) -> Unit = { _, _, _, cb -> cb(true, null) },
    onBack: () -> Unit,
    onShowCopiedAlert: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var biometricEnabled by remember(isBiometricEnabled) { mutableStateOf(isBiometricEnabled) }
    var pushNotificationsEnabled by remember(isPushNotificationsEnabled) { mutableStateOf(isPushNotificationsEnabled) }
    var dailyLimit by remember { mutableDoubleStateOf(5000.0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pushNotificationsEnabled = true
            onTogglePushNotifications(true)
            onShowCopiedAlert("Alertas push y de seguridad activadas en tiempo real")
        } else {
            pushNotificationsEnabled = false
            onTogglePushNotifications(false)
            onShowCopiedAlert("Permiso de notificaciones denegado. Puedes activarlo en Ajustes.")
        }
    }
    
    // 6-digit PIN Dialog State
    var showChangePinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Phone Change Dialog State
    var showChangePhoneDialog by remember { mutableStateOf(false) }
    var newPhoneInput by remember { mutableStateOf("") }
    var phonePin1Input by remember { mutableStateOf("") }
    var phonePin2Input by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showCategoryManagerDialog by remember { mutableStateOf(false) }
    var showVoucherVerifierModal by remember { mutableStateOf(false) }

    val holderName = account?.accountHolder?.ifBlank { "Usuario BC-BANK" } ?: "Usuario BC-BANK"
    val displayAccountType = if (userAccountType.isNotBlank()) userAccountType else (account?.bankName?.ifBlank { "Cuenta de Ahorros BC-BANK" } ?: "Cuenta de Ahorros BC-BANK")
    val displayPhone = userPhone.ifBlank { "No registrado" }
    val displayEmail = userEmail.ifBlank { "No registrado" }
    val displayDni = userDni.ifBlank { "No registrado" }
    val accountNumber = account?.accountNumber.orEmpty()
    val cciNumber = account?.cciNumber.orEmpty()

    val userInitials = remember(holderName) {
        holderName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "BC" }
    }

    val activeSupportChannels = remember(supportChannels) {
        val list = if (supportChannels.isNotEmpty()) {
            supportChannels.filter { it.isAvailable }
        } else {
            FirebaseManager.getDefaultLocalSupportChannels()
        }
        list.sortedWith(compareByDescending<SupportChannel> { it.isPrimary }.thenBy { it.priority })
    }

    fun copyToClipboard(label: String, text: String) {
        if (text.isNotBlank() && text != "No registrado") {
            clipboard.setText(AnnotatedString(text))
            onShowCopiedAlert("$label copiado al portapapeles")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x306D5BFF), Color(0x1038BDF8), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(250f, 150f),
                    radius = 850f
                )
            )
            .background(BackgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
        // 1. Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF101117))
                        .border(1.dp, BorderGlass, CircleShape)
                        .testTag("profile_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Mi Perfil & Gestión",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = "Configuración bancaria y seguridad BC-BANK",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. User Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryViolet, PrimaryVioletDark)
                                )
                            )
                            .border(1.5.dp, BorderGlass, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = holderName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verificado",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Cliente BC-BANK • Titular Verificado",
                            fontSize = 12.sp,
                            color = EmeraldLight
                        )
                        Text(
                            text = "Banca Digital • Perú",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 3. Cardless Banking Digital Badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF063A28),
                                Color(0xFF0D1E16)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        EmeraldPrimary.copy(alpha = 0.5f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldDark)
                            .border(1.dp, EmeraldLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Banca 100% Digital Sin Tarjeta",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Operaciones directas con N° de Cuenta, CCI y Clave Digital de seguridad.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 4. Permanent Account Type Card (Non-editable)
        item {
            Text(
                text = "Tipo de Cuenta Bancaria Registrada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldDark)
                                    .border(1.dp, EmeraldLight.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = displayAccountType,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Asignada en apertura • No modificable",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldDark)
                                .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Activa",
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val benefitHint = when {
                        displayAccountType.contains("Empresarial", ignoreCase = true) ->
                            "Cuenta Empresarial: RUC 10/20, pagos masivos y transferencias."
                        displayAccountType.contains("Corriente", ignoreCase = true) ->
                            "Cuenta Corriente: Operaciones comerciales y sobregiro."
                        else ->
                            "Cuenta de Ahorros: 0% comisión de mantenimiento y disponibilidad 24/7."
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D211A))
                            .border(1.dp, EmeraldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = benefitHint,
                            fontSize = 11.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 5. Bank Account Identifiers Card
        item {
            Text(
                text = "Cuentas Bancarias & Datos Registrados",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nro Cuenta
                    if (accountNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("N° de Cuenta Bancaria", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = accountNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                                if (displayAccountType.isNotBlank()) {
                                    Text(displayAccountType, fontSize = 10.sp, color = EmeraldLight, fontWeight = FontWeight.Medium)
                                }
                            }
                            IconButton(
                                onClick = { copyToClipboard("N° Cuenta BC-BANK", accountNumber) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceCard)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            }
                        }

                        HorizontalDivider(color = BorderDark, thickness = 1.dp)
                    }

                    // CCI
                    if (cciNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Código Interbancario (CCI)", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = cciNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = { copyToClipboard("CCI Interbancario", cciNumber) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceCard)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            }
                        }

                        HorizontalDivider(color = BorderDark, thickness = 1.dp)
                    }

                    // Celular Afiliado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Celular Afiliado a BC-BANK", fontSize = 11.sp, color = TextSecondary)
                            Text(displayPhone, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    newPhoneInput = ""
                                    phonePin1Input = ""
                                    phonePin2Input = ""
                                    phoneError = null
                                    showChangePhoneDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_change_phone")
                            ) {
                                Text("Cambiar", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (displayPhone != "No registrado") {
                                IconButton(
                                    onClick = { copyToClipboard("Celular", displayPhone) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceCard)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    // DNI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Documento de Identidad (DNI)", fontSize = 11.sp, color = TextSecondary)
                            Text(displayDni, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (displayDni != "No registrado") {
                            IconButton(
                                onClick = { copyToClipboard("DNI", displayDni) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceCard)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    // Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Correo Electrónico Registrado", fontSize = 11.sp, color = TextSecondary)
                            Text(displayEmail, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        if (displayEmail != "No registrado") {
                            IconButton(
                                onClick = { copyToClipboard("Correo", displayEmail) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceCard)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 5. Category Management Card
        item {
            Text(
                text = "Categorías de Ingresos & Gastos",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldDark)
                                .border(1.dp, EmeraldLight.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Gestión de Categorías",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Crea y administra tus categorías de Gastos, Ingresos, Metas y Presupuestos",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { showCategoryManagerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("btn_manage_categories")
                        ) {
                            Text("Gestionar", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5.1 Voucher Verification Card
        item {
            Text(
                text = "Auditoría de Comprobantes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldDark)
                                    .border(1.dp, EmeraldLight.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Verificador de Comprobantes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Audita y valida la autenticidad e inmutabilidad de cualquier comprobante o voucher bancario",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showVoucherVerifierModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("btn_open_voucher_verifier")
                        ) {
                            Text("Verificar", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Security & Preferences
        item {
            Text(
                text = "Seguridad y Límites Diarios",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Bloqueo del Dispositivo / Huella", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Permite desbloquear la app con la huella, Face ID o bloqueo de tu teléfono", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                onToggleBiometric(it)
                                onShowCopiedAlert(if (it) "Acceso por huella/bloqueo activado" else "Acceso por huella/bloqueo desactivado (solo PIN)")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    // 6-digit PIN Tile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("PIN de Seguridad (6 dígitos)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Requerido para reanudar sesión y autorizar transacciones", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Button(
                            onClick = {
                                newPinInput = ""
                                confirmPinInput = ""
                                pinError = null
                                showChangePinDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("btn_change_pin")
                        ) {
                            Text("Cambiar", color = EmeraldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Alertas Push en Tiempo Real", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(
                                    text = if (pushNotificationsEnabled) "Activo: Notificaciones bancarias y de transferencias" else "Desactivado por defecto. Activa para recibir alertas",
                                    fontSize = 11.sp,
                                    color = if (pushNotificationsEnabled) EmeraldLight else TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPerm) {
                                            pushNotificationsEnabled = true
                                            onTogglePushNotifications(true)
                                            onShowCopiedAlert("Alertas push activadas en tiempo real")
                                        } else {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        pushNotificationsEnabled = true
                                        onTogglePushNotifications(true)
                                        onShowCopiedAlert("Alertas push activadas en tiempo real")
                                    }
                                } else {
                                    pushNotificationsEnabled = false
                                    onTogglePushNotifications(false)
                                    BankNotificationManager.cancelAllNotifications(context)
                                    onShowCopiedAlert("Alertas push desactivadas")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Límite Diario de Transferencias:", fontSize = 12.sp, color = TextSecondary)
                            Text(Formatters.formatSoles(dailyLimit), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1000.0, 3000.0, 5000.0, 10000.0).forEach { limitOption ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (dailyLimit == limitOption) EmeraldDark else SurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (dailyLimit == limitOption) EmeraldPrimary else BorderDark,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            dailyLimit = limitOption
                                            onShowCopiedAlert("Límite diario actualizado a ${Formatters.formatSoles(limitOption)}")
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "S/ ${limitOption.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dailyLimit == limitOption) EmeraldLight else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Canales de Atención y Soporte Oficial
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HeadsetMic,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Canales de Atención y Soporte Oficial",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeSupportChannels.forEach { channel ->
                    SupportChannelCard(
                        channel = channel,
                        onOpen = {
                            try {
                                val customUrl = channel.actionUrl.trim()
                                if (customUrl.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } else {
                                    when (channel.type.uppercase()) {
                                        "WHATSAPP" -> {
                                            val cleanDigits = channel.value.filter { it.isDigit() }
                                            val targetPhone = if (cleanDigits.startsWith("51")) cleanDigits else "51$cleanDigits"
                                            val uri = Uri.parse("https://wa.me/$targetPhone?text=Hola%20BC-BANK,%20solicito%20asistencia")
                                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                        "TELEGRAM" -> {
                                            val handle = channel.value.replace("@", "").trim()
                                            val uri = if (channel.value.startsWith("http")) Uri.parse(channel.value) else Uri.parse("https://t.me/$handle")
                                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                        "EMAIL" -> {
                                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:${channel.value.trim()}")
                                                putExtra(Intent.EXTRA_SUBJECT, "Consulta BC-BANK Soporte")
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                        "PHONE" -> {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${channel.value.trim()}")).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                        else -> {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(channel.value)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        }

        // 8. Action Buttons & Logout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceCard,
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("close_profile_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Listo / Volver a Inicio", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showLogoutConfirm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7F1D1D).copy(alpha = 0.25f),
                        contentColor = Color(0xFFF87171)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_profile_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión Segura", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- DIALOGS HOISTED TO ROOT BOX FOR INSTANT RESPONSIVENESS ---

    // Dialog: Change Phone Number
    if (showChangePhoneDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showChangePhoneDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Cambiar Número Celular",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ingresa tu nuevo número (9 dígitos) e ingresa 2 veces tu PIN de 6 dígitos actual para autorizar la operación.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = newPhoneInput,
                        onValueChange = {
                            if (it.length <= 9 && it.all { c -> c.isDigit() }) {
                                newPhoneInput = it
                                phoneError = null
                            }
                        },
                        label = { Text("Nuevo Celular (9 dígitos)") },
                        placeholder = { Text("987654321") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_phone")
                    )

                    OutlinedTextField(
                        value = phonePin1Input,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                phonePin1Input = it
                                phoneError = null
                            }
                        },
                        label = { Text("PIN de Seguridad (1ra vez)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_phone_pin1")
                    )

                    OutlinedTextField(
                        value = phonePin2Input,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                phonePin2Input = it
                                phoneError = null
                            }
                        },
                        label = { Text("Confirmar PIN de Seguridad (2da vez)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_phone_pin2")
                    )

                    if (phoneError != null) {
                        Text(
                            text = phoneError ?: "",
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPhoneInput.length != 9 || !newPhoneInput.startsWith("9")) {
                            phoneError = "El celular debe tener 9 dígitos e iniciar con 9."
                            return@Button
                        }
                        if (phonePin1Input.length != 6) {
                            phoneError = "El PIN debe tener 6 dígitos."
                            return@Button
                        }
                        if (phonePin1Input != phonePin2Input) {
                            phoneError = "Los PINs ingresados no coinciden."
                            return@Button
                        }
                        onUpdatePhone(newPhoneInput, phonePin1Input, phonePin2Input) { success, err ->
                            if (success) {
                                showChangePhoneDialog = false
                            } else {
                                phoneError = err ?: "Error al actualizar celular"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_save_new_phone")
                ) {
                    Text("Guardar Celular", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showChangePhoneDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    // Dialog: Change 6-digit PIN
    if (showChangePinDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Actualizar PIN de Seguridad",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ingresa tu nuevo PIN numérico de 6 dígitos para reanudar sesión y validar transacciones.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                newPinInput = it
                                pinError = null
                            }
                        },
                        label = { Text("Nuevo PIN (6 dígitos)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                confirmPinInput = it
                                pinError = null
                            }
                        },
                        label = { Text("Confirmar Nuevo PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin")
                    )

                    if (pinError != null) {
                        Text(
                            text = pinError ?: "",
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length != 6) {
                            pinError = "El PIN debe tener exactamente 6 dígitos numéricos."
                            return@Button
                        }
                        if (newPinInput != confirmPinInput) {
                            pinError = "Los PINs ingresados no coinciden."
                            return@Button
                        }
                        onUpdatePin(newPinInput)
                        showChangePinDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_save_new_pin")
                ) {
                    Text("Guardar PIN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    if (showCategoryManagerDialog) {
        CategoryManagerDialog(
            onDismiss = { showCategoryManagerDialog = false },
            onShowAlert = onShowCopiedAlert
        )
    }

    if (showLogoutConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = SurfaceCard,
            title = {
                Text(
                    text = "¿Cerrar Sesión?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas cerrar tu sesión en BC-BANK? Deberás autenticarte nuevamente para acceder.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    // Voucher Verifier Modal
    if (showVoucherVerifierModal) {
        VoucherVerifierModal(
            onDismiss = { showVoucherVerifierModal = false }
        )
    }
}
}

@Composable
fun CategoryManagerDialog(
    onDismiss: () -> Unit,
    onShowAlert: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Gastos, 1 = Ingresos, 2 = Metas, 3 = Presupuestos
    var newCategoryName by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryItem?>(null) }

    val expenseCategories by CustomCategoryManager.expenseCategories.collectAsStateWithLifecycle()
    val incomeCategories by CustomCategoryManager.incomeCategories.collectAsStateWithLifecycle()
    val goalCategories by CustomCategoryManager.goalCategories.collectAsStateWithLifecycle()
    val budgetCategories by CustomCategoryManager.budgetCategories.collectAsStateWithLifecycle()

    val tabTitles = listOf("Gastos", "Ingresos", "Metas", "Presupuestos")
    val tabTypes = listOf(CategoryType.EXPENSE, CategoryType.INCOME, CategoryType.GOAL, CategoryType.BUDGET)
    val tabCounts = listOf(expenseCategories.size, incomeCategories.size, goalCategories.size, budgetCategories.size)

    val currentList = when (selectedTab) {
        0 -> expenseCategories
        1 -> incomeCategories
        2 -> goalCategories
        else -> budgetCategories
    }
    val currentType = tabTypes[selectedTab]
    val currentTabLabel = tabTitles[selectedTab]

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 500.dp)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldDark)
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gestión de Categorías",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Sincronizado en tiempo real",
                                fontSize = 11.sp,
                                color = EmeraldLight
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                // Subtitle
                Text(
                    text = "Crea y personaliza categorías para clasificar con precisión tus movimientos, presupuestos y metas de ahorro.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                // Segmented Tabs - 2x2 Grid for generous width and zero text wrapping
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCard)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Gastos & Ingresos
                        listOf(0 to "Gastos", 1 to "Ingresos").forEach { (index, title) ->
                            val isSelected = selectedTab == index
                            val count = tabCounts[index]
                            Surface(
                                onClick = {
                                    selectedTab = index
                                    inputError = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) EmeraldPrimary else SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldPrimary else BorderDark
                                ),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (index == 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else (if (index == 0) ExpenseRed else EmeraldPrimary),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.Black.copy(alpha = 0.15f) else BorderDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Metas & Presupuestos
                        listOf(2 to "Metas", 3 to "Presupuestos").forEach { (index, title) ->
                            val isSelected = selectedTab == index
                            val count = tabCounts[index]
                            Surface(
                                onClick = {
                                    selectedTab = index
                                    inputError = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) EmeraldPrimary else SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldPrimary else BorderDark
                                ),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (index == 2) Icons.Default.Savings else Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else EmeraldLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.Black.copy(alpha = 0.15f) else BorderDark)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Category Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Nueva Categoría",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldDark)
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Para: $currentTabLabel",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                            }
                            Text(
                                text = "Ingresa el nombre para agregarlo inmediatamente a tus $currentTabLabel.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { 
                                if (it.length <= 25) {
                                    newCategoryName = it 
                                    inputError = null
                                }
                            },
                            label = { Text("Nombre de la categoría") },
                            placeholder = { 
                                Text(
                                    when (selectedTab) {
                                        0 -> "Ej. Alquiler, Restaurantes, Tarjetas"
                                        1 -> "Ej. Sueldo, Freelance, Dividendos"
                                        2 -> "Ej. Boda, Maestría, Inversión"
                                        else -> "Ej. Mascotas, Suscripciones, Gimnasio"
                                    },
                                    fontSize = 12.sp
                                ) 
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_new_category_name")
                        )

                        if (inputError != null) {
                            Text(
                                text = inputError ?: "",
                                color = ExpenseRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                val trimmed = newCategoryName.trim()
                                if (trimmed.isBlank()) {
                                    inputError = "Ingresa un nombre para la categoría"
                                    return@Button
                                }
                                val exists = currentList.any { it.name.equals(trimmed, ignoreCase = true) }
                                if (exists) {
                                    inputError = "Esta categoría ya existe en $currentTabLabel"
                                    return@Button
                                }
                                CustomCategoryManager.addCategory(
                                    name = trimmed,
                                    type = currentType
                                )
                                onShowAlert("Categoría '$trimmed' creada y sincronizada")
                                newCategoryName = ""
                                inputError = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_save_category")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar Categoría", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Existing Categories List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categorías activas (${currentList.size}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sección: $currentTabLabel",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (currentList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay categorías de $currentTabLabel aún",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Escribe un nombre arriba y pulsa Guardar para registrarla.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentList.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldDark)
                                            .border(1.dp, EmeraldLight.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (selectedTab) {
                                                0 -> Icons.Default.TrendingDown
                                                1 -> Icons.Default.TrendingUp
                                                2 -> Icons.Default.Savings
                                                else -> Icons.Default.PieChart
                                            },
                                            contentDescription = null,
                                            tint = EmeraldLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (cat.isCustom) "Personalizada • Sincronizada" else "Predeterminada",
                                            fontSize = 10.sp,
                                            color = if (cat.isCustom) EmeraldLight else TextMuted
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        categoryToDelete = cat
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Category Confirmation Dialog
    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "¿Eliminar Categoría?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar '${cat.name}'? Se quitará de tu lista personalizada.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        CustomCategoryManager.deleteCategory(cat.id, currentType)
                        onShowAlert("Categoría '${cat.name}' eliminada")
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}
