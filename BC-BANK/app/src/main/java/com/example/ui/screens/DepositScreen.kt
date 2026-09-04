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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.example.data.local.AccountInfoEntity
import com.example.ui.components.Formatters
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class DepositMethod {
    BANK_TRANSFER,
    AGENT
}

@Composable
fun DepositScreen(
    account: AccountInfoEntity?,
    isBalanceHidden: Boolean,
    onBack: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf(DepositMethod.BANK_TRANSFER) }
    var isCciCopied by remember { mutableStateOf(false) }
    var isAccCopied by remember { mutableStateOf(false) }
    var isCipCopied by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val cciNumber = account?.cciNumber.orEmpty().ifBlank { "002-194-009182736450-45" }
    val accountNumber = account?.accountNumber.orEmpty().ifBlank { "194-82910482-0-88" }
    val holderName = account?.accountHolder?.ifBlank { "Usuario BC-BANK" } ?: "Usuario BC-BANK"
    
    val currentUid = remember { com.example.data.firebase.FirebaseManager.getCurrentUserUid().orEmpty() }
    val cipCode = remember(currentUid, accountNumber, holderName) {
        val seed = currentUid.ifBlank { accountNumber.ifBlank { holderName } }
        com.example.data.firebase.FirebaseManager.generateCleanCipCode(seed)
    }

    androidx.compose.runtime.LaunchedEffect(currentUid, cipCode, accountNumber) {
        if (currentUid.isNotBlank() && currentUid != "local_user") {
            com.example.data.firebase.FirebaseManager.syncUserCipCodeToFirestore(
                uid = currentUid,
                fullName = holderName,
                dni = "",
                accountNumber = accountNumber,
                cciNumber = cciNumber
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        // Atmospheric ambient blur glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x356D5BFF),
                            Color(0x1038BDF8),
                            Color.Transparent
                        ),
                        radius = 600f,
                        center = Offset(x = 600f, y = 60f)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF151620))
                        .border(1.dp, BorderGlass, CircleShape)
                        .testTag("deposit_back_button")
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
                        text = "Datos de depósito",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = "Información oficial de tu cuenta en Soles (S/)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. Current Balance Preview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF101117))
                    .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Saldo actual en cuenta",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatSolesHidden(account?.balance ?: 0.0, isBalanceHidden),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF241D42))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PEN (S/)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryVioletLight
                        )
                    }
                }
            }
        }

        // 3. Deposit Method Selector
        item {
            Text(
                text = "Canales de Abono Disponibles",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DepositMethodTab(
                    title = "Transferencia CCI",
                    icon = Icons.Default.AccountBalance,
                    isSelected = selectedMethod == DepositMethod.BANK_TRANSFER,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedMethod = DepositMethod.BANK_TRANSFER }
                )
                DepositMethodTab(
                    title = "En Agente BC-BANK",
                    icon = Icons.Default.Storefront,
                    isSelected = selectedMethod == DepositMethod.AGENT,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedMethod = DepositMethod.AGENT }
                )
            }
        }

        // 4. Method Details Form
        when (selectedMethod) {
            DepositMethod.BANK_TRANSFER -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDark)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "Datos para Transferencia Bancaria e Interbancaria",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )

                                Text(
                                    text = "Comparte estos datos con cualquier persona o empresa para que te transfieran directamente desde cualquier banco o billetera digital.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 17.sp
                                )

                                Column {
                                    Text("Código de Cuenta Interbancario (CCI)", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceElevated)
                                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cciNumber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(cciNumber))
                                                isCciCopied = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCciCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Copiar CCI",
                                                tint = if (isCciCopied) EmeraldPrimary else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text("Número de Cuenta BC-BANK (Soles)", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceElevated)
                                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = accountNumber,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(accountNumber))
                                                isAccCopied = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAccCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Copiar Número de Cuenta",
                                                tint = if (isAccCopied) EmeraldPrimary else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text("Titular de Cuenta", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = holderName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }

                                Column {
                                    Text("Entidad Bancaria", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = account?.bankName ?: "BC-BANK Perú",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EmeraldDark.copy(alpha = 0.5f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Recepción de transferencias inmediata y 100% libre de comisiones.",
                                        fontSize = 11.sp,
                                        color = EmeraldLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            DepositMethod.AGENT -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceDark)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Abono en Agente / Ventanilla BC-BANK",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )

                                Text(
                                    text = "Muestra este código CIP o el QR en cualquier ventanilla o agente autorizado BC-BANK para abonar efectivo al instante.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 17.sp
                                )

                                val rawCipDigits = remember(cipCode) { cipCode.filter { it.isDigit() } }
                                val qrPayload = "BCBANK_CIP:$rawCipDigits|ACC:$accountNumber|HOLDER:$holderName|CCI:$cciNumber"
                                val encodedCipPayload = remember(qrPayload) {
                                    URLEncoder.encode(qrPayload, StandardCharsets.UTF_8.toString())
                                }
                                val cipQrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data=$encodedCipPayload"

                                // QR Container
                                Box(
                                    modifier = Modifier
                                        .size(190.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = cipQrUrl,
                                        contentDescription = "Código QR CIP para Abono",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Código Único de Pago (CIP Personal)", fontSize = 11.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceElevated)
                                            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cipCode,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = EmeraldPrimary
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(cipCode))
                                                isCipCopied = true
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCipCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Copiar CIP",
                                                tint = if (isCipCopied) EmeraldPrimary else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Associated Account Details for Admin / Agent
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceElevated)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Datos de Cuenta Vinculada al CIP:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                                        Text("• Titular: $holderName", fontSize = 11.sp, color = TextSecondary)
                                        Text("• Cuenta Soles: $accountNumber", fontSize = 11.sp, color = TextSecondary)
                                        Text("• CCI: $cciNumber", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                        .testTag("cip_instructions_box")
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("📌 Instrucciones para Agente / Administrador:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                                        Text("1. El usuario proporciona su código $cipCode o muestra el código QR.", fontSize = 11.sp, color = TextSecondary)
                                        Text("2. El sistema identifica automáticamente al titular $holderName y su cuenta $accountNumber.", fontSize = 11.sp, color = TextSecondary)
                                        Text("3. El abono se acredita al instante sin cargos ni comisiones.", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun DepositMethodTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF241D42) else Color(0xFF13141C)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryViolet else BorderGlass
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) PrimaryVioletLight else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFFC7D2FE) else TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
