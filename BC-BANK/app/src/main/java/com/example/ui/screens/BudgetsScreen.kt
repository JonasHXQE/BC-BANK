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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BudgetEntity
import com.example.ui.components.ElegantProgressBar
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
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow

@Composable
fun BudgetsScreen(
    budgets: List<BudgetEntity>,
    isBalanceHidden: Boolean,
    onOpenAddBudget: () -> Unit,
    onEditBudget: (BudgetEntity) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    val totalLimit = remember(budgets) { budgets.sumOf { it.monthlyLimit } }
    val totalSpent = remember(budgets) { budgets.sumOf { it.spentAmount } }
    val remainingOverall = (totalLimit - totalSpent).coerceAtLeast(0.0)
    val overallRatio = if (totalLimit > 0) (totalSpent / totalLimit).toFloat() else 0f

    val overallColor = when {
        overallRatio < 0.70f -> AccentCyan
        overallRatio < 1.0f -> AccentGold
        else -> ExpenseRed
    }

    val overallStatusText = when {
        overallRatio < 0.70f -> "Finanzas Bajo Control"
        overallRatio < 1.0f -> "Cerca del Límite Mensual"
        else -> "Presupuesto Sobrepasado"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x306D5BFF), Color(0x1038BDF8), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(250f, 180f),
                    radius = 900f
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
        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Presupuestos Mensuales",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = "Control inteligente de gastos por categoría",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onOpenAddBudget,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryViolet)
                        .border(1.dp, BorderGlass, CircleShape)
                        .testTag("add_budget_header_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Nuevo Presupuesto",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Summary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF141520),
                                Color(0xFF101117),
                                overallColor.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(overallColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = overallStatusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = overallColor
                            )
                        }

                        Text(
                            text = "${(overallRatio * 100).toInt()}% gastado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Gastado", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = Formatters.formatSolesHidden(totalSpent, isBalanceHidden),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Límite Asignado", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = Formatters.formatSoles(totalLimit),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ElegantProgressBar(
                        progress = overallRatio,
                        barColor = overallColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (totalSpent <= totalLimit) {
                            "Disponible para gastar: ${Formatters.formatSoles(remainingOverall)}"
                        } else {
                            "Exceso de gasto: ${Formatters.formatSoles(totalSpent - totalLimit)}"
                        },
                        fontSize = 12.sp,
                        color = if (totalSpent <= totalLimit) AccentCyan else ExpenseRedLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Legend banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101117))
                    .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = AccentCyan, text = "< 70% Control")
                LegendItem(color = AccentGold, text = "70-99% Alerta")
                LegendItem(color = ExpenseRed, text = "≥ 100% Exceso")
            }
        }

        // Empty State or Categories List
        if (budgets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF101117))
                        .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF221E3B))
                                .border(1.dp, BorderGlass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = PrimaryVioletLight,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No tienes presupuestos registrados",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Presiona '+' para asignar un límite por categoría",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(budgets) { budget ->
                BudgetCategoryCard(
                    budget = budget,
                    isBalanceHidden = isBalanceHidden,
                    onEditClick = { onEditBudget(budget) },
                    onDeleteClick = { onDeleteBudget(budget) }
                )
            }
        }
    }
}
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun BudgetCategoryCard(
    budget: BudgetEntity,
    isBalanceHidden: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val ratio = if (budget.monthlyLimit > 0) (budget.spentAmount / budget.monthlyLimit).toFloat() else 0f
    val percentage = (ratio * 100).toInt()

    val statusColor = when {
        ratio < 0.70f -> AccentCyan
        ratio < 1.0f -> AccentGold
        else -> ExpenseRed
    }

    val statusBadgeText = when {
        ratio < 0.70f -> "Bajo control"
        ratio < 1.0f -> "Cerca del límite"
        else -> "Sobrepasado"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF101117))
            .border(1.dp, if (ratio >= 1.0f) ExpenseRed.copy(alpha = 0.5f) else BorderGlass, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("budget_card_${budget.category}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(budget.category),
                            contentDescription = budget.category,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = budget.category,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = statusBadgeText,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$percentage%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_budget_${budget.category}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Gestionar presupuesto",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_budget_${budget.category}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar presupuesto",
                            tint = ExpenseRedLight.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gastado: ${Formatters.formatSolesHidden(budget.spentAmount, isBalanceHidden)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Límite: ${Formatters.formatSoles(budget.monthlyLimit)}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ElegantProgressBar(
                progress = ratio,
                barColor = statusColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            val remaining = (budget.monthlyLimit - budget.spentAmount)
            Text(
                text = if (remaining >= 0) {
                    "Resta por gastar: ${Formatters.formatSoles(remaining)}"
                } else {
                    "Límite excedido por ${Formatters.formatSoles(-remaining)}"
                },
                fontSize = 11.sp,
                color = if (remaining >= 0) TextMuted else ExpenseRedLight
            )
        }
    }
}

