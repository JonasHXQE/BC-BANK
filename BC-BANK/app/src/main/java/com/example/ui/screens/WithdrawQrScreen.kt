package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.CloudWithdrawal
import com.example.data.local.AccountInfoEntity
import com.example.ui.components.CleanQrCameraScanner
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun WithdrawQrScreen(
    account: AccountInfoEntity?,
    userPhone: String,
    userDni: String,
    isBalanceHidden: Boolean,
    activeWithdrawal: CloudWithdrawal?,
    onBack: () -> Unit,
    onCreateWithdrawalReservation: (amount: Double, pin: String, opCode: String) -> Unit,
    onCancelWithdrawalReservation: (pin1: String, pin2: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onWithdrawFunds: (amount: Double, method: String, pinCode: String) -> Unit,
    onScanTransfer: (recipient: String, identifier: String, amount: Double) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Retiro Agente", "Mi QR", "Escanear")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceCard, CircleShape)
                    .border(1.dp, BorderDark, CircleShape)
                    .testTag("withdraw_qr_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Operaciones QR y Retiro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Retiros sin tarjeta, cobros y transferencias",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = BackgroundDark,
            contentColor = EmeraldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (selectedTabIndex == index) EmeraldLight else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> AgentWithdrawalTab(
                account = account,
                isBalanceHidden = isBalanceHidden,
                activeWithdrawal = activeWithdrawal,
                onCreateReservation = onCreateWithdrawalReservation,
                onCancelReservation = onCancelWithdrawalReservation
            )
            1 -> MyQrTab(
                account = account,
                userPhone = userPhone,
                userDni = userDni
            )
            2 -> ScanQrTab(
                onScanTransfer = onScanTransfer
            )
        }
    }
}

