package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import androidx.compose.material.icons.filled.Shield
import com.example.ui.util.QrCodeParser
import com.example.ui.util.QrParsedResult
import com.example.ui.util.CustomerQrScanResult
import com.example.ui.components.CleanQrCameraScanner
import com.example.ui.components.VoucherVerifierModal
import com.example.ui.util.ImageExportUtils
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.data.firebase.CloudWithdrawal
import com.example.data.local.AccountInfoEntity
import com.example.ui.components.Formatters
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
import com.example.ui.theme.ExpenseRedLight
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
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.random.Random

@Composable
fun WithdrawQrScreen(
    account: AccountInfoEntity?,
    userPhone: String = "",
    userDni: String = "",
    isBalanceHidden: Boolean,
    activeWithdrawal: CloudWithdrawal? = null,
    onBack: () -> Unit,
    onCreateWithdrawalReservation: (amount: Double, pin: String, opCode: String) -> Unit = { _, _, _ -> },
    onCancelWithdrawalReservation: (pin1: String, pin2: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    onWithdrawFunds: (amount: Double, method: String, pinCode: String) -> Unit,
    onScanTransfer: (recipient: String, accountOrPhone: String, amount: Double?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Retiro en Cajero, 1: Mi Código QR, 2: Escanear QR
    val context = LocalContext.current

    // Retiro States
    var withdrawAmountText by remember { mutableStateOf("") }
    var showWithdrawExportSuccess by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelPin1 by remember { mutableStateOf("") }
    var cancelPin2 by remember { mutableStateOf("") }
    var cancelError by remember { mutableStateOf<String?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    // Live retention timer (1 hour default)
    var remainingTimeMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(activeWithdrawal) {
        if (activeWithdrawal != null && activeWithdrawal.status == "PENDING") {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = (activeWithdrawal.expiresAt - now).coerceAtLeast(0L)
                remainingTimeMillis = diff
                if (diff <= 0L) break
                delay(1000L)
            }
        } else {
            remainingTimeMillis = 0L
        }
    }

    val remainingMinutes = (remainingTimeMillis / 1000L / 60L).toInt()
    val remainingSeconds = ((remainingTimeMillis / 1000L) % 60L).toInt()
    val timerString = String.format("%02d:%02d", remainingMinutes, remainingSeconds)

    // Mi QR States
    var showQrExportSuccess by remember { mutableStateOf(false) }

    // Scanner & Verifier States
    var isFlashOn by remember { mutableStateOf(false) }
    var rejectedQrDialogData by remember { mutableStateOf<Pair<String, String>?>(null) }
    var scannedVoucherSerialForVerifier by remember { mutableStateOf<String?>(null) }
    var showVerifierModal by remember { mutableStateOf(false) }
    var verifierInitialSerial by remember { mutableStateOf("") }
    var manualVoucherInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

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
                        .testTag("withdraw_qr_back_button")
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
                        text = "Retiros & Código QR",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cajeros BC-BANK, tu QR para recibir y escáner de pagos",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. Tabs: Retiro en Cajero, Mi Código QR, Escanear QR
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF101117),
                contentColor = PrimaryViolet,
                edgePadding = 6.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryViolet
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Retiro en Cajero",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Color.White else TextSecondary
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Atm,
                            contentDescription = null,
                            tint = if (selectedTab == 0) PrimaryVioletLight else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Mi Código QR",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Color.White else TextSecondary
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            tint = if (selectedTab == 1) PrimaryVioletLight else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Escanear QR",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) Color.White else TextSecondary
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = if (selectedTab == 2) PrimaryVioletLight else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "Verificador",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) Color.White else TextSecondary
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (selectedTab == 3) PrimaryVioletLight else TextSecondary
                        )
                    }
                )
            }
        }

        // ================= TAB 0: RETIRO EN CAJERO CON RETENCIÓN Y PIN 2X =================
        if (selectedTab == 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
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
                            Text(
                                text = "Orden de Retiro sin Tarjeta",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Text(
                                text = "Saldo: ${Formatters.formatSolesHidden(account?.balance ?: 0.0, isBalanceHidden)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Active Pending Withdrawal Voucher (Retained for 1 hour)
                        if (activeWithdrawal != null && activeWithdrawal.status == "PENDING") {
                            val amt = activeWithdrawal.amount
                            val pin = activeWithdrawal.pinCode
                            val op = activeWithdrawal.opCode
                            val dniVal = userDni.ifBlank { "DOC" }
                            val withdrawQrData = activeWithdrawal.qrData.ifBlank {
                                "RETIRO-BCBANK:MONTO$amt:PIN$pin:OP$op:DNI$dniVal"
                            }
                            val encodedWithdrawQr = remember(withdrawQrData) {
                                URLEncoder.encode(withdrawQrData, StandardCharsets.UTF_8.toString())
                            }
                            val withdrawQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=$encodedWithdrawQr"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(EmeraldDark.copy(alpha = 0.35f))
                                    .border(1.dp, EmeraldPrimary, RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Live 1-hour retention timer badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(WarningYellow.copy(alpha = 0.2f))
                                            .border(1.dp, WarningYellow.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = WarningYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Saldo Retenido por 1 Hora: $timerString restante",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WarningYellow
                                            )
                                        }
                                    }

                                    // Display ATM QR
                                    Box(
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = withdrawQrUrl,
                                            contentDescription = "QR de Retiro",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(144.dp),
                                            loading = {
                                                CircularProgressIndicator(
                                                    color = EmeraldPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        )
                                    }

                                    Text(
                                        text = "Clave PIN Secreta para el Cajero:",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )

                                    Text(
                                        text = pin,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )

                                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Monto Retenido:", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            Formatters.formatSoles(amt),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldLight
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Código de Operación:", fontSize = 12.sp, color = TextSecondary)
                                        Text(op, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Comisión de Retiro:", fontSize = 12.sp, color = TextSecondary)
                                        Text("S/ 0.00 (Gratis)", fontSize = 12.sp, color = EmeraldLight)
                                    }
                                }
                            }

                            if (showWithdrawExportSuccess) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "¡Constancia de retiro guardada en galería!",
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            // Export voucher button
                            OutlinedButton(
                                onClick = { showWithdrawExportSuccess = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("export_withdraw_voucher_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardar Comprobante de Retiro", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Cancel Withdrawal Button (Restores funds with double PIN verification)
                            Button(
                                onClick = {
                                    cancelPin1 = ""
                                    cancelPin2 = ""
                                    cancelError = null
                                    showCancelDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ExpenseRed.copy(alpha = 0.15f),
                                    contentColor = ExpenseRedLight
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("cancel_withdrawal_button")
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = ExpenseRedLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancelar Retiro y Liberar Saldo (PIN x2)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Standard Form to Generate a New Withdrawal Reservation
                            Text(
                                text = "Ingresa el monto que deseas retirar en efectivo. El saldo quedará retenido por un plazo de 1 hora mientras te acercas a cualquier cajero BC-BANK a nivel nacional.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )

                            // Quick Amounts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(20, 50, 100, 200).forEach { amt ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (withdrawAmountText == amt.toString()) EmeraldDark else SurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (withdrawAmountText == amt.toString()) EmeraldPrimary else BorderDark,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                withdrawAmountText = amt.toString()
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "S/ $amt",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (withdrawAmountText == amt.toString()) EmeraldLight else TextPrimary
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = withdrawAmountText,
                                onValueChange = { withdrawAmountText = it },
                                label = { Text("Monto a retirar en Soles (S/)") },
                                placeholder = { Text("Ej: 150.00") },
                                leadingIcon = {
                                    Text("S/", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = EmeraldPrimary,
                                    unfocusedLabelColor = TextSecondary,
                                    focusedContainerColor = SurfaceCard,
                                    unfocusedContainerColor = SurfaceCard
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_amount_input")
                            )

                            Button(
                                onClick = {
                                    val amount = withdrawAmountText.toDoubleOrNull() ?: 0.0
                                    val randomPin = String.format("%04d", Random.nextInt(1000, 9999))
                                    val randomOp = "RET-" + String.format("%06d", Random.nextInt(100000, 999999))
                                    onCreateWithdrawalReservation(amount, randomPin, randomOp)
                                },
                                enabled = (withdrawAmountText.toDoubleOrNull() ?: 0.0) > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("generate_pin_button")
                            ) {
                                Text(
                                    text = "Generar Clave y Retener Saldo (1 Hora)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= TAB 1: MI CÓDIGO QR =================
        if (selectedTab == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Mi Código QR para Recibir Dinero",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Muestra este código para que otros usuarios y comercios te transfieran directamente a tu cuenta BC-BANK en Soles.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        val holder = account?.accountHolder?.ifBlank { "Usuario BC-BANK" } ?: "Usuario BC-BANK"
                        val displayPhone = userPhone.ifBlank { "Afiliado BC-BANK" }
                        val displayCci = account?.cciNumber.orEmpty()
                        val myQrData = "BCBANK:${userPhone.ifBlank { "BCBANK" }}:$holder:BCBANK_SOLES"
                        val encodedMyQr = remember(myQrData) {
                            URLEncoder.encode(myQrData, StandardCharsets.UTF_8.toString())
                        }
                        val personalQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=320x320&data=$encodedMyQr"

                        // Stylized Clean QR Card
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = personalQrUrl,
                                contentDescription = "Código QR Personal",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(206.dp),
                                loading = {
                                    CircularProgressIndicator(
                                        color = EmeraldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = holder,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Celular: $displayPhone • BC-BANK Soles",
                                fontSize = 13.sp,
                                color = EmeraldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (displayCci.isNotBlank()) {
                                Text(
                                    text = "CCI: $displayCci",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (showQrExportSuccess) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldDark.copy(alpha = 0.4f))
                                    .border(1.dp, EmeraldPrimary, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "¡Código QR guardado en la galería!",
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val qrBitmap = ImageExportUtils.generateBrandedQrCard(
                                        qrContent = myQrData,
                                        holderName = holder,
                                        phoneOrCci = displayPhone,
                                        accountType = "Cuenta Soles BC-BANK"
                                    )
                                    if (qrBitmap != null) {
                                        val savedUri = ImageExportUtils.saveBitmapToGallery(
                                            context = context,
                                            bitmap = qrBitmap,
                                            title = "BCBANK_QR_${displayPhone.filter { it.isLetterOrDigit() }}"
                                        )
                                        if (savedUri != null) {
                                            showQrExportSuccess = true
                                            Toast.makeText(context, "¡Código QR guardado en la galería!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error al guardar el código QR", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Error al generar imagen de QR", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(46.dp).testTag("save_my_qr_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardar QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val qrBitmap = ImageExportUtils.generateBrandedQrCard(
                                        qrContent = myQrData,
                                        holderName = holder,
                                        phoneOrCci = displayPhone,
                                        accountType = "Cuenta Soles BC-BANK"
                                    )
                                    ImageExportUtils.shareQrCard(
                                        context = context,
                                        bitmap = qrBitmap,
                                        holderName = holder,
                                        phoneOrCci = displayPhone
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(46.dp).testTag("share_my_qr_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compartir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ================= TAB 2: ESCANEAR QR (SOLO CLIENTES / PAGOS) =================
        if (selectedTab == 2) {
            item {
                CleanQrCameraScanner(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Escanear QR de Clientes",
                    subtitle = "Apunta la cámara al código QR generado en 'Mi Código QR' por otro usuario BC-BANK:",
                    onQrDetected = { rawData ->
                        when (val parsed = QrCodeParser.parseCustomerTransferQr(rawData)) {
                            is CustomerQrScanResult.ValidRecipient -> {
                                onScanTransfer(parsed.recipientName, parsed.identifier, parsed.amount)
                            }
                            is CustomerQrScanResult.VoucherCodeDetected -> {
                                scannedVoucherSerialForVerifier = parsed.serial
                                rejectedQrDialogData = Pair(
                                    "Comprobante de Operación Detectado",
                                    "Has escaneado el código QR de un comprobante bancario emitido (Serial: ${parsed.serial}).\n\nEl escáner de pagos solo reconoce códigos QR de clientes para transferir fondos. ¿Deseas auditar y verificar este comprobante en el Verificador Oficial?"
                                )
                            }
                            is CustomerQrScanResult.WithdrawalCodeDetected -> {
                                rejectedQrDialogData = Pair(
                                    "Clave de Retiro Detectada",
                                    "Has escaneado una orden o clave de retiro en cajero. El escáner de pagos únicamente admite códigos QR de clientes para transferir dinero."
                                )
                            }
                            is CustomerQrScanResult.PaymentOrCipCodeDetected -> {
                                rejectedQrDialogData = Pair(
                                    "${parsed.typeDesc} Detectado",
                                    "Has escaneado un código de pago o servicio. El escáner de transferencias únicamente reconoce códigos QR de clientes destinatarios."
                                )
                            }
                            is CustomerQrScanResult.NotAValidCustomerQr -> {
                                rejectedQrDialogData = Pair(
                                    "QR no Reconocido para Pagos",
                                    parsed.reason
                                )
                            }
                        }
                    }
                )
            }
        }

        // ================= TAB 3: VERIFICADOR DE COMPROBANTES =================
        if (selectedTab == 3) {
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                            Column {
                                Text(
                                    text = "Auditoría Antifraude BC-BANK",
                                    fontSize = 11.sp,
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Verificador de Comprobantes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Text(
                            text = "Escanea el código QR de cualquier comprobante emitido por BC-BANK o ingresa su número serial para comprobar su autenticidad e inmutabilidad en tiempo real:",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            item {
                CleanQrCameraScanner(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Escanear QR de Comprobante",
                    subtitle = "Apunta la cámara al código QR impreso en el voucher o constancia bancaria:",
                    onQrDetected = { rawData ->
                        if (rawData.startsWith("BCBANK:", ignoreCase = true)) {
                            Toast.makeText(
                                context,
                                "El código escaneado es un QR de cuenta de cliente. Para transferirle fondos, usa la pestaña 'Escanear QR'.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val cleanSerial = rawData.trim()
                                .removePrefix("BCBANK-VOUCHER:")
                                .removePrefix("BC-VOUCHER:")
                                .removePrefix("BCBANK_VOUCHER:")
                                .removePrefix("BC-")
                                .trim()
                            verifierInitialSerial = cleanSerial
                            showVerifierModal = true
                        }
                    }
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Búsqueda Manual por N° Serial",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ingresa los 6 dígitos del serial o el código de operación impreso en el voucher físico/digital:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = manualVoucherInput,
                            onValueChange = { manualVoucherInput = it.uppercase().filter { ch -> ch.isLetterOrDigit() || ch == '-' } },
                            label = { Text("Serial o N° Operación (ej. 729104)") },
                            placeholder = { Text("729104") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("manual_voucher_search_input")
                        )

                        Button(
                            onClick = {
                                if (manualVoucherInput.isNotBlank()) {
                                    val clean = manualVoucherInput.trim()
                                        .removePrefix("BCBANK-VOUCHER:")
                                        .removePrefix("BC-VOUCHER:")
                                        .removePrefix("BC-")
                                        .trim()
                                    verifierInitialSerial = clean
                                    showVerifierModal = true
                                } else {
                                    Toast.makeText(context, "Ingresa un número de serial", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_verify_manual_voucher")
                        ) {
                            Text("Verificar Autenticidad", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

    }

    // ================= CANCEL WITHDRAWAL MODAL (DOUBLE PIN VERIFICATION) =================
    if (showCancelDialog) {
        Dialog(onDismissRequest = {
            if (!isCancelling) showCancelDialog = false
        }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ExpenseRed.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ExpenseRedLight, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Liberar Saldo Retenido",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { if (!isCancelling) showCancelDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                        }
                    }

                    Text(
                        text = "Por seguridad bancaria, para cancelar este retiro y devolver el saldo retenido a tu cuenta disponible, ingresa tu PIN secreto de seguridad de 6 dígitos.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = cancelPin1,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                cancelPin1 = it
                                cancelError = null
                            }
                        },
                        label = { Text("Ingresa tu PIN de 6 dígitos") },
                        placeholder = { Text("••••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("cancel_pin1_input")
                    )

                    OutlinedTextField(
                        value = cancelPin2,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                cancelPin2 = it
                                cancelError = null
                            }
                        },
                        label = { Text("Confirma tu PIN (2da vez)") },
                        placeholder = { Text("••••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("cancel_pin2_input")
                    )

                    if (cancelError != null) {
                        Text(
                            text = cancelError!!,
                            fontSize = 12.sp,
                            color = ExpenseRedLight,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCancelDialog = false },
                            enabled = !isCancelling,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Atrás", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                if (cancelPin1.length != 6 || cancelPin2.length != 6) {
                                    cancelError = "Debes ingresar tu PIN de 6 dígitos en ambos campos."
                                    return@Button
                                }
                                if (cancelPin1 != cancelPin2) {
                                    cancelError = "Los PINs no coinciden. Verifica ambos campos."
                                    return@Button
                                }

                                isCancelling = true
                                cancelError = null
                                onCancelWithdrawalReservation(cancelPin1, cancelPin2) { success, msg ->
                                    isCancelling = false
                                    if (success) {
                                        showCancelDialog = false
                                    } else {
                                        cancelError = msg
                                    }
                                }
                            },
                            enabled = !isCancelling && cancelPin1.length == 6 && cancelPin2.length == 6,
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("confirm_cancel_withdrawal_button")
                        ) {
                            if (isCancelling) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Confirmar Cancelación", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= REJECTED QR DIALOG =================
    if (rejectedQrDialogData != null) {
        val (dialogTitle, dialogMsg) = rejectedQrDialogData!!
        Dialog(onDismissRequest = {
            rejectedQrDialogData = null
            scannedVoucherSerialForVerifier = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (scannedVoucherSerialForVerifier != null) EmeraldDark.copy(alpha = 0.5f) else ExpenseRed.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (scannedVoucherSerialForVerifier != null) EmeraldPrimary else ExpenseRedLight,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (scannedVoucherSerialForVerifier != null) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (scannedVoucherSerialForVerifier != null) EmeraldLight else ExpenseRedLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = dialogTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = dialogMsg,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                rejectedQrDialogData = null
                                scannedVoucherSerialForVerifier = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Cerrar", color = TextSecondary, fontSize = 13.sp)
                        }

                        if (scannedVoucherSerialForVerifier != null) {
                            Button(
                                onClick = {
                                    val serial = scannedVoucherSerialForVerifier ?: ""
                                    rejectedQrDialogData = null
                                    scannedVoucherSerialForVerifier = null
                                    verifierInitialSerial = serial
                                    showVerifierModal = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).height(44.dp)
                            ) {
                                Text("Verificar Voucher", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ================= VOUCHER VERIFIER MODAL =================
    if (showVerifierModal) {
        VoucherVerifierModal(
            initialSerial = verifierInitialSerial,
            onOpenQrScanner = {
                showVerifierModal = false
                selectedTab = 3
            },
            onDismiss = {
                showVerifierModal = false
                verifierInitialSerial = ""
            }
        )
    }
}
}
