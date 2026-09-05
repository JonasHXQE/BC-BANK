package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.CloudWithdrawal
import com.example.data.local.TransactionEntity
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.ImageExportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailScreen(
    transaction: TransactionEntity,
    activeWithdrawal: CloudWithdrawal?,
    onBack: () -> Unit,
    onCancelPendingWithdrawal: (String, (Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var isCancelling by remember { mutableStateOf(false) }

    val isIncome = transaction.type == "INCOME"
    val isWithdrawal = transaction.category.contains("Retiro", ignoreCase = true) ||
            transaction.title.contains("Retiro", ignoreCase = true)
    val opCode = transaction.referenceNumber.ifBlank {
        activeWithdrawal?.opCode ?: "OP-${transaction.id}"
    }

    val isPendingWithdrawal = isWithdrawal && activeWithdrawal != null &&
            (activeWithdrawal.opCode == opCode || activeWithdrawal.amount == transaction.amount) &&
            activeWithdrawal.status == "PENDING"

    val formattedDate = SimpleDateFormat("dd 'de' MMMM, yyyy - HH:mm", Locale("es", "PE"))
        .format(Date(transaction.timestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceCard, CircleShape)
                    .border(1.dp, BorderDark, CircleShape)
                    .testTag("tx_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Detalle del Movimiento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isIncome) EmeraldPrimary.copy(alpha = 0.5f) else BorderDark,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) EmeraldDark else ExpenseRed.copy(alpha = 0.2f)
                        )
                        .border(
                            1.5.dp,
                            if (isIncome) EmeraldPrimary else ExpenseRedLight,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIncome) EmeraldLight else ExpenseRedLight,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = transaction.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${if (isIncome) "+" else "-"} S/ ${String.format(Locale.US, "%.2f", transaction.amount)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) EmeraldLight else TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            if (isPendingWithdrawal) ExpenseRed.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPendingWithdrawal) "PENDIENTE DE COBRO" else "COMPLETADO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPendingWithdrawal) ExpenseRedLight else EmeraldLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info Details
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Fecha y Hora", value = formattedDate)
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

                DetailRow(label = "Categoría", value = transaction.category)
                HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))

                if (transaction.recipientOrSender.isNotBlank()) {
                    DetailRow(
                        label = if (isIncome) "De" else "Para",
                        value = transaction.recipientOrSender
                    )
                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                }

                DetailRow(label = "Número de Operación", value = opCode)

                if (transaction.note.isNotBlank()) {
                    HorizontalDivider(color = BorderDark, modifier = Modifier.padding(vertical = 12.dp))
                    DetailRow(label = "Mensaje / Nota", value = transaction.note)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Guardar Constancia y Compartir
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val bitmap = ImageExportUtils.generateReceiptBitmap(
                        title = transaction.title,
                        opCode = opCode,
                        amount = transaction.amount,
                        beneficiary = transaction.recipientOrSender.ifBlank { "BC-BANK Usuario" },
                        concept = transaction.note.ifBlank { transaction.title }
                    )
                    val uri = ImageExportUtils.saveBitmapToGallery(
                        context = context,
                        bitmap = bitmap,
                        title = "BCBANK_TX_${opCode.filter { it.isLetterOrDigit() }}"
                    )
                    if (uri != null) {
                        Toast.makeText(context, "¡Constancia guardada en imágenes!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al guardar comprobante", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("tx_save_receipt_button")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Guardar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    val bitmap = ImageExportUtils.generateReceiptBitmap(
                        title = transaction.title,
                        opCode = opCode,
                        amount = transaction.amount,
                        beneficiary = transaction.recipientOrSender.ifBlank { "BC-BANK Usuario" },
                        concept = transaction.note.ifBlank { transaction.title }
                    )
                    ImageExportUtils.shareReceiptCard(
                        context = context,
                        bitmap = bitmap,
                        opCode = opCode,
                        amount = transaction.amount,
                        beneficiary = transaction.recipientOrSender.ifBlank { "BC-BANK" }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("tx_share_receipt_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Compartir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // If pending withdrawal: Cancel reservation option
        if (isPendingWithdrawal) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isCancelling = true
                    onCancelPendingWithdrawal(opCode) { success, message ->
                        isCancelling = false
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            onBack()
                        }
                    }
                },
                enabled = !isCancelling,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("cancel_pending_withdrawal_button")
            ) {
                if (isCancelling) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancelar Reserva y Devolver Saldo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
