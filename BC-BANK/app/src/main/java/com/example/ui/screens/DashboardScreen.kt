package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.example.data.local.AccountInfoEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.ui.components.DarkGlassCard
import com.example.ui.components.ElegantProgressBar
import com.example.ui.components.Formatters
import com.example.ui.components.QuickActionButton
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.getGoalCategoryIcon
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AvatarPurple
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CurrencyPurple
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import com.example.ui.theme.WarningYellow
import com.example.ui.viewmodel.NavigationTab

@Composable
fun DashboardScreen(
    account: AccountInfoEntity?,
    transactions: List<TransactionEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    budgets: List<BudgetEntity>,
    isBalanceHidden: Boolean,
    selectedFilter: String,
    onTogglePrivacy: () -> Unit,
    onFilterChanged: (String) -> Unit,
    onNavigate: (NavigationTab) -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenPayServices: () -> Unit,
    onOpenWithdrawQr: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenTransactionDetail: (TransactionEntity) -> Unit,
    onOpenGoalDeposit: (SavingsGoalEntity) -> Unit,
    unreadNotificationsCount: Int = 0
) {

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" || it.type == "GOAL_DEPOSIT" }.sumOf { it.amount }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        // Atmospheric ambient blur glow at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x386D5BFF), // Ambient violet blur
                            Color(0x1438BDF8), // Subtle cyan touch
                            Color.Transparent
                        ),
                        radius = 700f,
                        center = Offset(x = 600f, y = 80f)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // 1. Header Profile & Bank Tag (Clickable to open Profile & Notifications)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenProfile() }
                        .padding(4.dp)
                        .testTag("dashboard_profile_button")
                ) {
                    val holderName = account?.accountHolder?.ifBlank { "Usuario" } ?: "Usuario"
                    val firstName = remember(holderName) {
                        holderName.split(" ").firstOrNull { it.isNotBlank() } ?: "Usuario"
                    }
                    val initials = remember(holderName) {
                        holderName.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                            .ifEmpty { "JH" }
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AvatarPurple)
                            .border(1.dp, Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hola, $firstName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cuenta Corriente • ${account?.bankName?.ifBlank { "BC Bank" } ?: "BC Bank"}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF14141B))
                        .border(1.dp, BorderGlass, CircleShape)
                        .clickable { onOpenNotifications() }
                        .testTag("dashboard_notifications_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                    }
                }

            }
        }

        // 2. Main Account Balance Card (Frosted Glass with Ambient Blur)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A1828), // Violet glass reflection
                                Color(0xFF111118),
                                Color(0xFF0C0D12)
                            )
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(26.dp))
                    .padding(22.dp)
                    .testTag("account_balance_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saldo total disponible",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = onTogglePrivacy,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("privacy_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Ocultar/Mostrar saldo",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isBalanceHidden) {
                        Text(
                            text = "S/ ••••••",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TextPrimary,
                            modifier = Modifier.testTag("total_balance_text")
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("total_balance_text")
                        ) {
                            Text(
                                text = "S/",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = CurrencyPurple,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = Formatters.formatAmountOnly(account?.balance ?: 0.0),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                letterSpacing = (-0.5).sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val cciText = account?.cciNumber?.ifBlank { "002-194-0081493000-84" } ?: "002-194-0081493000-84"
                    Text(
                        text = "CCI $cciText",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Income & Expense sub-pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Income Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C0E14))
                                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "↓ Ingresos (mes)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IncomeGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBalanceHidden) "S/ ••••••" else "S/ ${Formatters.formatAmountOnly(totalIncome)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Expense Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C0E14))
                                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "↑ Egresos (mes)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ExpenseRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBalanceHidden) "S/ ••••••" else "S/ ${Formatters.formatAmountOnly(totalExpense)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Actions Row (Transferir, Depositar, Servicios, Retiro / QR)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionButton(
                    label = "Transferir",
                    icon = Icons.Default.SwapHoriz,
                    isPrimaryFilled = true,
                    testTag = "action_transfer",
                    onClick = onOpenTransfer
                )
                QuickActionButton(
                    label = "Depositar",
                    icon = Icons.Default.ArrowDownward,
                    accentColor = IncomeGreen,
                    testTag = "action_deposit",
                    onClick = onOpenDeposit
                )
                QuickActionButton(
                    label = "Servicios",
                    icon = Icons.Default.Receipt,
                    accentColor = AccentGold,
                    testTag = "action_pay_services",
                    onClick = onOpenPayServices
                )
                QuickActionButton(
                    label = "Retiro / QR",
                    icon = Icons.Default.QrCodeScanner,
                    accentColor = AccentCyan,
                    testTag = "action_withdraw_qr",
                    onClick = onOpenWithdrawQr
                )
            }
        }

        // 4. Savings Goals Snapshot
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metas de ahorro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate(NavigationTab.METAS) }
                    ) {
                        Text(
                            text = "Ver todas →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(savingsGoals) { goal ->
                        val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                        val index = savingsGoals.indexOf(goal)
                        val gradientColors = if (index % 2 == 0) {
                            listOf(Color(0xFF6366F1), Color(0xFF22D3EE))
                        } else {
                            listOf(Color(0xFFF97316), Color(0xFFFF6B6B))
                        }

                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF111218))
                                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                                .clickable { onOpenGoalDeposit(goal) }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF191A24)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getGoalCategoryIcon(goal.categoryIcon),
                                            contentDescription = null,
                                            tint = gradientColors.last(),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gradientColors.first()
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = goal.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = Formatters.formatSolesCompact(goal.currentAmount),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = Formatters.formatSolesCompact(goal.targetAmount),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                ElegantProgressBar(
                                    progress = progress,
                                    gradientColors = gradientColors
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Activity Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividad reciente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = TextPrimary
                    )
                }

                // Filter Chips - fully scrollable and responsive so chips never squeeze or break text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "Todos", "INCOME" to "Ingresos", "EXPENSE" to "Egresos").forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF241D42) else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryViolet else BorderGlass,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onFilterChanged(key) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFC7D2FE) else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay movimientos registrados",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(transactions.take(15)) { tx ->
                TransactionRowItem(
                    tx = tx,
                    isBalanceHidden = isBalanceHidden,
                    onClick = { onOpenTransactionDetail(tx) }
                )
            }
        }
    }
}
}

@Composable
fun TransactionRowItem(
    tx: TransactionEntity,
    isBalanceHidden: Boolean,
    onClick: () -> Unit = {}
) {
    val isIncome = tx.type == "INCOME"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF101117))
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("tx_item_${tx.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF151620))
                        .border(
                            1.dp,
                            if (isIncome) IncomeGreen.copy(alpha = 0.25f) else ExpenseRed.copy(alpha = 0.25f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(tx.category),
                        contentDescription = tx.category,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tx.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "BC • ${Formatters.formatShortDate(tx.timestamp)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (isIncome) "+ " else "– "
                val color = if (isIncome) IncomeGreen else ExpenseRed

                Text(
                    text = if (isBalanceHidden) "S/ ••••••" else "$prefix${Formatters.formatSoles(tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                val refCode = tx.referenceNumber.ifBlank { "OP-171619" }
                Text(
                    text = refCode,
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
