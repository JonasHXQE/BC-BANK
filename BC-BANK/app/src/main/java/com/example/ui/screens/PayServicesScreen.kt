package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.PublicService
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
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

private fun getServiceIconAndColor(category: String): Pair<ImageVector, Color> {
    val cat = category.lowercase()
    return when {
        cat.contains("electri") || cat.contains("luz") -> Icons.Default.ElectricBolt to Color(0xFFF59E0B)
        cat.contains("agua") || cat.contains("sedapal") -> Icons.Default.WaterDrop to Color(0xFF3B82F6)
        cat.contains("gas") || cat.contains("cálidda") || cat.contains("calidda") -> Icons.Default.LocalGasStation to Color(0xFFEC4899)
        cat.contains("teleco") || cat.contains("internet") || cat.contains("wifi") -> Icons.Default.Wifi to Color(0xFF06B6D4)
        cat.contains("móvil") || cat.contains("movil") || cat.contains("telefo") -> Icons.Default.PhoneAndroid to Color(0xFF10B981)
        cat.contains("municip") || cat.contains("sat") || cat.contains("tribut") || cat.contains("sunat") -> Icons.Default.Apartment to Color(0xFF8B5CF6)
        cat.contains("tarjeta") || cat.contains("crédito") || cat.contains("financ") -> Icons.Default.CreditCard to Color(0xFFEAB308)
        cat.contains("educa") || cat.contains("univers") || cat.contains("coleg") -> Icons.Default.School to Color(0xFF6366F1)
        else -> Icons.Default.Receipt to EmeraldPrimary
    }
}

private val ALL_CATEGORIES = listOf(
    "Todos",
    "Luz y Electricidad",
    "Agua Potable",
    "Gas Natural",
    "Telecomunicaciones & Internet",
    "Telefonía Móvil",
    "Impuestos y Municipalidad",
    "Educación",
    "Tarjetas y Financiero"
)

private val PRESET_AMOUNTS = listOf(30.0, 50.0, 80.0, 120.0, 200.0)

