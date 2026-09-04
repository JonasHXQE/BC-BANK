package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BudgetEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.ui.components.ElegantProgressBar
import com.example.ui.components.Formatters
import com.example.ui.components.getCategoryIcon
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
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow

@Composable
fun AnalyticsScreen(
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    isBalanceHidden: Boolean
) {
    var showScoreInfoDialog by remember { mutableStateOf(false) }

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpenses = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" || it.type == "GOAL_DEPOSIT" }.sumOf { it.amount }
    }
    val netSavings = (totalIncome - totalExpenses).coerceAtLeast(0.0)
    val savingsRate = if (totalIncome > 0) ((netSavings / totalIncome) * 100).toInt().coerceIn(0, 100) else 0

    // Dynamic Financial Health Score Calculation (0-100)
    val financialScore = remember(transactions, totalIncome, totalExpenses, savingsRate, budgets) {
        if (transactions.isEmpty() || (totalIncome == 0.0 && totalExpenses == 0.0)) {
            0 // Strictly 0 if no transaction history exists
        } else {
            var score = 0
            // 1. Savings rate contribution (max 40 pts)
            score += (savingsRate * 0.4).toInt().coerceIn(0, 40)

            // 2. Positive cash flow contribution (max 30 pts)
            if (totalIncome >= totalExpenses) {
                val flowRatio = if (totalIncome > 0) ((totalIncome - totalExpenses) / totalIncome) else 0.0
                score += (flowRatio * 30).toInt().coerceIn(10, 30)
            } else {
                score += 5
            }

            // 3. Budgets health contribution (max 30 pts)
            if (budgets.isNotEmpty()) {
                val compliantCount = budgets.count { it.spentAmount <= it.monthlyLimit }
                score += ((compliantCount.toDouble() / budgets.size) * 30).toInt()
            } else {
                score += 20 // Default baseline when no budgets configured
            }

            score.coerceIn(1, 100)
        }
    }

    val healthLabel = when {
        financialScore == 0 -> "Salud Financiera: Sin movimientos"
        financialScore >= 80 -> "Salud Financiera: Excelente"
        financialScore >= 60 -> "Salud Financiera: Buena"
        financialScore >= 40 -> "Salud Financiera: Moderada"
        else -> "Salud Financiera: En Alerta"
    }

    val healthColor = when {
        financialScore == 0 -> TextMuted
        financialScore >= 80 -> EmeraldLight
        financialScore >= 60 -> EmeraldPrimary
        financialScore >= 40 -> WarningYellow
        else -> ExpenseRedLight
    }

    // Group expenses by category
    val expensesByCategory = remember(transactions) {
        transactions
            .filter { it.type == "EXPENSE" || it.type == "GOAL_DEPOSIT" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
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
                .height(320.dp)
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
        // Header
        item {
            Column {
                Text(
                    text = "Análisis financiero",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color = TextPrimary
                )
                Text(
                    text = "Rendimiento y distribución de tus Soles",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Financial Score & Health Gauge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF141524),
                                Color(0xFF1B1936),
                                Color(0xFF12131D)
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = healthColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = healthLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = healthColor
                            )
                            IconButton(
                                onClick = { showScoreInfoDialog = true },
                                modifier = Modifier.size(24.dp).testTag("btn_health_score_info")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Información del Score",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tasa de ahorro: $savingsRate%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = if (totalIncome > 0) {
                                "Ahorraste ${Formatters.formatSolesHidden(netSavings, isBalanceHidden)} de tus ingresos en Soles."
                            } else {
                                "Registra ingresos y gastos para calcular tus ahorros."
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D1B2E))
                            .border(2.dp, healthColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$financialScore",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = healthColor
                        )
                    }
                }
            }
        }

        // Cash Flow Comparison Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF101117))
                    .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Flujo de caja mensual",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Income column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF141520))
                                .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Total Ingresos", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Formatters.formatSolesHidden(totalIncome, isBalanceHidden),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        }

                        // Expense column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF141520))
                                .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = ExpenseRedLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Total Gastos", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Formatters.formatSolesHidden(totalExpenses, isBalanceHidden),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Category Expenses Breakdown
        item {
            Text(
                text = "Distribución de gastos por categoría",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        if (expensesByCategory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin gastos suficientes para graficar",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(expensesByCategory) { (category, amount) ->
                val ratio = if (totalExpenses > 0) (amount / totalExpenses).toFloat() else 0f
                val percentage = (ratio * 100).toInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF101117))
                        .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
                        .padding(14.dp)
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF181A26)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(category),
                                        contentDescription = category,
                                        tint = PrimaryVioletLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = category,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatSoles(amount),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$percentage% del total",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ElegantProgressBar(
                            progress = ratio,
                            barColor = when (category) {
                                "Alimentación" -> PrimaryViolet
                                "Servicios" -> InfoBlue
                                "Entretenimiento" -> PurpleAccent
                                "Transporte" -> AccentGold
                                "Ahorro" -> EmeraldLight
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }
        }
    }

    if (showScoreInfoDialog) {
        HealthScoreInfoDialog(onDismiss = { showScoreInfoDialog = false })
    }
}
}

@Composable
private fun HealthScoreInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF13141E),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp),
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
                                .background(Color(0xFF221E3B))
                                .border(1.dp, BorderGlass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryVioletLight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Salud financiera",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Text(
                    text = "El puntaje de Salud Financiera evalúa tu disciplina y estabilidad económica en una escala de 0 a 100 puntos en tiempo real:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1017))
                        .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCriteriaRow(
                        title = "1. Tasa de ahorro (Hasta 40 pts)",
                        desc = "Porcentaje de tus ingresos totales retenidos tras descontar egresos y metas."
                    )
                    ScoreCriteriaRow(
                        title = "2. Flujo de caja positivo (Hasta 30 pts)",
                        desc = "Garantiza que tus ingresos superen tus gastos mensuales de manera consistente."
                    )
                    ScoreCriteriaRow(
                        title = "3. Cumplimiento de presupuestos (Hasta 30 pts)",
                        desc = "Mantener los gastos por debajo de los topes configurados en cada categoría."
                    )
                    ScoreCriteriaRow(
                        title = "4. Sin movimientos (0 pts)",
                        desc = "Se requiere registrar actividad de ingresos o gastos para iniciar la medición."
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ScoreCriteriaRow(title: String, desc: String) {
    Column {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryVioletLight)
        Text(text = desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
    }
}
