package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.util.CustomCategoryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.util.ImageExportUtils
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.BudgetEntity
import com.example.data.local.SavingsGoalEntity
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TransferReceipt

@Composable
fun DepositMoneyDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingresar Dinero",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Acredita saldo a tu cuenta BC-BANK Soles",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Amount Chips
                Text(
                    text = "Montos rápidos:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(50.0, 100.0, 200.0, 500.0).forEach { quickAmt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { amountText = quickAmt.toInt().toString() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S/ ${quickAmt.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto en Soles (S/)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Concepto o Motivo (Opcional)") },
                    placeholder = { Text("Ej. Depósito agente, Devolución") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_note_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onConfirm(amount, noteText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_deposit_button")
                ) {
                    Text(
                        text = "Acreditar Fondos (S/)",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Double, initial: Double, icon: String, date: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("SAVINGS") }
    var targetDate by remember { mutableStateOf("31/12/2026") }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories by CustomCategoryManager.goalCategories.collectAsStateWithLifecycle()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            targetDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Seleccionar", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceDark
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary,
                    headlineContentColor = EmeraldLight,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextPrimary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = EmeraldPrimary,
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = EmeraldPrimary,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = EmeraldPrimary,
                    todayContentColor = EmeraldLight,
                    todayDateBorderColor = EmeraldPrimary
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nueva Meta de Ahorro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Meta") },
                    placeholder = { Text("Ej. Fondo de Emergencia, Viaje Cusco") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Monto Objetivo (S/)") },
                    placeholder = { Text("5000.00") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_target_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = initialText,
                    onValueChange = { initialText = it },
                    label = { Text("Aporte Inicial (Opcional)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Estimated Date with Mini Calendar Trigger
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    readOnly = true,
                    label = { Text("Fecha Estimada Límite") },
                    placeholder = { Text("dd/MM/yyyy") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.testTag("btn_open_goal_date_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha en calendario",
                                tint = EmeraldPrimary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Categoría vinculada:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { catItem ->
                            val isSelected = selectedIcon == catItem.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) EmeraldDark else SurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) EmeraldLight else BorderDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedIcon = catItem.name }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catItem.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldLight else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 0.0
                        val initial = initialText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, target, initial, selectedIcon, targetDate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_create_goal_button")
                ) {
                    Text(
                        text = "Guardar Meta",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DepositToGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val isCompleted = goal.status == "COMPLETED" || (goal.currentAmount >= goal.targetAmount && goal.targetAmount > 0)
    val isWithdrawn = goal.status == "WITHDRAWN"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Abonar a Meta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = goal.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                )

                if (isCompleted || isWithdrawn) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldDark.copy(alpha = 0.5f))
                            .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isWithdrawn) "Esta meta ya fue retirada y finalizada." else "¡Meta 100% Alcanzada!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Text(
                                text = if (isWithdrawn) "Puedes crear una nueva meta de ahorro cuando gustes." else "Ya has acumulado el monto objetivo de ${Formatters.formatSoles(goal.targetAmount)}. Para usar tus fondos, selecciona 'Retirar Ahorro'.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Cerrar", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Ahorrado: ${Formatters.formatSoles(goal.currentAmount)} de ${Formatters.formatSoles(goal.targetAmount)} (Falta ${Formatters.formatSoles(remaining)})",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Amount Chips (limited to remaining amount)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50.0, 100.0, 200.0, remaining).distinct().filter { it > 0 }.take(4).forEach { quickAmt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable {
                                        amountText = if (quickAmt == remaining) "%.2f".format(Locale.US, remaining) else quickAmt.toInt().toString()
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (quickAmt == remaining) "Completar" else "S/ ${quickAmt.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto a Abonar (S/)") },
                        placeholder = { Text("0.00") },
                        prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_deposit_amount_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            onConfirm(amount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_goal_deposit_button")
                    ) {
                        Text(
                            text = "Confirmar Abono",
                            fontWeight = FontWeight.Bold,
                            color = BackgroundDark,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSavingsGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Double, icon: String, date: String) -> Unit
) {
    var name by remember { mutableStateOf(goal.name) }
    var targetText by remember { mutableStateOf(goal.targetAmount.toInt().toString()) }
    var selectedIcon by remember { mutableStateOf(goal.categoryIcon) }
    var targetDate by remember { mutableStateOf(goal.targetDate.ifBlank { "31/12/2026" }) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories by CustomCategoryManager.goalCategories.collectAsStateWithLifecycle()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            targetDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Seleccionar", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceDark
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary,
                    headlineContentColor = EmeraldLight,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextPrimary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = EmeraldPrimary,
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = EmeraldPrimary,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = EmeraldPrimary,
                    todayContentColor = EmeraldLight,
                    todayDateBorderColor = EmeraldPrimary
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Meta de Ahorro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Meta") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_goal_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Monto Objetivo (S/)") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_goal_target_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    readOnly = true,
                    label = { Text("Fecha Estimada Límite") },
                    placeholder = { Text("dd/MM/yyyy") },
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.testTag("btn_edit_goal_date_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha en calendario",
                                tint = EmeraldPrimary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                )

                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Categoría vinculada:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { catItem ->
                            val isSelected = selectedIcon == catItem.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) EmeraldDark else SurfaceCard)
                                    .border(1.dp, if (isSelected) EmeraldLight else BorderDark, RoundedCornerShape(10.dp))
                                    .clickable { selectedIcon = catItem.name }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catItem.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) EmeraldLight else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val target = targetText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, target, selectedIcon, targetDate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_edit_goal_button")
                ) {
                    Text(
                        text = "Guardar Cambios",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteSavingsGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: (returnFunds: Boolean) -> Unit
) {
    var returnFunds by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ExpenseRed.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRedLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "¿Eliminar Meta?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Estás a punto de eliminar la meta de ahorro '${goal.name}'.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                if (goal.currentAmount > 0) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reintegrar saldo a mi cuenta",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Se devolverán ${Formatters.formatSoles(goal.currentAmount)} a tu saldo disponible.",
                                    fontSize = 11.sp,
                                    color = EmeraldLight
                                )
                            }
                            Switch(
                                checked = returnFunds,
                                onCheckedChange = { returnFunds = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = EmeraldPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = SurfaceElevated
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }

                    Button(
                        onClick = { onConfirmDelete(returnFunds) },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("confirm_delete_goal_button")
                    ) {
                        Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawFromGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    val isCompleted = goal.status == "COMPLETED" || (goal.currentAmount >= goal.targetAmount && goal.targetAmount > 0)
    var amountText by remember { mutableStateOf(if (isCompleted) "%.2f".format(Locale.US, goal.currentAmount) else "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isCompleted) EmeraldLight.copy(alpha = 0.6f) else BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompleted) "Retirar Ahorro Cumplido" else "Retirar de Meta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = goal.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                )

                if (isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldDark.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "¡Felicitaciones! Has completado tu meta de ahorro al 100%. Los fondos se transferirán como un abono directo a tu cuenta disponible y se generará tu constancia oficial.",
                            fontSize = 12.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Fondos acumulados disponibles: ${Formatters.formatSoles(goal.currentAmount)}",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick percentage chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.25 to "25%", 0.50 to "50%", 0.75 to "75%", 1.0 to "Todo").forEach { (frac, label) ->
                        val chipAmt = if (frac == 1.0) "%.2f".format(Locale.US, goal.currentAmount) else "%.2f".format(Locale.US, goal.currentAmount * frac)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { amountText = chipAmt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto a Retirar a la Cuenta (S/)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_withdraw_amount_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onConfirm(amount)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_goal_withdraw_button")
                ) {
                    Text(
                        text = if (isCompleted) "Retirar Total a Mi Saldo" else "Transferir a Mi Saldo",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, limit: Double) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    val customBudgetCategories by CustomCategoryManager.budgetCategories.collectAsStateWithLifecycle()
    val presetCategories = customBudgetCategories.map { it.name }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Presupuesto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría del Presupuesto") },
                    placeholder = { Text("Ej. Gimnasio, Mascotas, Estudios o Otros") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_category_input")
                )

                if (presetCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tus categorías creadas:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetCategories.forEach { cat ->
                            val isSelected = category.equals(cat, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldDark else SurfaceCard)
                                    .border(1.dp, if (isSelected) EmeraldLight else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { category = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (isSelected) EmeraldLight else TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Límite Mensual Máximo (S/)") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Montos sugeridos:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(200.0, 500.0, 1000.0, 2000.0).forEach { quickAmt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { limitText = quickAmt.toInt().toString() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S/ ${quickAmt.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 0.0
                        onConfirm(category.ifBlank { "Otros" }, limit)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_new_budget_button")
                ) {
                    Text(
                        text = "Guardar Presupuesto",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EditBudgetDialog(
    budget: BudgetEntity,
    onDismiss: () -> Unit,
    onConfirm: (category: String, newLimit: Double) -> Unit,
    onResetSpent: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var category by remember { mutableStateOf(budget.category) }
    var limitText by remember { mutableStateOf(budget.monthlyLimit.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestionar Presupuesto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Nombre de la Categoría") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Límite Mensual (S/)") },
                    prefix = { Text("S/ ", color = EmeraldPrimary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Gasto registrado este ciclo: ${Formatters.formatSoles(budget.spentAmount)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Reset counter and Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetSpent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reiniciar", fontSize = 12.sp, color = EmeraldLight)
                    }

                    OutlinedButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRedLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRedLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 12.sp, color = ExpenseRedLight)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val newLimit = limitText.toDoubleOrNull() ?: 0.0
                        onConfirm(category.ifBlank { "Otros" }, newLimit)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_budget_limit_button")
                ) {
                    Text(
                        text = "Guardar Presupuesto",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteBudgetDialog(
    budget: BudgetEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ExpenseRed.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRedLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "¿Eliminar Presupuesto?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "¿Estás seguro de que deseas eliminar el presupuesto de '${budget.category}'? Esta acción no se puede deshacer.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }

                    Button(
                        onClick = onConfirmDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("confirm_delete_budget_button")
                    ) {
                        Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TransferReceiptDialog(
    receipt: TransferReceipt,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EmeraldDark)
                        .border(2.dp, EmeraldPrimary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Transferencia Exitosa!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Constancia Digital de Pago",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = Formatters.formatSoles(receipt.amount),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldLight
                )

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = BorderDark)

                Spacer(modifier = Modifier.height(16.dp))

                ReceiptRow(label = "Destinatario", value = receipt.recipient)
                ReceiptRow(label = "Cuenta / Celular", value = receipt.accountOrPhone)
                ReceiptRow(label = "Concepto", value = receipt.concept)
                ReceiptRow(label = "Categoría", value = receipt.category)
                ReceiptRow(label = "N° de Operación", value = receipt.operationCode, highlight = true)
                ReceiptRow(label = "Fecha y Hora", value = Formatters.formatDateTime(receipt.timestamp))

                Spacer(modifier = Modifier.height(20.dp))

                val context = LocalContext.current
                var savedSuccess by remember { mutableStateOf(false) }

                if (savedSuccess) {
                    Text(
                        text = "✓ Constancia guardada en la galería",
                        color = EmeraldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val bitmap = ImageExportUtils.generateReceiptBitmap(
                                title = "Transferencia a ${receipt.recipient}",
                                opCode = receipt.operationCode,
                                amount = receipt.amount,
                                beneficiary = receipt.recipient,
                                concept = receipt.concept
                            )
                            val uri = ImageExportUtils.saveBitmapToGallery(
                                context = context,
                                bitmap = bitmap,
                                title = "BCBANK_TRANSFER_${receipt.operationCode.filter { it.isLetterOrDigit() }}"
                            )
                            if (uri != null) {
                                savedSuccess = true
                                Toast.makeText(context, "¡Constancia guardada en galería!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al guardar constancia", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("receipt_save_button")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Guardar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val bitmap = ImageExportUtils.generateReceiptBitmap(
                                title = "Transferencia a ${receipt.recipient}",
                                opCode = receipt.operationCode,
                                amount = receipt.amount,
                                beneficiary = receipt.recipient,
                                concept = receipt.concept
                            )
                            ImageExportUtils.shareReceiptCard(
                                context = context,
                                bitmap = bitmap,
                                opCode = receipt.operationCode,
                                amount = receipt.amount,
                                beneficiary = receipt.recipient
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("receipt_share_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Compartir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("receipt_close_button")
                ) {
                    Text(
                        text = "Listo",
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) EmeraldLight else TextPrimary,
            textAlign = TextAlign.End
        )
    }
}