@Composable
private fun AgentWithdrawalTab(
    account: AccountInfoEntity?,
    isBalanceHidden: Boolean,
    activeWithdrawal: CloudWithdrawal?,
    onCreateReservation: (amount: Double, pin: String, opCode: String) -> Unit,
    onCancelReservation: (pin1: String, pin2: String, onResult: (Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val currentBalance = account?.balance ?: 0.0

    var amountInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var isCancelling by remember { mutableStateOf(false) }

    val quickAmounts = listOf(20.0, 50.0, 100.0, 200.0, 500.0)

    val isPending = activeWithdrawal != null && activeWithdrawal.status == "PENDING"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (isPending && activeWithdrawal != null) {
            // ACTIVE RESERVATION TICKET
            ActiveWithdrawalTicket(
                withdrawal = activeWithdrawal,
                isCancelling = isCancelling,
                onCancel = {
                    isCancelling = true
                    onCancelReservation(activeWithdrawal.pinCode, activeWithdrawal.opCode) { success, msg ->
                        isCancelling = false
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            // CREATE NEW WITHDRAWAL FORM
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Retiro en Agente o Cajero",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Saldo disponible: ${if (isBalanceHidden) "••••••" else "S/ " + String.format(Locale.US, "%.2f", currentBalance)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Monto a retirar (S/)", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) amountInput = input
                        },
                        placeholder = { Text("0.00", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick amount chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { amt ->
                            val isSelected = amountInput == amt.toInt().toString()
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldPrimary else SurfaceElevated)
                                    .clickable { amountInput = amt.toInt().toString() }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "S/ ${amt.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Crea tu Clave Secreta de 4 dígitos", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) pinInput = input
                        },
                        placeholder = { Text("Ej. 1234", color = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = EmeraldLight)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_pin_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
                    val isFormValid = enteredAmount > 0 && enteredAmount <= currentBalance && pinInput.length == 4

                    Button(
                        onClick = {
                            if (enteredAmount <= 0) {
                                Toast.makeText(context, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (enteredAmount > currentBalance) {
                                Toast.makeText(context, "Saldo insuficiente para reservar el retiro", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (pinInput.length != 4) {
                                Toast.makeText(context, "La clave debe ser de 4 dígitos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val randomOp = "OP-${Random.nextInt(100000, 999999)}"
                            onCreateReservation(enteredAmount, pinInput, randomOp)
                            Toast.makeText(context, "¡Reserva de retiro generada con éxito!", Toast.LENGTH_SHORT).show()
                            amountInput = ""
                            pinInput = ""
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_withdrawal_reservation_button")
                    ) {
                        Text(
                            text = "Generar Clave de Retiro",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¿Cómo funciona?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Genera tu reserva eligiendo el monto y tu clave secreta de 4 dígitos.\n" +
                                    "2. Acércate a cualquier Agente o Cajero asociado BC-BANK.\n" +
                                    "3. Proporciona tu DNI y la clave secreta o escanea el QR generado.\n" +
                                    "4. Tu dinero se entregará de inmediato sin necesidad de tu tarjeta física.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveWithdrawalTicket(
    withdrawal: CloudWithdrawal,
    isCancelling: Boolean,
    onCancel: () -> Unit
) {
    val qrBitmap = remember(withdrawal.opCode, withdrawal.qrData) {
        val qrContent = withdrawal.qrData.ifBlank { "BCBANK:WITHDRAW:${withdrawal.opCode}:${withdrawal.amount}" }
        generateQrBitmap(qrContent, 400)
    }

    val expiresFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(withdrawal.expiresAt))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, EmeraldPrimary, RoundedCornerShape(20.dp))
            .testTag("active_withdrawal_ticket")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(EmeraldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "RESERVA ACTIVA LISTA PARA COBRAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "S/ ${String.format(Locale.US, "%.2f", withdrawal.amount)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code image
            if (qrBitmap != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR de Retiro",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Codes Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Código de Operación", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = withdrawal.opCode,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldLight
                        )
                    }

                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Clave Secreta (4 dígitos)", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = withdrawal.pinCode,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = WarningYellow
                        )
                    }

                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Válido hasta", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = expiresFormatted,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onCancel,
                enabled = !isCancelling,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("cancel_withdrawal_reservation_button")
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cancelar Retiro y Liberar Saldo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MyQrTab(
    account: AccountInfoEntity?,
    userPhone: String,
    userDni: String
) {
    val phone = userPhone.ifBlank { "987654321" }
    val qrData = "BCBANK:PAY:PHONE=$phone:NAME=${account?.accountHolder ?: "Usuario"}"
    val qrBitmap = remember(qrData) { generateQrBitmap(qrData, 450) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = account?.accountHolder ?: "Titular de Cuenta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Celular: $phone",
                    fontSize = 14.sp,
                    color = EmeraldLight,
                    fontWeight = FontWeight.SemiBold
                )

                if (userDni.isNotBlank()) {
                    Text(text = "DNI: $userDni", fontSize = 12.sp, color = TextMuted)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (qrBitmap != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Mi Código QR para recibir dinero",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Muestra este código para recibir dinero al instante desde cualquier aplicación o cajero.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ScanQrTab(
    onScanTransfer: (recipient: String, identifier: String, amount: Double) -> Unit
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        CleanQrCameraScanner(
            title = "Escanear QR de Pago o Retiro",
            subtitle = "Apunta al código QR para cobrar, pagar o transferir",
            modifier = Modifier.fillMaxSize(),
            onQrDetected = { rawData ->
                // Example format: BCBANK:PAY:PHONE=987654321:NAME=Juan
                if (rawData.contains("BCBANK:PAY", ignoreCase = true)) {
                    val phonePart = rawData.split(":").find { it.startsWith("PHONE=") }?.removePrefix("PHONE=") ?: ""
                    val namePart = rawData.split(":").find { it.startsWith("NAME=") }?.removePrefix("NAME=") ?: "Destinatario QR"
                    onScanTransfer(namePart, phonePart, 0.0)
                } else if (rawData.all { it.isDigit() } && (rawData.length == 9 || rawData.length == 8)) {
                    onScanTransfer("Destinatario", rawData, 0.0)
                } else {
                    Toast.makeText(context, "QR detectado: $rawData", Toast.LENGTH_SHORT).show()
                    onScanTransfer("Destinatario QR", rawData, 0.0)
                }
            }
        )
    }
}

private fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
