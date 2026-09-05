package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseManager
import com.example.data.local.AccountInfoEntity
import com.example.ui.components.Formatters
import com.example.ui.util.CustomCategoryManager
import kotlinx.coroutines.delay
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CurrencyPurple
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton

data class QuickContact(
    val name: String,
    val identifier: String,
    val initial: String,
    val type: String // "BC-BANK", "CCI", "Cuenta Bancaria"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    account: AccountInfoEntity?,
    isBalanceHidden: Boolean,
    initialRecipient: String = "",
    initialIdentifier: String = "",
    initialAmount: Double? = null,
    quickContacts: List<QuickContact> = emptyList(),
    onBack: () -> Unit,
    onPerformTransfer: (recipient: String, accountOrPhone: String, amount: Double, concept: String, category: String) -> Unit
) {
    val expenseCategories by CustomCategoryManager.expenseCategories.collectAsState()
    val availableCategories = remember(expenseCategories) {
        if (expenseCategories.isEmpty()) {
            listOf("No especificado")
        } else {
            expenseCategories.map { it.name } + listOf("No especificado")
        }
    }

    var destinationIdentifier by remember(initialIdentifier) { mutableStateOf(initialIdentifier) }
    var recipientName by remember(initialRecipient) { mutableStateOf(initialRecipient) }
    var recipientBank by remember { mutableStateOf("") }
    var isSearchingRecipient by remember { mutableStateOf(false) }
    var recipientFound by remember { mutableStateOf(initialRecipient.isNotBlank()) }
    var recipientNotFound by remember { mutableStateOf(false) }

    LaunchedEffect(destinationIdentifier) {
        val clean = destinationIdentifier.trim()
        val digits = clean.filter { it.isDigit() }
        if (digits.length < 8) {
            if (initialRecipient.isBlank()) {
                recipientName = ""
            }
            recipientBank = ""
            isSearchingRecipient = false
            recipientNotFound = false
            recipientFound = recipientName.isNotBlank()
            return@LaunchedEffect
        }

        isSearchingRecipient = true
        recipientNotFound = false
        delay(350)
        val result = FirebaseManager.lookupRecipientByIdentifier(clean)
        isSearchingRecipient = false
        if (result != null && result.found) {
            recipientName = result.fullName
            recipientBank = result.bankName
            recipientFound = true
            recipientNotFound = false
        } else {
            recipientName = ""
            recipientBank = ""
            recipientFound = false
            recipientNotFound = true
        }
    }

    var amountText by remember(initialAmount) {
        mutableStateOf(if (initialAmount != null && initialAmount > 0) "%.2f".format(initialAmount) else "")
    }
    var concept by remember { mutableStateOf("") }
    var selectedCategory by remember(availableCategories) {
        mutableStateOf(availableCategories.firstOrNull() ?: "No especificado")
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header with Back Button
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
                        .testTag("transfer_back_button")
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
                        text = "Transferir dinero",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = "Envía Soles a cuentas bancarias, CCI o DNI",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Available Balance & Source Account Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF191A24))
                                .border(1.dp, BorderGlass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = PrimaryVioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cuenta de origen",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${account?.bankName ?: "BC-BANK Soles"} • •••• ${account?.accountNumber?.takeLast(4) ?: "----"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Saldo disponible",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = Formatters.formatSolesHidden(account?.balance ?: 0.0, isBalanceHidden),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                }
            }
        }

        // Quick Contacts Carousel (only if user has frequent contacts)
        if (quickContacts.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Contactos frecuentes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(quickContacts) { contact ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF12131C))
                                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                                    .clickable {
                                        destinationIdentifier = contact.identifier
                                        recipientName = contact.name
                                        recipientFound = true
                                        recipientNotFound = false
                                    }
                                    .padding(12.dp)
                                    .width(90.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryViolet.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.initial,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC7D2FE)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = contact.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = contact.type,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Transfer Form
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF101117))
                    .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Datos de la transferencia",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = destinationIdentifier,
                        onValueChange = { input ->
                            destinationIdentifier = input.filter { it.isDigit() || it == '-' || it == ' ' }
                        },
                        label = { Text("N° de DNI (8 dígitos), Cuenta o CCI (20 dígitos)") },
                        placeholder = { Text("Ej. 72345678 o 002-194-000000000000-11") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryVioletLight) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFF151620),
                            unfocusedContainerColor = Color(0xFF12131A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_account_input")
                    )

                    // Real-time resolution status indicator
                    if (isSearchingRecipient) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = PrimaryVioletLight,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Buscando titular en el sistema...",
                                fontSize = 12.sp,
                                color = PrimaryVioletLight
                            )
                        }
                    } else if (recipientFound && recipientName.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verificado",
                                tint = IncomeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Titular verificado: $recipientName • $recipientBank",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IncomeGreen
                            )
                        }
                    } else if (recipientNotFound) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "No encontrado",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "No se encontró ningún titular asociado a este DNI, Cuenta o CCI",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444)
                            )
                        }
                    } else {
                        Text(
                            text = "Ingresa el DNI (8 dígitos), Cuenta o CCI para validar al titular automáticamente",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { /* Read-only, no manual typing allowed */ },
                        readOnly = true,
                        label = { Text("Nombre del destinatario (Autocargado)") },
                        placeholder = { Text("Se cargará en tiempo real al ingresar DNI/Cuenta/CCI") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryVioletLight) },
                        trailingIcon = {
                            if (recipientFound && recipientName.isNotBlank()) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Titular Validado", tint = IncomeGreen)
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (recipientFound) IncomeGreen else PrimaryViolet,
                            unfocusedBorderColor = if (recipientFound) IncomeGreen.copy(alpha = 0.5f) else BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFF151620),
                            unfocusedContainerColor = Color(0xFF12131A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_recipient_input")
                    )

                    // Quick amount shortcuts
                    Text(
                        text = "Montos rápidos en Soles:",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(20.0, 50.0, 100.0, 200.0).forEach { quickAmt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF151620))
                                    .border(1.dp, BorderGlass, RoundedCornerShape(10.dp))
                                    .clickable { amountText = quickAmt.toInt().toString() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S/ ${quickAmt.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFC7D2FE)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto a transferir (S/)") },
                        placeholder = { Text("0.00") },
                        prefix = { Text("S/ ", color = AccentGold, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFF151620),
                            unfocusedContainerColor = Color(0xFF12131A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_amount_input")
                    )

                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text("Concepto o motivo") },
                        placeholder = { Text("Ej. Pago de almuerzo, Alquiler") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFF151620),
                            unfocusedContainerColor = Color(0xFF12131A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría del gasto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryViolet,
                                unfocusedBorderColor = BorderGlass,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color(0xFF151620),
                                unfocusedContainerColor = Color(0xFF12131A)
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFF181924))
                        ) {
                            availableCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = TextPrimary) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val currentBalance = account?.balance ?: 0.0
                    val hasSufficientBalance = amount <= currentBalance
                    val isAmountValid = amount > 0 && hasSufficientBalance
                    val canTransfer = isAmountValid && recipientFound && recipientName.isNotBlank() && !isSearchingRecipient

                    if (amount > currentBalance) {
                        Text(
                            text = "Saldo insuficiente en tu cuenta (Saldo disponible: ${Formatters.formatSoles(currentBalance)})",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val finalCategory = selectedCategory.ifBlank { "No especificado" }
                            onPerformTransfer(recipientName, destinationIdentifier.trim(), amount, concept, finalCategory)
                        },
                        enabled = canTransfer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryViolet,
                            disabledContainerColor = PrimaryViolet.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_transfer_button")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val buttonLabel = when {
                            !recipientFound -> "Ingresa titular válido"
                            !hasSufficientBalance -> "Saldo insuficiente"
                            amount <= 0.0 -> "Ingresa un monto"
                            else -> "Transferir ahora"
                        }
                        Text(
                            text = buttonLabel,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
}
