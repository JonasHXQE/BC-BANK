package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import com.example.ui.util.ImageExportUtils
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.TransactionEntity
import com.example.ui.components.Formatters
import com.example.ui.components.getCategoryIcon
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
import com.example.ui.theme.IncomeGreen
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
import com.example.data.firebase.CloudWithdrawal
import com.example.data.firebase.FirebaseManager
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun TransactionDetailScreen(
    transaction: TransactionEntity,
    activeWithdrawal: CloudWithdrawal? = null,
    onBack: () -> Unit,
    onCancelPendingWithdrawal: ((opCode: String, onResult: (Boolean, String) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val isIncome = transaction.type == "INCOME"
    val clipboardManager = LocalClipboardManager.current
    var showExportSuccess by remember { mutableStateOf(false) }
    var copiedLabel by remember { mutableStateOf<String?>(null) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }
    var wasCancelledSuccess by remember { mutableStateOf(false) }
    var liveCloudStatus by remember { mutableStateOf<String?>(null) }

    // Live remote checking if active
    LaunchedEffect(transaction.referenceNumber) {
        val opCodeClean = transaction.referenceNumber.removePrefix("RET-")
        while (true) {
            val remote = FirebaseManager.findWithdrawalByCodeOrPin(opCodeClean)
            if (remote != null) {
                liveCloudStatus = remote.status
            }
            delay(2000L)
        }
    }

    val isCancelled = wasCancelledSuccess ||
            liveCloudStatus == "CANCELLED" ||
            liveCloudStatus == "EXPIRED" ||
            activeWithdrawal?.status == "CANCELLED" ||
            activeWithdrawal?.status == "EXPIRED" ||
            transaction.title.contains("Cancelad", ignoreCase = true) ||
            transaction.note.contains("Cancelad", ignoreCase = true)

    val isCompletedWithdrawal = liveCloudStatus == "COMPLETED" ||
            activeWithdrawal?.status == "COMPLETED" ||
            transaction.title.contains("Cobrado", ignoreCase = true) ||
            transaction.note.contains("Cobrado", ignoreCase = true) ||
            transaction.note.contains("Completado", ignoreCase = true) ||
            transaction.title.contains("Efectuado", ignoreCase = true)

    // Check if this transaction represents a pending ATM withdrawal
    val isWithdrawalOp = transaction.referenceNumber.startsWith("RET-", ignoreCase = true) ||
            transaction.title.contains("Retiro", ignoreCase = true) ||
            transaction.recipientOrSender.contains("Cajero", ignoreCase = true)

    val isPendingWithdrawalCandidate = isWithdrawalOp && transaction.type == "EXPENSE" && !isCancelled && !isCompletedWithdrawal

    // Live countdown timer for 1 hour retention
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isPendingWithdrawalCandidate) {
        if (isPendingWithdrawalCandidate) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    val elapsedMs = (currentTime - transaction.timestamp).coerceAtLeast(0L)
    val isWithinOneHour = elapsedMs < 3600000L
    val remainingSeconds = ((3600000L - elapsedMs) / 1000L).coerceAtLeast(0L)
    val remMin = remainingSeconds / 60L
    val remSec = remainingSeconds % 60L
    val remainingTimerStr = String.format("%02d:%02d", remMin, remSec)

    // Official 6-digit antifraud serial number
    val voucherSerial = remember(transaction.referenceNumber, transaction.id, transaction.timestamp) {
        val hash = kotlin.math.abs((transaction.referenceNumber + transaction.timestamp.toString()).hashCode())
        String.format(java.util.Locale.US, "%06d", (hash % 900000) + 100000)
    }

    LaunchedEffect(voucherSerial) {
        try {
            com.example.data.firebase.FirebaseManager.registerVoucherSerial(
                serial = voucherSerial,
                opCode = transaction.referenceNumber.ifBlank { "OP-${transaction.id}" },
                amount = transaction.amount,
                title = transaction.title,
                senderOrRecipient = transaction.recipientOrSender.ifBlank { "Titular BC-BANK" },
                type = transaction.type
            )
        } catch (_: Exception) {}
    }

    val qrData = "BCBANK-VOUCHER:$voucherSerial"
    val encodedQr = remember(qrData) {
        URLEncoder.encode(qrData, StandardCharsets.UTF_8.toString())
    }
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=$encodedQr"

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar with Back Button
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
                            .testTag("tx_detail_back_button")
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
                            text = "Constancia de Operación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TextPrimary
                        )
                        Text(
                            text = "Comprobante digital verificado en Soles (PEN)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 1.4 Container when withdrawal was cancelled
            if (isCancelled && isWithdrawalOp) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRedLight.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = ExpenseRedLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Orden de Retiro Cancelada",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRedLight
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ExpenseRed.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "REINTEGRADO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ExpenseRedLight
                                    )
                                }
                            }
                            Text(
                                text = "Esta orden de retiro fue cancelada exitosamente. El saldo retenido de S/ ${String.format("%.2f", transaction.amount)} fue liberado e integrado a tu saldo disponible en cuenta.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 1.5 Container when withdrawal was completed/dispensed at ATM
            if (isCompletedWithdrawal && isWithdrawalOp) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldDark.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Retiro Cobrado en Cajero",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldPrimary.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ENTREGADO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldLight
                                    )
                                }
                            }
                            Text(
                                text = "El dinero en efectivo fue dispensado exitosamente en el cajero automático / agente BC-BANK.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 1.6 Live 1-Hour Pending Withdrawal Banner (if active & not cancelled)
            if (isPendingWithdrawalCandidate && isWithinOneHour) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningYellow.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = WarningYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Retiro Pendiente de Cobro",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningYellow
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WarningYellow.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$remainingTimerStr restante",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WarningYellow,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Text(
                                text = "El saldo de este retiro está retenido por 1 hora. Puedes acercarte a un cajero BC-BANK o cancelarlo aquí antes de que expire para reintegrar los fondos a tu cuenta.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = { showCancelConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ExpenseRed.copy(alpha = 0.18f),
                                    contentColor = ExpenseRedLight
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detail_cancel_withdrawal_button")
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = ExpenseRedLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cancelar Retiro y Liberar Saldo (S/ ${String.format("%.2f", transaction.amount)})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 2. Receipt Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Category & Status Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCancelled -> ExpenseRed.copy(alpha = 0.2f)
                                        isCompletedWithdrawal -> PrimaryVioletDark
                                        isIncome -> PrimaryVioletDark
                                        isPendingWithdrawalCandidate && isWithinOneHour -> WarningYellow.copy(alpha = 0.2f)
                                        else -> SurfaceElevated
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isCancelled -> ExpenseRedLight
                                        isCompletedWithdrawal -> PrimaryViolet
                                        isIncome -> PrimaryViolet
                                        isPendingWithdrawalCandidate && isWithinOneHour -> WarningYellow
                                        else -> BorderGlass
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isCancelled -> Icons.Default.Cancel
                                    isCompletedWithdrawal -> Icons.Default.CheckCircle
                                    isWithdrawalOp -> Icons.Default.Atm
                                    else -> getCategoryIcon(transaction.category)
                                },
                                contentDescription = transaction.category,
                                tint = when {
                                    isCancelled -> ExpenseRedLight
                                    isCompletedWithdrawal -> AccentCyan
                                    isIncome -> IncomeGreen
                                    isPendingWithdrawalCandidate && isWithinOneHour -> WarningYellow
                                    else -> TextPrimary
                                },
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Amount Display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val sign = if (isIncome || isCancelled) "+ " else "- "
                            val color = when {
                                isCancelled -> ExpenseRedLight
                                isCompletedWithdrawal -> AccentCyan
                                isIncome -> IncomeGreen
                                else -> TextPrimary
                            }

                            Text(
                                text = "$sign${Formatters.formatSoles(transaction.amount)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Serif,
                                color = color
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    isCancelled -> "Retiro Cancelado (Reintegrado)"
                                    isCompletedWithdrawal -> "Retiro en Cajero (Cobrado)"
                                    else -> transaction.title
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Badge Operación Status
                        val badgeBg = when {
                            isCancelled -> ExpenseRed.copy(alpha = 0.3f)
                            isCompletedWithdrawal -> EmeraldDark.copy(alpha = 0.5f)
                            isPendingWithdrawalCandidate && isWithinOneHour -> WarningYellow.copy(alpha = 0.2f)
                            isPendingWithdrawalCandidate && !isWithinOneHour -> SurfaceElevated
                            else -> EmeraldDark.copy(alpha = 0.5f)
                        }
                        val badgeBorder = when {
                            isCancelled -> ExpenseRedLight
                            isCompletedWithdrawal -> EmeraldPrimary.copy(alpha = 0.4f)
                            isPendingWithdrawalCandidate && isWithinOneHour -> WarningYellow
                            isPendingWithdrawalCandidate && !isWithinOneHour -> BorderDark
                            else -> EmeraldPrimary.copy(alpha = 0.4f)
                        }
                        val badgeTextColor = when {
                            isCancelled -> ExpenseRedLight
                            isCompletedWithdrawal -> EmeraldLight
                            isPendingWithdrawalCandidate && isWithinOneHour -> WarningYellow
                            isPendingWithdrawalCandidate && !isWithinOneHour -> TextMuted
                            else -> EmeraldLight
                        }
                        val badgeText = when {
                            isCancelled -> "Retiro Cancelado • Saldo Reintegrado"
                            isCompletedWithdrawal -> "Retiro Completado • Efectivo Entregado"
                            isPendingWithdrawalCandidate && isWithinOneHour -> "Retiro Pendiente • Saldo Retenido (< 1h)"
                            isPendingWithdrawalCandidate && !isWithinOneHour -> "Orden de Retiro Expirada (> 1h)"
                            else -> "Operación Exitosa • Acreditada"
                        }
                        val badgeIcon = when {
                            isCancelled -> Icons.Default.Cancel
                            isCompletedWithdrawal -> Icons.Default.CheckCircle
                            isPendingWithdrawalCandidate && isWithinOneHour -> Icons.Default.Timer
                            isPendingWithdrawalCandidate && !isWithinOneHour -> Icons.Default.HourglassEmpty
                            else -> Icons.Default.CheckCircle
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(badgeBg)
                                .border(1.dp, badgeBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = badgeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }

                        HorizontalDivider(color = BorderDark, thickness = 1.dp)

                        // Details breakdown
                        DetailRow(
                            label = "Código de Operación",
                            value = transaction.referenceNumber.ifBlank { "OP-982341" },
                            isMonospace = true,
                            isCopyable = true,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(transaction.referenceNumber))
                                copiedLabel = "Código copiado"
                                Toast.makeText(context, "Código de operación copiado", Toast.LENGTH_SHORT).show()
                            }
                        )

                        DetailRow(
                            label = "N° Serial Antifraude",
                            value = voucherSerial,
                            isMonospace = true,
                            isCopyable = true,
                            valueColor = EmeraldLight,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(voucherSerial))
                                Toast.makeText(context, "Serial $voucherSerial copiado", Toast.LENGTH_SHORT).show()
                            }
                        )

                        DetailRow(
                            label = "Fecha y Hora",
                            value = Formatters.formatFullDate(transaction.timestamp)
                        )

                        DetailRow(
                            label = if (isIncome) "Origen / Emisor" else "Destino / Beneficiario",
                            value = transaction.recipientOrSender.ifBlank { "Cuenta BC-BANK" }
                        )

                        DetailRow(
                            label = "Categoría",
                            value = transaction.category
                        )

                        if (transaction.note.isNotBlank()) {
                            DetailRow(
                                label = "Concepto / Motivo",
                                value = transaction.note
                            )
                        }

                        DetailRow(
                            label = "Comisión de Transacción",
                            value = "S/ 0.00 (Sin Costo)",
                            valueColor = EmeraldLight
                        )

                        DetailRow(
                            label = "Moneda Oficial",
                            value = "Soles Peruanos (PEN - S/)"
                        )

                        HorizontalDivider(color = BorderDark, thickness = 1.dp)

                        // Verification QR Code section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "QR de Verificación Bancaria",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = qrUrl,
                                    contentDescription = "QR de Constancia",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(114.dp),
                                    loading = {
                                        CircularProgressIndicator(
                                            color = EmeraldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Escanea este QR para validar la autenticidad en cualquier agencia o app",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 3. Export Notification Feedback
            if (showExportSuccess) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(EmeraldDark.copy(alpha = 0.4f))
                            .border(1.dp, EmeraldPrimary, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "¡Constancia exportada y guardada como imagen con éxito!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 4. Action Buttons (Export voucher, Share, Back)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val bitmap = ImageExportUtils.generateReceiptBitmap(
                                    title = if (wasCancelledSuccess) "Retiro Cancelado y Reintegrado" else transaction.title,
                                    opCode = transaction.referenceNumber.ifBlank { "OP-${transaction.id}" },
                                    amount = transaction.amount,
                                    beneficiary = transaction.recipientOrSender.ifBlank { "Cuenta BC-BANK" },
                                    concept = transaction.note.ifBlank { transaction.category },
                                    serial = voucherSerial
                                )
                                val savedUri = ImageExportUtils.saveBitmapToGallery(
                                    context = context,
                                    bitmap = bitmap,
                                    title = "BCBANK_CONSTANCIA_${transaction.referenceNumber.filter { it.isLetterOrDigit() }.ifBlank { "${System.currentTimeMillis()}" }}"
                                )
                                if (savedUri != null) {
                                    showExportSuccess = true
                                    Toast.makeText(context, "¡Constancia guardada en la galería!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error al guardar la constancia", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("export_voucher_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Guardar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val bitmap = ImageExportUtils.generateReceiptBitmap(
                                    title = if (wasCancelledSuccess) "Retiro Cancelado y Reintegrado" else transaction.title,
                                    opCode = transaction.referenceNumber.ifBlank { "OP-${transaction.id}" },
                                    amount = transaction.amount,
                                    beneficiary = transaction.recipientOrSender.ifBlank { "Cuenta BC-BANK" },
                                    concept = transaction.note.ifBlank { transaction.category },
                                    serial = voucherSerial
                                )
                                ImageExportUtils.shareReceiptCard(
                                    context = context,
                                    bitmap = bitmap,
                                    opCode = transaction.referenceNumber.ifBlank { "OP-${transaction.id}" },
                                    amount = transaction.amount,
                                    beneficiary = transaction.recipientOrSender.ifBlank { "Cuenta BC-BANK" }
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("share_voucher_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Compartir",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cerrar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Cancel Confirmation Dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCancelling) showCancelConfirmDialog = false },
            title = {
                Text(
                    text = "Cancelar Orden de Retiro",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "¿Deseas cancelar la orden de retiro ${transaction.referenceNumber}?",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "El monto retenido de S/ ${String.format("%.2f", transaction.amount)} será liberado y reintegrado a tu saldo disponible inmediatamente.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    if (isCancelling) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Text("Procesando cancelación...", color = EmeraldLight, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onCancelPendingWithdrawal != null) {
                            isCancelling = true
                            onCancelPendingWithdrawal(transaction.referenceNumber) { success, msg ->
                                isCancelling = false
                                showCancelConfirmDialog = false
                                if (success) {
                                    wasCancelledSuccess = true
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            showCancelConfirmDialog = false
                        }
                    },
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, Cancelar Retiro", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelConfirmDialog = false },
                    enabled = !isCancelling
                ) {
                    Text("Volver", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    isMonospace: Boolean = false,
    isCopyable: Boolean = false,
    onCopy: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                color = valueColor
            )
            if (isCopyable) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar",
                    tint = EmeraldPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onCopy)
                )
            }
        }
    }
}
