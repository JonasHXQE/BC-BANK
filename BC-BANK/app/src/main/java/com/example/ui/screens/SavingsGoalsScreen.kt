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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavingsGoalEntity
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.ElegantProgressBar
import com.example.ui.components.Formatters
import com.example.ui.components.getGoalCategoryIcon
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CurrencyPurple
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SavingsGoalsScreen(
    goals: List<SavingsGoalEntity>,
    isBalanceHidden: Boolean,
    onOpenAddGoal: () -> Unit,
    onOpenDepositGoal: (SavingsGoalEntity) -> Unit,
    onOpenEditGoal: (SavingsGoalEntity) -> Unit,
    onOpenDeleteGoal: (SavingsGoalEntity) -> Unit,
    onOpenWithdrawGoal: (SavingsGoalEntity) -> Unit
) {
    val totalSaved = remember(goals) { goals.sumOf { it.currentAmount } }
    val totalTarget = remember(goals) { goals.sumOf { it.targetAmount } }
    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

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
        // Header Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Metas de ahorro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Text(
                        text = "Control y ahorro inteligente en Soles",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onOpenAddGoal,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryViolet)
                        .testTag("add_goal_header_button")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Nueva Meta",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Overview Summary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1B1828),
                                Color(0xFF111118),
                                Color(0xFF0C0D12)
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ahorro acumulado total",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(overallProgress * 100).toInt()}% de la meta",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentGold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = Formatters.formatSolesHidden(totalSaved, isBalanceHidden),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Meta global: ${Formatters.formatSoles(totalTarget)} • ${goals.size} metas activas",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ElegantProgressBar(
                        progress = overallProgress,
                        gradientColors = listOf(Color(0xFF6366F1), Color(0xFF22D3EE))
                    )
                }
            }
        }

        // Goals List
        if (goals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aún no tienes metas de ahorro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Presiona '+ Nueva' para crear tu primer objetivo",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(goals) { goal ->
                GoalItemCard(
                    goal = goal,
                    isBalanceHidden = isBalanceHidden,
                    onDepositClick = { onOpenDepositGoal(goal) },
                    onEditClick = { onOpenEditGoal(goal) },
                    onDeleteClick = { onOpenDeleteGoal(goal) },
                    onWithdrawClick = { onOpenWithdrawGoal(goal) }
                )
            }
        }
    }
}
}

@Composable
fun GoalItemCard(
    goal: SavingsGoalEntity,
    isBalanceHidden: Boolean,
    onDepositClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val isWithdrawn = goal.status == "WITHDRAWN"
    val isCompleted = goal.status == "COMPLETED" || (goal.currentAmount >= goal.targetAmount && goal.targetAmount > 0)

    val borderColor = when {
        isCompleted -> AccentGold.copy(alpha = 0.5f)
        else -> BorderGlass
    }

    val statusBadgeText = when {
        isWithdrawn -> "Ahorro Retirado"
        isCompleted -> "¡Completada!"
        else -> "$percentage%"
    }

    val statusBadgeBg = when {
        isWithdrawn -> SurfaceElevated
        isCompleted -> AccentGold.copy(alpha = 0.2f)
        else -> PrimaryViolet.copy(alpha = 0.2f)
    }

    val statusBadgeColor = when {
        isWithdrawn -> TextSecondary
        isCompleted -> AccentGold
        else -> Color(0xFFC7D2FE)
    }

    val gradientColors = when {
        isCompleted -> listOf(AccentGold, IncomeGreen)
        else -> listOf(PrimaryViolet, AccentCyan)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF111218))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("goal_card_${goal.id}")
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF191A24)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWithdrawn) Icons.Default.CheckCircle else getGoalCategoryIcon(goal.categoryIcon),
                            contentDescription = goal.name,
                            tint = if (isWithdrawn) TextSecondary else gradientColors.first(),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = goal.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWithdrawn) TextSecondary else TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isWithdrawn) "Meta finalizada" else "Límite: ${goal.targetDate}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isWithdrawn) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp).testTag("edit_goal_${goal.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp).testTag("delete_goal_${goal.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ExpenseRedLight.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusBadgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusBadgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(if (isWithdrawn) "Monto Retirado" else "Ahorrado", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = Formatters.formatSolesHidden(goal.currentAmount, isBalanceHidden),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWithdrawn) TextSecondary else Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Monto Objetivo", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = Formatters.formatSoles(goal.targetAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ElegantProgressBar(
                progress = if (isWithdrawn) 1f else progress,
                gradientColors = if (isWithdrawn) listOf(TextMuted, TextMuted) else gradientColors
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isWithdrawn -> "Ahorro retirado a tu cuenta disponible"
                        isCompleted -> "¡Objetivo alcanzado! Listo para retirar"
                        else -> "Falta ${Formatters.formatSoles(remaining)}"
                    },
                    fontSize = 12.sp,
                    color = if (isCompleted && !isWithdrawn) AccentGold else TextSecondary,
                    fontWeight = if (isCompleted && !isWithdrawn) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isWithdrawn) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Completada",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }
                    } else if (isCompleted) {
                        // When 100% completed: prominent Retirar Ahorro button and no more deposits
                        Button(
                            onClick = onWithdrawClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("withdraw_from_goal_${goal.id}")
                        ) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Retirar Ahorro",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        // In progress: can withdraw partial and can deposit
                        if (goal.currentAmount > 0) {
                            OutlinedButton(
                                onClick = onWithdrawClick,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("withdraw_from_goal_${goal.id}")
                            ) {
                                Text(
                                    text = "Retirar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = onDepositClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .border(1.dp, PrimaryViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .testTag("deposit_to_goal_${goal.id}")
                        ) {
                            Text(
                                text = "+ Abonar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }
                }
            }
        }
    }
}
