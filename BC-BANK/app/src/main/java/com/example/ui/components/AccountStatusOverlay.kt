package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.AccountSecurityStatus
import com.example.data.firebase.AccountStatusType
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.SupportChannel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
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
fun AccountStatusOverlay(
    statusInfo: AccountSecurityStatus?,
    supportChannels: List<SupportChannel> = emptyList(),
    onLogout: () -> Unit
) {
    if (statusInfo == null || statusInfo.status == AccountStatusType.ACTIVE) return

    val context = LocalContext.current
    val isSuspended = statusInfo.status == AccountStatusType.SUSPENDED
    val primaryColor = if (isSuspended) WarningYellow else ExpenseRed
    val iconVector = if (isSuspended) Icons.Default.Schedule else Icons.Default.Block

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    val activeChannels = remember(supportChannels) {
        val list = if (supportChannels.isNotEmpty()) {
            supportChannels.filter { it.isAvailable }
        } else {
            FirebaseManager.getDefaultLocalSupportChannels()
        }
        list.sortedWith(compareByDescending<SupportChannel> { it.isPrimary }.thenBy { it.priority })
    }

    Dialog(
        onDismissRequest = { /* Blocking modal dialog */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark.copy(alpha = 0.98f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Icon
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(2.dp, primaryColor.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Security Label
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSuspended) "RESTRICCIÓN TEMPORAL" else "BLOQUEO DE SEGURIDAD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    Text(
                        text = statusInfo.title.ifBlank {
                            if (isSuspended) "Cuenta Temporalmente Suspendida" else "Cuenta Bloqueada"
                        },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    // Reason Card
                    CardItem(
                        title = "Motivo de la resolución bancaria",
                        value = statusInfo.reason.ifBlank {
                            if (isSuspended) "Revisión preventiva de seguridad por actividad inusual."
                            else "La cuenta ha sido bloqueada permanentemente por políticas de seguridad y prevención."
                        },
                        highlightColor = primaryColor
                    )

                    // Expiration or Date Details
                    if (isSuspended && statusInfo.suspendedUntil != null && statusInfo.suspendedUntil > 0) {
                        val formattedDate = dateFormatter.format(Date(statusInfo.suspendedUntil))
                        CardItem(
                            title = "Fin de la suspensión",
                            value = "$formattedDate (Levantamiento automático)",
                            highlightColor = WarningYellow
                        )
                    } else if (!isSuspended && statusInfo.blockedAt != null) {
                        val formattedDate = dateFormatter.format(Date(statusInfo.blockedAt))
                        CardItem(
                            title = "Fecha de registro",
                            value = formattedDate,
                            highlightColor = ExpenseRed
                        )
                    }

                    HorizontalDivider(color = BorderDark, thickness = 1.dp)

                    // Support Channels Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Canales de Atención y Apelación Oficial",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Comunícate con uno de nuestros canales oficiales disponibles para solicitar revisión inmediata de tu caso:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Available Support Channels List
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        activeChannels.forEach { channel ->
                            SupportChannelCard(
                                channel = channel,
                                onOpen = {
                                    launchChannelAction(context, channel)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Logout Button
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cerrar Sesión", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SupportChannelCard(
    channel: SupportChannel,
    onOpen: () -> Unit
) {
    val (icon, badgeColor, buttonText, containerBg) = when (channel.type.uppercase()) {
        "WHATSAPP" -> ChannelVisuals(
            Icons.Default.Send,
            Color(0xFF25D366),
            "Chatear por WhatsApp",
            Color(0xFF064E3B).copy(alpha = 0.35f)
        )
        "TELEGRAM" -> ChannelVisuals(
            Icons.Default.Send,
            Color(0xFF38BDF8),
            "Abrir Telegram",
            Color(0xFF0369A1).copy(alpha = 0.30f)
        )
        "EMAIL" -> ChannelVisuals(
            Icons.Default.Email,
            Color(0xFFA78BFA),
            "Enviar Correo",
            Color(0xFF4C1D95).copy(alpha = 0.30f)
        )
        "PHONE" -> ChannelVisuals(
            Icons.Default.Call,
            EmeraldLight,
            "Llamar a Central",
            EmeraldDark.copy(alpha = 0.40f)
        )
        else -> ChannelVisuals(
            Icons.Default.HeadsetMic,
            EmeraldLight,
            "Contactar Soporte",
            SurfaceElevated
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (channel.isPrimary) badgeColor.copy(alpha = 0.6f) else BorderDark
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = channel.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = channel.value,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }
                }

                if (channel.isPrimary) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PRINCIPAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            if (channel.description.isNotBlank()) {
                Text(
                    text = channel.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }

            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(
                    containerColor = badgeColor.copy(alpha = 0.9f),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

private fun launchChannelAction(context: android.content.Context, channel: SupportChannel) {
    try {
        val customUrl = channel.actionUrl.trim()
        if (customUrl.isNotBlank()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        when (channel.type.uppercase()) {
            "WHATSAPP" -> {
                val cleanDigits = channel.value.filter { it.isDigit() }
                val targetPhone = if (cleanDigits.startsWith("51")) cleanDigits else "51$cleanDigits"
                val uri = Uri.parse("https://wa.me/$targetPhone?text=Hola%20BC-BANK,%20solicito%20asistencia%20con%20mi%20cuenta")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            "TELEGRAM" -> {
                val handle = channel.value.replace("@", "").trim()
                val uri = if (channel.value.startsWith("http")) Uri.parse(channel.value) else Uri.parse("https://t.me/$handle")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            "EMAIL" -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${channel.value.trim()}")
                    putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre Estado de Cuenta BC-BANK")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            "PHONE" -> {
                val cleanPhone = channel.value.trim()
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            else -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(channel.value)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    } catch (_: Exception) {}
}

private data class ChannelVisuals(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badgeColor: Color,
    val buttonText: String,
    val containerBg: Color
)

@Composable
private fun CardItem(
    title: String,
    value: String,
    highlightColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
