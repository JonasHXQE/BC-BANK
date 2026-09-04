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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Atm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BankNotificationEntity
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
import com.example.ui.theme.PrimaryVioletDark
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    notifications: List<BankNotificationEntity>,
    unreadCount: Int,
    onBack: () -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteNotification: (Long) -> Unit,
    onClearAll: () -> Unit,
    onShowAlert: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val categories = listOf("Todas", "Transacciones", "Depósitos", "Retiros", "Servicios", "Seguridad")

    val filteredList = notifications.filter {
        if (selectedFilter == "Todas") true else it.category.equals(selectedFilter, ignoreCase = true)
    }

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
        // 1. Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF101117))
                            .border(1.dp, BorderGlass, CircleShape)
                            .testTag("notifications_back_button")
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
                            text = "Notificaciones",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = TextPrimary
                        )
                        Text(
                            text = if (unreadCount > 0) "$unreadCount sin leer" else "Todas al día",
                            fontSize = 12.sp,
                            color = if (unreadCount > 0) PrimaryVioletLight else TextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = {
                                onMarkAllAsRead()
                                onShowAlert("Todas las notificaciones fueron marcadas como leídas")
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF101117))
                                .border(1.dp, BorderGlass, CircleShape)
                                .testTag("mark_all_read_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Marcar todas como leídas",
                                tint = PrimaryVioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF101117))
                                .border(1.dp, BorderGlass, CircleShape)
                                .testTag("clear_all_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Limpiar historial",
                                tint = ExpenseRedLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedFilter == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryVioletDark,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF101117),
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryViolet else BorderGlass
                        )
                    )
                }
            }
        }

        // 3. Notification List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF101117))
                                .border(1.dp, BorderGlass, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedFilter == "Todas") "No tienes notificaciones recibidas" else "No hay notificaciones en \"$selectedFilter\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tus movimientos bancarios, pagos y transferencias en tiempo real aparecerán aquí.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                val iconInfo = resolveNotificationIcon(item.type, item.category)
                val timeAgoStr = formatTimestampAgo(item.timestamp)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isRead) Color(0xFF101117) else Color(0xFF161822)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.isRead) BorderGlass else PrimaryViolet.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!item.isRead) {
                                onMarkAsRead(item.id)
                            }
                        }
                        .testTag("notification_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icon Circle
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(iconInfo.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconInfo.icon,
                                contentDescription = null,
                                tint = iconInfo.color,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (!item.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryViolet)
                                    )
                                }
                            }

                            Text(
                                text = item.message,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = timeAgoStr,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                if (!item.amountTag.isNullOrBlank()) {
                                    Text(
                                        text = item.amountTag,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.amountTag.startsWith("+")) EmeraldLight else ExpenseRedLight
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

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Limpiar Notificaciones",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar todas las notificaciones recibidas?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAll()
                        onShowAlert("Historial de notificaciones eliminado")
                    }
                ) {
                    Text("Eliminar Todas", color = ExpenseRedLight, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private data class NotificationIconInfo(
    val icon: ImageVector,
    val color: Color
)

private fun resolveNotificationIcon(type: String, category: String): NotificationIconInfo {
    return when {
        type == "TRANSFER_RECEIVED" -> NotificationIconInfo(Icons.Default.SouthWest, EmeraldLight)
        type == "TRANSFER_SENT" -> NotificationIconInfo(Icons.Default.SwapHoriz, EmeraldPrimary)
        type == "DEPOSIT_CONFIRMED" -> NotificationIconInfo(Icons.Default.ElectricBolt, EmeraldPrimary)
        type.startsWith("WITHDRAWAL") -> NotificationIconInfo(Icons.Default.Atm, WarningYellow)
        type == "SERVICE_PAYMENT" -> NotificationIconInfo(Icons.Default.ReceiptLong, Color(0xFF60A5FA))
        type == "SECURITY" || category.equals("Seguridad", ignoreCase = true) -> NotificationIconInfo(Icons.Default.Security, Color(0xFF818CF8))
        category.equals("Depósitos", ignoreCase = true) -> NotificationIconInfo(Icons.Default.ElectricBolt, EmeraldPrimary)
        category.equals("Retiros", ignoreCase = true) -> NotificationIconInfo(Icons.Default.Atm, WarningYellow)
        category.equals("Servicios", ignoreCase = true) -> NotificationIconInfo(Icons.Default.ReceiptLong, Color(0xFF60A5FA))
        else -> NotificationIconInfo(Icons.Default.Notifications, EmeraldPrimary)
    }
}

private fun formatTimestampAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)
    val seconds = diff / 1000L
    val minutes = seconds / 60L
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        seconds < 60 -> "Hace un momento"
        minutes < 60 -> "Hace $minutes min"
        hours < 24 -> "Hace $hours ${if (hours == 1L) "hora" else "horas"}"
        days < 2 -> {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            "Ayer, ${timeFmt.format(Date(timestamp))}"
        }
        else -> {
            val dateFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateFmt.format(Date(timestamp))
        }
    }
}