@Composable
fun PayServicesScreen(
    account: AccountInfoEntity?,
    isBalanceHidden: Boolean,
    onBack: () -> Unit,
    onPayService: (
        serviceId: String,
        serviceName: String,
        category: String,
        supplyCode: String,
        amount: Double,
        commission: Double,
        comment: String
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Strictly real-time cloud data from Firestore: no mock, simulated, or hardcoded services
    var servicesList by remember { mutableStateOf<List<PublicService>>(emptyList()) }
    var isLoadingServices by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }

    var selectedService by remember { mutableStateOf<PublicService?>(null) }
    var supplyCode by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }

    // Real-time listener directly attached to Firestore /services collection
    DisposableEffect(Unit) {
        var listener: ListenerRegistration? = null
        coroutineScope.launch {
            val initial = FirebaseManager.getPublicServices().filter { it.active }
            servicesList = initial
            isLoadingServices = false
        }
        listener = FirebaseManager.listenToPublicServices { updatedList ->
            servicesList = updatedList.filter { it.active }
            isLoadingServices = false
        }
        onDispose {
            listener?.remove()
        }
    }

    // Dynamic categories extracted directly from real Firestore services
    val dynamicCategories = remember(servicesList) {
        val cats = servicesList.map { it.category }.filter { it.isNotBlank() }.distinct()
        listOf("Todos") + cats
    }

    val filteredServices = servicesList.filter { service ->
        val matchesCategory = (selectedCategory == "Todos" || service.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() ||
                service.name.contains(searchQuery, ignoreCase = true) ||
                service.category.contains(searchQuery, ignoreCase = true) ||
                service.code.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar with Back Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedService != null) {
                                selectedService = null
                                supplyCode = ""
                                amountText = ""
                                commentText = ""
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF151620))
                            .border(1.dp, BorderGlass, CircleShape)
                            .testTag("pay_services_back_button")
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
                            text = if (selectedService != null) "Completar pago de recibo" else "Pagar servicios",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color = TextPrimary
                        )
                        Text(
                            text = if (selectedService != null) selectedService!!.name else "Luz, agua, gas, telefonía, impuestos y más",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 2. Balance Chip
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF101117))
                        .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo disponible:",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = Formatters.formatSolesHidden(account?.balance ?: 0.0, isBalanceHidden),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                }
            }

            // ================= PAYMENT FORM (WHEN SERVICE IS SELECTED) =================
            if (selectedService != null) {
                val service = selectedService!!
                val (icon, iconColor) = getServiceIconAndColor(service.category)
                val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                val totalDebit = parsedAmount + service.commission
                val currentBal = account?.balance ?: 0.0
                val hasSufficientBalance = currentBal >= totalDebit
                val isSupplyCodeValid = supplyCode.trim().length >= service.supplyCodeMinLength

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Selected Service Header Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(iconColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = service.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Rubro: ${service.category}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = "Cambiar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryVioletLight,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF221E3B))
                                        .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                                        .clickable { selectedService = null }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            if (service.description.isNotBlank()) {
                                Text(
                                    text = service.description,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }

                            HorizontalDivider(color = BorderDark, thickness = 1.dp)

                            // 1. Suministro Input
                            OutlinedTextField(
                                value = supplyCode,
                                onValueChange = { supplyCode = it },
                                label = { Text(service.supplyCodeLabel) },
                                placeholder = { Text(service.supplyCodePlaceholder) },
                                leadingIcon = {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = PrimaryVioletLight)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryViolet,
                                    unfocusedBorderColor = BorderGlass,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = PrimaryVioletLight,
                                    unfocusedLabelColor = TextSecondary,
                                    focusedContainerColor = Color(0xFF141520),
                                    unfocusedContainerColor = Color(0xFF141520)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("supply_code_input")
                            )

                            // 2. Preset Quick Amounts
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Montos sugeridos:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PRESET_AMOUNTS.forEach { preset ->
                                        val isSelected = amountText == "%.2f".format(preset) || amountText == "${preset.toInt()}"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFF261E42) else Color(0xFF141520))
                                                .border(1.dp, if (isSelected) PrimaryViolet else BorderGlass, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    amountText = "%.2f".format(preset)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "S/ ${preset.toInt()}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFFC7D2FE) else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Monto Libre a Pagar
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("Monto a pagar en Soles (S/)") },
                                placeholder = { Text("0.00") },
                                leadingIcon = {
                                    Text(
                                        "S/",
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryViolet,
                                    unfocusedBorderColor = BorderGlass,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = PrimaryVioletLight,
                                    unfocusedLabelColor = TextSecondary,
                                    focusedContainerColor = Color(0xFF141520),
                                    unfocusedContainerColor = Color(0xFF141520)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("service_amount_input")
                            )

                            // 4. Comentario / Nota adicional
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                label = { Text("Comentario o Referencia (Opcional)") },
                                placeholder = { Text("Ej: Recibo mes actual") },
                                leadingIcon = {
                                    Icon(Icons.Default.Comment, contentDescription = null, tint = TextSecondary)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryViolet,
                                    unfocusedBorderColor = BorderGlass,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedLabelColor = PrimaryVioletLight,
                                    unfocusedLabelColor = TextSecondary,
                                    focusedContainerColor = Color(0xFF141520),
                                    unfocusedContainerColor = Color(0xFF141520)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("service_comment_input")
                            )

                            // Summary Voucher Preview Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF141520))
                                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Empresa:", fontSize = 12.sp, color = TextSecondary)
                                        Text(service.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Suministro:", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            if (supplyCode.isNotBlank()) supplyCode else "Por ingresar",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Comisión:", fontSize = 12.sp, color = TextSecondary)
                                        val comText = if (service.commission > 0) "S/ ${String.format("%.2f", service.commission)}" else "S/ 0.00 (Sin comisión)"
                                        Text(comText, fontSize = 12.sp, color = AccentGold)
                                    }
                                    if (parsedAmount > 0) {
                                        HorizontalDivider(
                                            color = BorderDark,
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Total a debitar:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text(
                                                Formatters.formatSoles(totalDebit),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (hasSufficientBalance) AccentGold else Color(0xFFEF4444)
                                            )
                                        }
                                        if (!hasSufficientBalance) {
                                            Text(
                                                text = "⚠️ Saldo insuficiente para procesar este pago",
                                                fontSize = 11.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }
                            }

                            // Pay Button
                            val canSubmit = isSupplyCodeValid && parsedAmount > 0 && hasSufficientBalance

                            Button(
                                onClick = {
                                    onPayService(
                                        service.id,
                                        service.name,
                                        service.category,
                                        supplyCode.trim(),
                                        parsedAmount,
                                        service.commission,
                                        commentText.trim()
                                    )
                                },
                                enabled = canSubmit,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryViolet,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color(0xFF1E202E),
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("confirm_pay_service_button")
                            ) {
                                Text(
                                    text = if (parsedAmount > 0) "Pagar ${Formatters.formatSoles(totalDebit)}" else "Ingresa el monto",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // ================= SERVICES CATALOG & DIRECTORY =================

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar servicio (ej. Enel, Sedapal, Cálidda)") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = BorderGlass,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = Color(0xFF101117),
                            unfocusedContainerColor = Color(0xFF101117)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_services_input")
                    )
                }

                // Horizontal Category Filter Pills (Dynamically loaded from Firestore)
                if (dynamicCategories.size > 1) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dynamicCategories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF261E42) else Color(0xFF101117))
                                        .border(1.dp, if (isSelected) PrimaryViolet else BorderGlass, RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFC7D2FE) else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                if (isLoadingServices) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = PrimaryViolet,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Cargando servicios...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Consultando catálogo oficial de servicios y convenios...",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (servicesList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF221E3B))
                                        .border(1.dp, BorderGlass, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = PrimaryVioletLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "Sin Servicios Disponibles",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "En este momento no hay empresas o servicios de pago habilitados. Por favor, intenta más tarde o comunícate con atención al cliente.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedCategory == "Todos") "Empresas y Servicios (${filteredServices.size})" else "$selectedCategory (${filteredServices.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    if (filteredServices.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101117)),
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGlass),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "No se encontraron coincidencias",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Intenta buscar con otro término o selecciona 'Todos' en las categorías.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredServices, key = { it.id }) { service ->
                        val (icon, iconColor) = getServiceIconAndColor(service.category)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF101117))
                                .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
                                .clickable {
                                    selectedService = service
                                    supplyCode = ""
                                    amountText = ""
                                    commentText = ""
                                }
                                .padding(14.dp)
                                .testTag("service_item_${service.id}")
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
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(iconColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = service.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = service.category,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        if (service.commission > 0) {
                                            Text(
                                                text = "Comisión: S/ ${String.format("%.2f", service.commission)}",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Pagar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryVioletLight
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "→",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryVioletLight
                                    )
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
